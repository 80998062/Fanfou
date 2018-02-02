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

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.content.SharedPreferences
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.Authorization
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import com.sinyuk.fanfou.domain.util.stringLiveData
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
@Singleton
class AccountRepository
@Inject constructor(
        application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
        @Named(TYPE_GLOBAL) val prefs: SharedPreferences) : AbstractRepository(application, url, interceptor) {


    private fun uniqueId(): String? = prefs.getString(UNIQUE_ID, null)

    fun accessToken(): String? = prefs.getString(ACCESS_TOKEN, null)

    fun accessSecret(): String? = prefs.getString(ACCESS_SECRET, null)

    /**
     * sigin in
     */
    fun sign(account: String, password: String): MutableLiveData<Resource<Authorization>> {
        val task = SignInTask(account, password, okHttpClient, prefs)
        appExecutors.diskIO().execute(task)
        return task.liveData
    }

    /**
     * 返回所有登录过的用户
     */
    fun admins() = db.playerDao().admin()


    fun clearAllStatuses() {

    }

    fun logout() {

    }

    fun userLive(): LiveData<Player?> = map(prefs.stringLiveData(UNIQUE_ID, ""), {
        if (it.isBlank()) {
            null
        } else {
            db.playerDao().query(uniqueId())
        }
    })

//    fun switchTo(uniqueId: String): LiveData<Resource<Player>> {
//        if (db.playerDao().query(uniqueId) != null) {
//            val player = db.playerDao().query(uniqueId)!!
//            val oldToken = accessToken()
//            val oldSecret = accessSecret()
//            prefs.edit().putString(ACCESS_TOKEN, player.authorization?.token).apply()
//            prefs.edit().putString(ACCESS_SECRET, player.authorization?.secret).apply()
//            object : NetworkBoundResource<Player, Player>(appExecutors) {
//                override fun onFetchFailed() {
//                    prefs.edit().putString(ACCESS_TOKEN, oldToken).apply()
//                    prefs.edit().putString(ACCESS_SECRET, oldSecret).apply()
//                }
//
//                override fun saveCallResult(item: Player?) {
//                    item?.let {
//                        prefs.edit().apply { putString(UNIQUE_ID, it.uniqueId) }.apply()
//                        it.updatedAt = Date(System.currentTimeMillis())
//                        db.playerDao().insert(it)
//                    }
//                }
//
//                override fun shouldFetch(data: Player?) = true
//
//                override fun loadFromDb(): LiveData<Player?> = AbsentLiveData.create()
//
//                override fun createCall(): LiveData<ApiResponse<Player>> = restAPI.verify_credentials()
//
//            }.asLiveData()
//        } else {
//            Resource.error()
//        }
//    }

    /**
     * load account
     *
     */
    fun verifyCredentials(forcedUpdate: Boolean = false) = object : NetworkBoundResource<Player, Player>(appExecutors) {
        override fun onFetchFailed() {}

        override fun saveCallResult(item: Player?) {
            savePlayerInDb(item)
        }

        override fun shouldFetch(data: Player?): Boolean = forcedUpdate || data == null

        override fun loadFromDb(): LiveData<Player?> = db.playerDao().queryAsLive(uniqueId())

        override fun createCall(): LiveData<ApiResponse<Player>> = restAPI.verify_credentials()

    }.asLiveData()

    private fun savePlayerInDb(item: Player?) {
        item?.let {
            prefs.edit().apply { putString(UNIQUE_ID, it.uniqueId) }.apply()
            it.authorization = Authorization(accessToken(), accessSecret())
            it.addPathFlag(USERS_ADMIN)
            it.updatedAt = Date(System.currentTimeMillis())
            db.playerDao().insert(it)
        }
    }


}