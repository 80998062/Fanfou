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
import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.entities.User
import com.sinyuk.fanfou.domain.rest.*
import com.sinyuk.fanfou.domain.room.LocalTasks
import io.reactivex.Completable
import io.reactivex.Single
import java.io.IOException
import java.util.*

/**
 * Created by sinyuk on 2017/11/27.
 */
class Repository constructor(private val remoteTasks: RemoteTasks,
                             private val localTasks: LocalTasks,
                             private val interceptor: Oauth1SigningInterceptor,
                             private val preferences: RxSharedPreferences) {

    init {

    }


    fun signIn(account: String, password: String): Completable = remoteTasks.requestToken(account, password)
            .flatMap { authorization ->
                onAuthorize(authorization)
                return@flatMap remoteTasks.updateProfile(sortedMapOf())
                        .map {
                            preferences.getString(UNIQUE_ID).set(it.uniqueId)
                            localTasks.saveAccount(it, account, authorization, Date(System.currentTimeMillis()))
                            it
                        }
            }.toCompletable()


    fun currentAccount(): LiveData<User> {
        return localTasks.queryAccount(preferences.getString(UNIQUE_ID).get())
    }

    fun allAccounts(): LiveData<List<User>> = localTasks.allAccounts()


    private fun onAuthorize(authorization: Authorization) {
        interceptor.authenticator(authorization)
    }

    private fun onDeauthorize() {
        interceptor.authenticator(null)
    }


}