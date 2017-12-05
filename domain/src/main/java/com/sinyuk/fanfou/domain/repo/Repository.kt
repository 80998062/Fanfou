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

package com.sinyuk.fanfou.domain.repo

import android.util.Log
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.api.Authorization
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.api.RemoteTasks
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.util.SaveInDisk
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.domain.vo.Registration
import com.sinyuk.fanfou.domain.vo.Status
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/27.
 */
class Repository constructor(private val remoteTasks: RemoteTasks,
                             @Named(DATABASE_IN_DISK) val database: LocalDatabase,
                             @Named(DATABASE_IN_MEMORY) val inMemoryDatabase: LocalDatabase,
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
    fun admins() = database.playerDao().admins()

    /**
     *  获取登录的用户
     */
    fun admin(uniqueId: String) = database.playerDao().query(uniqueId)

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

    // 登录用户's主页消息
    fun adminHome() = database.playerAndStatusDao().query(preferences.getString(UNIQUE_ID).get())

    // 登录用户's收藏的消息
    fun adminFavorite() {}

    // 登录用户's被@的消息
    fun adminMetions() {}

    // 登录用户's发送的消息
    fun adminFeed() {}

    // 未登录用户's发送的消息
    fun playerFeed(uniqueId: String) {}

    // 未登录用户's收藏的消息
    fun playerFavorite(uniqueId: String) {}


    fun fetchHomeTimeline(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFromPath(TIMELINE_HOME, since, max)
            .map(SaveInDisk(database, TIMELINE_HOME, preferences.getString(UNIQUE_ID).get()))
            .doOnError { Log.e("fetchHomeTimeline", "保存到数据库", it) }
            .subscribeOn(Schedulers.computation())

    fun fetchFeed(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFromPath(TIMELINE_USER, since, max)
            .map(SaveInDisk(database, TIMELINE_USER, preferences.getString(UNIQUE_ID).get()))
            .doOnError { Log.e("fetchFeed", "保存到数据库", it) }
            .subscribeOn(Schedulers.computation())

    fun fetchMentions(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFromPath(TIMELINE_MENTIONS, since, max)
            .map(SaveInDisk(database, TIMELINE_MENTIONS, preferences.getString(UNIQUE_ID).get()))
            .doOnError { Log.e("fetchMentions", "保存到数据库", it) }
            .subscribeOn(Schedulers.computation())

    fun fetchReplies(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFromPath(TIMELINE_REPLIES, since, max)
            .map(SaveInDisk(database, TIMELINE_REPLIES, preferences.getString(UNIQUE_ID).get()))
            .doOnError { Log.e("fetchReplies", "保存到数据库", it) }
            .subscribeOn(Schedulers.computation())

    fun fetchFavorites(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFavorites(null, since, max)
            .map(SaveInDisk(database, TIMELINE_FAVORITES, preferences.getString(UNIQUE_ID).get()))
            .doOnError { Log.e("fetchFavorites", "保存到数据库", it) }
            .subscribeOn(Schedulers.computation())

//
//    fun fetchPublicTimeline(since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFromPath(TIMELINE_PUBLIC, since, max)
//            .map(SaveInMemory(inMemoryDatabase))
//            .doOnError { Log.e("fetchPublicTimeline", "保存到闪存中", it) }
//            .subscribeOn(Schedulers.computation())
//
//    fun fetchPlayersFavorites(uniqueId: String, since: String?, max: String?): Single<MutableList<Status>> = remoteTasks.fetchFavorites(uniqueId, since, max)
//            .map(SaveInMemory(inMemoryDatabase))
//            .doOnError { Log.e("fetchPlayersFavorites", "保存到闪存中", it) }
//            .subscribeOn(Schedulers.computation())


    /**
     * authorize or deauthorize
     */
    private fun onAuthorize(authorization: Authorization) {
        Log.d("Repository", "<======= Update Authorization ======>")
        interceptor.authenticator(authorization)
    }


}