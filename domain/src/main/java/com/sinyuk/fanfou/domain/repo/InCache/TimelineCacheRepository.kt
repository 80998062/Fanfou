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

package com.sinyuk.fanfou.domain.repo.InMemory

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.repo.InCache.TimelineCacheTask
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/20.
 *
 */
@Singleton
class TimelineCacheRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors) : AbstractRepository(application, url, interceptor) {

    fun fetchAfterTop(path: String, uniqueId: String, max: String, page: Int): MutableLiveData<Resource<MutableList<Status>>> {
        val task = TimelineCacheTask(path = path,
                uniqueId = uniqueId,
                webservice = cacheAPI,
                max = max,
                page = page)
        appExecutors.networkIO().execute(task)
        return task.liveData
    }


}