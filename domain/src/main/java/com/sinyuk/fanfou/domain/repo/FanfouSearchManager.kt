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
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.io.IOException
import java.util.*
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


    fun savedSearches(limit: Int? = null) = if (limit == null) {
        db.keywordDao().list()
    } else {
        db.keywordDao().take(limit)
    }

    fun createSearch(query: String) {
        appExecutors.diskIO().execute {
            db.runInTransaction { db.keywordDao().save(Keyword(query = query, createdAt = Date())) }
        }
    }


    fun deleteSearch(query: String) {
        appExecutors.diskIO().execute {
            db.runInTransaction { db.keywordDao().delete(Keyword(query = query)) }
        }
    }

    fun clearSearches(){
        appExecutors.diskIO().execute {
            db.runInTransaction { db.keywordDao().clear() }
        }
    }


}