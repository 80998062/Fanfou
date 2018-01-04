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
import android.arch.lifecycle.MutableLiveData
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.Keyword
import com.sinyuk.fanfou.domain.DO.Trend
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
@Singleton
class FanfouSearchManager @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
        @Named(DATABASE_IN_MEMORY) private val memory: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    fun trends(forcedUpdate: Boolean = false) = object : NetworkBoundResource<MutableList<Trend>, MutableList<Trend>>(appExecutors) {
        override fun onFetchFailed() {
        }

        override fun saveCallResult(item: MutableList<Trend>?) {
            item?.let { memory.runInTransaction { memory.trendDao().save(item) } }
        }

        override fun shouldFetch(data: MutableList<Trend>?) = isOnline(application) && (forcedUpdate || data == null)

        override fun loadFromDb() = memory.trendDao().list()

        override fun createCall() = cacheAPI.trends()
    }

    fun savedSearches(forcedUpdate: Boolean = false) = object : NetworkBoundResource<MutableList<Keyword>, MutableList<Keyword>>(appExecutors) {
        override fun onFetchFailed() {}

        override fun saveCallResult(item: MutableList<Keyword>?) {
            item?.let { db.runInTransaction { db.keywordDao().save(item) } }
        }

        override fun shouldFetch(data: MutableList<Keyword>?) = isOnline(application) && (forcedUpdate || data == null)

        override fun loadFromDb() = db.keywordDao().list()


        override fun createCall() = restAPI.list_searches()
    }

    fun createSearch(query: String): MutableLiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.postValue(NetworkState.LOADING)
        restAPI.create_search(query).enqueue(object : Callback<Keyword> {
            override fun onResponse(call: Call<Keyword>?, response: Response<Keyword>?) {
                if (response?.isSuccessful == true) {
                    response.body()?.let {
                        try {
                            db.beginTransaction()
                            db.keywordDao().create(it)
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                } else {
                    networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                }
            }

            override fun onFailure(call: Call<Keyword>?, t: Throwable?) {
                networkState.postValue(NetworkState.error("error msg: ${t?.message}"))
            }
        })
        return networkState
    }


    fun deleteSearch(id: String): MutableLiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.postValue(NetworkState.LOADING)
        restAPI.delete_search(id).enqueue(object : Callback<Keyword> {
            override fun onResponse(call: Call<Keyword>?, response: Response<Keyword>?) {
                if (response?.isSuccessful == true) {
                    response.body()?.let {
                        try {
                            db.beginTransaction()
                            db.keywordDao().delete(it)
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                } else {
                    networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                }
            }

            override fun onFailure(call: Call<Keyword>?, t: Throwable?) {
                networkState.postValue(NetworkState.error("error msg: ${t?.message}"))
            }
        })
        return networkState
    }


}