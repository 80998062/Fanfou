/*
 *
 *  * Apache License
 *  *
 *  * Copyright [2017] Sinyuk
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.sinyuk.fanfou.domain

import android.arch.lifecycle.LiveData
import android.util.Log
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.entities.Player
import com.sinyuk.fanfou.domain.entities.Registration
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.domain.funcs.SaveStatusFunc
import com.sinyuk.fanfou.domain.rest.Authorization
import com.sinyuk.fanfou.domain.rest.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.rest.RemoteTasks
import com.sinyuk.fanfou.domain.room.LocalDatabase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by sinyuk on 2017/11/27.
 */
class Repository constructor(private val remoteTasks: RemoteTasks,
                             private val database: LocalDatabase,
                             private val interceptor: Oauth1SigningInterceptor,
                             private val preferences: RxSharedPreferences) {

    init {
        preferences.getString(UNIQUE_ID).asObservable()
                .subscribeOn(Schedulers.computation())
                .onErrorReturn {
                    it.printStackTrace()
                    ""
                }
                .subscribe { it ->
                    Log.d("Repository", "切换用户: " + it)
                    registration(it)
                            .subscribeOn(Schedulers.computation())
                            .onErrorReturn {
                                it.printStackTrace()
                                Registration()
                            }
                            .subscribe(Consumer { onAuthorize(Authorization(it?.token, it?.secret)) })
                }

    }


    fun signIn(account: String, password: String): Completable = remoteTasks.requestToken(account, password)
            .flatMap { authorization ->
                onAuthorize(authorization)
                return@flatMap remoteTasks.updateProfile()
                        .doOnError({ Log.e("updateProfile", it.message) })
                        .map {
                            Log.d("Repository", "<======= Save Registration ======>")
                            database.playerDao().insert(it)
                            database.registrationDao().insert(
                                    Registration(it.uniqueId, account, password, Date(System.currentTimeMillis()), authorization.token, authorization.secret))
                            preferences.getString(UNIQUE_ID).set(it.uniqueId)
                            it
                        }
                        .doOnError({ Log.e("Save Registration", it.message) })
                        .subscribeOn(Schedulers.computation())
            }.toCompletable()


    /**
     *  获取登录信息
     */
    fun registration(uniqueId: String): Single<Registration?> =
            Single.fromCallable { database.registrationDao().query(uniqueId) }.subscribeOn(Schedulers.computation())

    /**
     * 删除登录信息
     */
    fun deleteRegistration(uniqueId: String): Completable =
            Completable.fromCallable { database.registrationDao().delete(Registration(uniqueId)) }
                    .subscribeOn(Schedulers.computation())

    /**
     *  获取所有用户
     */
    fun admins(): LiveData<List<Player>> = database.playerDao().admins()

    /**
     *  获取登录的用户
     */
    fun admin(uniqueId: String): LiveData<Player> = database.playerDao().query(uniqueId)

    /**
     * 更新用户资料
     */
    fun updateProfile(player: Player?): Completable {
        val params: SortedMap<String, Any> = sortedMapOf()
        player?.let {
            // convert player to map
        }
        return remoteTasks.updateProfile()
                .map { it ->
                }
                .toCompletable()
    }

    /**
     *  获取公共消息的缓存
     */
    @Deprecated("删")
    fun homeTimeline() = database.playerAndStatusDao().query(preferences.getString(UNIQUE_ID).get())

    @Deprecated("删")
    fun publicTimeline() = database.statusDao().publicTimeline()

    @Deprecated("删")
    fun fetchTimeline(path: String, since: String?, max: String?): Single<List<Status>> {
        return remoteTasks.fetchTimeline(path, since, max)
                .map(SaveStatusFunc(database, path, preferences.getString(UNIQUE_ID).get()))
                .doOnError {
                    Log.e("fetchTimeline", "保存到数据库", it)
                }
                .subscribeOn(Schedulers.computation())
    }


    fun loadTimelineBefore(path: String, max: String, size: Int): MutableList<Status> {
        // TODO: path.switch()
        val cache = database.statusDao().loadBefore(max, size)
        return if (cache.isNotEmpty()) {
            cache
        } else {
            val prefetch = remoteTasks.fetchTimelineCall(path, null, max)
            prefetch?.let {
                SaveStatusFunc.saveInDatabase(it, database, preferences.getString(UNIQUE_ID).get())
            }
            prefetch ?: mutableListOf()
        }
    }

    fun loadTimelineAfter(path: String, since: String, size: Int): MutableList<Status> {
        // TODO: path.switch()
        val cache = database.statusDao().loadAfter(since, size)
        return if (cache.isNotEmpty()) {
            cache
        } else {
            val prefetch = remoteTasks.fetchTimelineCall(path, since, null)
            prefetch?.let {
                SaveStatusFunc.saveInDatabase(it, database, preferences.getString(UNIQUE_ID).get())
            }
            prefetch ?: mutableListOf()
        }
    }

    fun loadTimelineInitial(path: String, size: Int): MutableList<Status> {
        // TODO: path.switch()
        val cache = database.statusDao().initial(size)
        return if (cache.isNotEmpty()) {
            cache
        } else {
            val prefetch = remoteTasks.fetchTimelineCall(path, null, null)
            prefetch?.let {
                SaveStatusFunc.saveInDatabase(it, database, preferences.getString(UNIQUE_ID).get())
            }
            prefetch ?: mutableListOf()
        }
    }

    /**
     * authorize or deauthorize
     */
    private fun onAuthorize(authorization: Authorization) {
        Log.d("Repository", "<======= Update Authorization ======>")
        interceptor.authenticator(authorization)
    }


}