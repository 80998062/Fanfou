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
        @Named(TYPE_GLOBAL) private val sharedPreferences: SharedPreferences) : AbstractRepository(application, url, interceptor) {


    fun accessToken(): String? = sharedPreferences.getString(ACCESS_TOKEN, null)

    fun accessSecret(): String? = sharedPreferences.getString(ACCESS_SECRET, null)

    /**
     * authorization
     */
    fun authorization(account: String, password: String): MutableLiveData<Resource<Authorization>> {
        val task = SignInTask(account, password, okHttpClient, sharedPreferences)
        appExecutors.diskIO().execute(task)
        return task.liveData
    }

    /**
     * 验证Token
     *
     */
    fun verifyCredentials(authorization: Authorization): LiveData<Resource<Player>> {
        val liveData = MutableLiveData<Resource<Player>>()
        liveData.postValue(Resource.loading(null))
        setTokenAndSecret(authorization.token, authorization.secret)
        appExecutors.networkIO().execute {
            val response = restAPI.fetch_profile().execute()
            if (response.isSuccessful) {
                savePlayerInDb(response.body(), authorization)
                liveData.postValue(Resource.success(response.body()))
            } else {
                setTokenAndSecret(null, null)
                liveData.postValue(Resource.error("error: ${response.message()}", null))
            }
        }
        return liveData
    }


    /**
     * 返回所有登录过的用户
     */
    fun admins() = db.playerDao().admin()


    fun logout() {

    }

    fun userLive(): LiveData<LiveData<Player?>> = map(sharedPreferences.stringLiveData(UNIQUE_ID, ""), {
        if (it.isBlank()) {
            TODO()
        } else {
            db.playerDao().queryAsLive(it)
        }
    })

    fun signIn(uniqueId: String): LiveData<Resource<Player>> {
        val liveData = MutableLiveData<Resource<Player>>()
        liveData.postValue(Resource.loading(null))
        val oldToken = accessToken()
        val oldSecret = accessSecret()
        appExecutors.diskIO().execute {
            if (db.playerDao().query(uniqueId) != null) {
                val player = db.playerDao().query(uniqueId)!!
                setTokenAndSecret(player.authorization?.token, player.authorization?.secret)

                appExecutors.networkIO().execute {
                    val response = restAPI.fetch_profile().execute()
                    if (response.isSuccessful) {
                        savePlayerInDb(response.body(), player.authorization)
                        liveData.postValue(Resource.success(response.body()))
                    } else {
                        setTokenAndSecret(oldToken, oldSecret)
                        liveData.postValue(Resource.error("error: ${response.message()}", null))
                    }
                }
            } else {
                setTokenAndSecret(oldToken, oldSecret)
                liveData.postValue(Resource.error("error: user: $uniqueId is not recorded", null))
            }
        }
        return liveData
    }

    private fun setTokenAndSecret(token: String?, secret: String?) {
        sharedPreferences.edit().putString(ACCESS_TOKEN, token).putString(ACCESS_SECRET, secret).apply()
    }


    private fun savePlayerInDb(item: Player?, authorization: Authorization? = null) {
        item?.let {
            sharedPreferences.edit().apply { putString(UNIQUE_ID, it.uniqueId) }.apply()
            it.authorization = authorization ?: Authorization(accessToken(), accessSecret())
            it.addPathFlag(USERS_ADMIN)
            it.updatedAt = Date(System.currentTimeMillis())
            db.playerDao().insert(it)
        }
    }

    fun delete(uniqueId: String) {
        appExecutors.diskIO().execute { db.playerDao().delete(Player(uniqueId = uniqueId)) }
    }


}