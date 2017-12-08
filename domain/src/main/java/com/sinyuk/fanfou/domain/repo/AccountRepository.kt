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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.SharedPreferences
import android.util.Log
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.util.AbsentLiveData
import com.sinyuk.fanfou.domain.vo.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/6.
 */
@Singleton
class AccountRepository
@Inject constructor(url: Endpoint,
                    interceptor: Oauth1SigningInterceptor,
                    private val appExecutors: AppExecutors,
                    @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
                    @Named(TYPE_GLOBAL) val prefs: SharedPreferences) : AbstractRepository(url, interceptor) {


    fun uniqueId(): String? = prefs.getString(UNIQUE_ID, null)

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
     * load account
     */
    fun verifyCredentials(forcedUpdate: Boolean = false) = object : NetworkBoundResource<Player, Player>(appExecutors) {
        override fun onFetchFailed() {}

        override fun saveCallResult(item: Player?) {
            item?.let {
                prefs.edit().apply {
                    putString(UNIQUE_ID, it.uniqueId)
                }.apply()
                it.authorization = Authorization(accessToken(), accessSecret())
                db.playerDao().insert(it)
            }
        }

        override fun shouldFetch(data: Player?): Boolean = rateLimiter.shouldFetch(KEY) || forcedUpdate || data == null

        override fun loadFromDb(): LiveData<Player?> = db.playerDao().query(uniqueId())

        override fun createCall(): LiveData<ApiResponse<Player>> = restAPI.verify_credentials()

    }.asLiveData()

    fun timeline(max: String?, forcedUpdate: Boolean = false) =
            object : NetworkBoundResource<MutableList<Status>, MutableList<Status>>(appExecutors) {
                override fun onFetchFailed() {
                    Log.e("timeline", "onFetchFailed")
                }

                override fun saveCallResult(item: MutableList<Status>?) {
                    Log.e("timeline", "saveCallResult ")
                    item?.let { saveStatus(it) }
                }

                override fun shouldFetch(data: MutableList<Status>?) =
                        rateLimiter.shouldFetch(KEY) || forcedUpdate || data == null || data.isEmpty()

                override fun loadFromDb(): LiveData<MutableList<Status>?> = if (max == null) {
                    db.statusDao().initial(PAGE_SIZE)
                } else {
                    Transformations.switchMap(db.statusDao().query(max), {
                        if (it == null) {
                            AbsentLiveData.create()
                        } else {
                            db.statusDao().after(max, PAGE_SIZE)
                        }
                    })
                }

                override fun createCall() = restAPI.fetch_from_path(TIMELINE_HOME, null, max)

            }.asLiveData()

    private fun saveStatus(t: MutableList<Status>) {
        db.beginTransaction()
        try {
            for (status in t) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                Log.d("saveStatus", "insert at " + db.statusDao().insert(status))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}