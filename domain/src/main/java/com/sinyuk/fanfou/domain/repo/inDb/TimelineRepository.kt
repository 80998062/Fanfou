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
import android.arch.persistence.room.InvalidationTracker
import android.support.annotation.WorkerThread
import android.util.Log
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.convertPathToFlag
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.util.concurrent.atomic.AtomicBoolean
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

        // create a data source factory from Room
        val dataSourceFactory = StatusDataSourceFactory(db.statusDao(), convertPathToFlag(path))

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = StatusBoundaryCallback(webservice = restAPI, path = path, uniqueId = uniqueId, handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors, networkPageSize = pageSize)

        val config = PagedList.Config.Builder().setPageSize(pageSize).setEnablePlaceholders(true).setPrefetchDistance(10).setInitialLoadSizeHint(pageSize)

        val builder = LivePagedListBuilder(dataSourceFactory, config.build()).setBoundaryCallback(boundaryCallback).setInitialLoadKey(null)
                .setBackgroundThreadExecutor(appExecutors.diskIO())


        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, { refresh(path, uniqueId, pageSize) })

        val pagedList = builder.build()

        db.invalidationTracker.addObserver(object : InvalidationTracker.Observer("statuses") {
            override fun onInvalidated(tables: MutableSet<String>) {
                if (isInvalid.compareAndSet(true, false)) {
                    Log.i(TAG, "onInvalidated: " + tables.first())
                    dataSourceFactory.sourceLiveData.value?.invalidate()
                }
            }
        })

        return Listing(
                pagedList = pagedList,
                networkState = boundaryCallback.networkState,
                retry = { boundaryCallback.helper.retryAllFailed() },
                refresh = { refreshTrigger.value = null },
                refreshState = refreshState
        )
    }

    private fun refresh(path: String, uniqueId: String?, pageSize: Int): LiveData<NetworkState> {
        isInvalid.set(true)
        val task = StatusFetchTopTask(restAPI = restAPI, path = path, uniqueId = uniqueId, pageSize = 1, db = db)
        appExecutors.networkIO().execute(task)
        return task.networkState
    }

    private val isInvalid = AtomicBoolean(false)

    @WorkerThread
    private fun insertResultIntoDb(path: String, uniqueId: String?, body: MutableList<Status>?): Int {
        if (body?.isNotEmpty() == true) {
            val count: Int
            try {
                db.beginTransaction()
                for (status in body) {
                    status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                    db.statusDao().query(status.id)?.let { status.addPath(it.pathFlag) }
                    status.addPathFlag(path)
                }
                count = db.statusDao().inserts(body).size
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
                isInvalid.set(true)
            }
            return count
        } else {
            return 0
        }
    }
}