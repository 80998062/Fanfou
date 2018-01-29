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

package com.sinyuk.fanfou.domain.repo.inDb

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.support.annotation.WorkerThread
import android.util.Log
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
@Singleton
class TimelineRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    companion object {
        const val TAG = "TimelineRepository"
    }

    /**
     * 加载保存的所有状态,并自动请求新的旧状态
     * @param pageSize 一次请求的条数
     */
    fun statuses(path: String, uniqueId: String? = null, pageSize: Int): Listing<Status> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = StatusBoundaryCallback(
                webservice = restAPI,
                path = path,
                uniqueId = uniqueId,
                handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors,
                networkPageSize = PAGE_SIZE)

        val config = PagedList.Config.Builder().setPageSize(pageSize).setEnablePlaceholders(false).setPrefetchDistance(10).setInitialLoadSizeHint(pageSize)

        // create a data source factory from Room
        val dataSourceFactory = StatusDataSourceFactory(db.statusDao(), convertPathToFlag(path))

        dataSourceFactory.create().addInvalidatedCallback {
            Log.i(TAG, "invalidated")
        }
        val builder = LivePagedListBuilder(dataSourceFactory, config.build()).setBoundaryCallback(boundaryCallback)
                .setInitialLoadKey(null)
                .setBackgroundThreadExecutor(appExecutors.diskIO())


        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, { refresh(path, uniqueId, pageSize) })

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = { boundaryCallback.helper.retryAllFailed() },
                refresh = { refreshTrigger.value = null },
                refreshState = refreshState
        )
    }

    private fun refresh(path: String, uniqueId: String?, pageSize: Int): LiveData<NetworkState> {
        val task = StatusFetchTopTask(restAPI = restAPI, path = path, uniqueId = uniqueId, pageSize = pageSize, db = db)
        appExecutors.networkIO().execute(task)
        return task.networkState
    }

    @WorkerThread
    private fun insertResultIntoDb(path: String, uniqueId: String?, body: MutableList<Status>?) = if (body?.isNotEmpty() == true) {
        for (status in body) {
            status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            db.statusDao().query(status.id)?.let { status.addPath(it.pathFlag) }
            status.addPathFlag(path)
        }
        db.statusDao().inserts(body).size
    } else {
        0
    }


}