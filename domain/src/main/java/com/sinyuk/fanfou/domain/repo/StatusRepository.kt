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
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DATABASE_IN_MEMORY
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.vo.Status
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
@Singleton
class StatusRepository @Inject constructor(url: Endpoint,
                                           interceptor: Oauth1SigningInterceptor,
                                           private val appExecutors: AppExecutors,
                                           @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
                                           @Named(DATABASE_IN_MEMORY) private val memory: LocalDatabase) : AbstractRepository(url, interceptor) {

    fun loadMyHomeTimline() = object : NetworkBoundResource<MutableList<Status>, MutableList<Status>>(appExecutors) {
        override fun onFetchFailed() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun saveCallResult(item: MutableList<Status>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun shouldFetch(data: MutableList<Status>?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun loadFromDb(): LiveData<MutableList<Status>?> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun createCall(): LiveData<ApiResponse<MutableList<Status>>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}