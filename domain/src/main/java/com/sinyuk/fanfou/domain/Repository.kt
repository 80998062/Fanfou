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
import com.sinyuk.fanfou.domain.rest.Authorization
import com.sinyuk.fanfou.domain.rest.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.rest.RemoteTasks
import com.sinyuk.fanfou.domain.room.LocalTasks
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by sinyuk on 2017/11/27.
 */
class Repository constructor(private val remoteTasks: RemoteTasks,
                             private val localTasks: LocalTasks,
                             private val interceptor: Oauth1SigningInterceptor,
                             private val preferences: RxSharedPreferences) {

    init {
        preferences.getString(UNIQUE_ID).asObservable()
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
                return@flatMap remoteTasks.updateProfile(sortedMapOf())
                        .map {
                            preferences.getString(UNIQUE_ID).set(it.uniqueId)
                            it.addFlags(FLAG_ADMIN)
                            localTasks.insertPlayer(it)
                            localTasks.insertRegistration(it.uniqueId, account, password, authorization)
                            it
                        }
            }.toCompletable()


    /**
     *  获取登录信息
     */
    fun registration(uniqueId: String = preferences.getString(UNIQUE_ID).get()): Single<Registration?> =
            Single.fromCallable { localTasks.queryRegistration(uniqueId) }.subscribeOn(Schedulers.io())

    fun deleteRegistration(uniqueId: String) = localTasks.deleteRegistration(uniqueId)

    /**
     *  获取所有用户
     */
    fun admins(): LiveData<List<Player>> = localTasks.queryAdmins()

    /**
     *  获取登录的用户
     */
    fun admin(): LiveData<Player> = localTasks.queryPlayer(preferences.getString(UNIQUE_ID).get())

    /**
     * 更新用户资料
     */
    fun updateProfile(player: Player?): Completable {
        val params: SortedMap<String, Any> = sortedMapOf()
        player?.let {
            // convert player to map
        }
        return remoteTasks.updateProfile(params)
                .map { it ->
                    it.addFlags(FLAG_ADMIN)
                    localTasks.insertPlayer(it)
                }
                .toCompletable()
    }

    /**
     * authorize or deauthorize
     */
    private fun onAuthorize(authorization: Authorization) {
        Log.d("Repository", "更新授权")
        interceptor.authenticator(authorization)
    }


}