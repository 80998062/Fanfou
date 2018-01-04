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
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DO.Keyword
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Trend
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.isOnline
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.io.IOException
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
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    fun trends(): MutableLiveData<Resource<MutableList<Trend>>> {
        val live = MutableLiveData<Resource<MutableList<Trend>>>()
        appExecutors.networkIO().execute {
            try {
                val response = cacheAPI.trends().execute()
                if (response.isSuccessful && response.body() != null) {
                    live.postValue(Resource.success(response.body()!!.data))
                } else {
                    live.postValue(Resource.error("error code: ${response.code()}", null))
                }
            } catch (e: IOException) {
                live.postValue(Resource.error("error msg: ${e.message}", null))
            }
        }
        return live
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
        appExecutors.networkIO().execute {
            try {
                val response = restAPI.create_search(query).execute()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        db.runInTransaction { db.keywordDao().create(response.body()!!) }
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                    }
                } else {
                    networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                }
            } catch (e: IOException) {
                networkState.postValue(NetworkState.error("error msg: ${e.message}"))
            }
        }
        return networkState
    }


    fun deleteSearch(id: String): MutableLiveData<NetworkState> {

        val networkState = MutableLiveData<NetworkState>()
        networkState.postValue(NetworkState.LOADING)
        appExecutors.networkIO().execute {
            try {
                val response = restAPI.delete_search(id).execute()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        db.runInTransaction { db.keywordDao().delete(response.body()!!) }
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                    }
                } else {
                    networkState.postValue(NetworkState.error("error code ${response?.code()}"))
                }
            } catch (e: IOException) {
                networkState.postValue(NetworkState.error("error msg: ${e.message}"))
            }
        }
        return networkState
    }


}