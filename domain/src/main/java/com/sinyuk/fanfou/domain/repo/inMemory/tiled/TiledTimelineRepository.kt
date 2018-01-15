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

package com.sinyuk.fanfou.domain.repo.inMemory.tiled

import android.app.Application
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.support.annotation.MainThread
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/29.
 *
 */
@Singleton
class TiledTimelineRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors) : AbstractRepository(application, url, interceptor) {
    @MainThread
    fun statuses(path: String, uniqueId: String? = null, query: String? = null, pageSize: Int): Listing<Status> {
        val encode = query?.let { URLEncoder.encode(it, "utf-8") }
        val sourceFactory = TiledStatusDataSourceFactory(restAPI = restAPI, path = path, uniqueId = uniqueId, appExecutors = appExecutors, query = encode)

        val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(false).setPrefetchDistance(pageSize).setInitialLoadSizeHint(pageSize).setPageSize(pageSize).build()

        val pagedList = LivePagedListBuilder(sourceFactory, pagedListConfig).setBackgroundThreadExecutor(appExecutors.networkIO()).build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return Listing(
                pagedList = pagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData, {
                    it.networkState
                }),
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }

}