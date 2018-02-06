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
import android.content.SharedPreferences
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
import com.sinyuk.fanfou.domain.util.stringLiveData
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
        @Named(TYPE_GLOBAL) sharedPreferences: SharedPreferences,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    companion object {
        const val TAG = "TimelineRepository"
    }

    val accountLiveData = sharedPreferences.stringLiveData(UNIQUE_ID, "")

    /**
     * 加载保存的所有状态,并自动请求新的旧状态
     *
     * <b>只能用来获取当前登录用户的TL,收藏,发送的消息</b>
     *
     * @param pageSize 一次请求的条数
     */
    fun statuses(path: String, pageSize: Int, uniqueId: String): Listing<Status> {

        // create a data source factory from Room
        val dataSourceFactory = StatusDataSourceFactory(db.statusDao(), convertPathToFlag(path), uniqueId)

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = StatusBoundaryCallback(webservice = restAPI, path = path, uniqueId = uniqueId, handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors, networkPageSize = pageSize)

        val config = PagedList.Config.Builder().setPageSize(pageSize).setEnablePlaceholders(false).setPrefetchDistance(pageSize).setInitialLoadSizeHint(pageSize)

        val builder = LivePagedListBuilder(dataSourceFactory, config.build()).setBoundaryCallback(boundaryCallback).setBackgroundThreadExecutor(appExecutors.diskIO())


        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, { refresh(path, pageSize, uniqueId) })

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

    private fun refresh(path: String, pageSize: Int, uniqueId: String): LiveData<NetworkState> {
        isInvalid.set(true)
        val task = StatusFetchTopTask(restAPI = restAPI, path = path, pageSize = pageSize, db = db, uniqueId = uniqueId)
        appExecutors.networkIO().execute(task)
        return task.networkState
    }

    fun fetchTop(path: String, pageSize: Int, uniqueId: String): LiveData<NetworkState> {
        val first = db.statusDao().first(convertPathToFlag(path), uniqueId)?.id
        isInvalid.set(true)
        val task = StatusFetchTopTask(restAPI = restAPI, path = path, pageSize = pageSize, db = db, uniqueId = uniqueId, since = first)
        appExecutors.networkIO().execute(task)
        return task.networkState
    }

    private val isInvalid = AtomicBoolean(false)

    @WorkerThread
    private fun insertResultIntoDb(path: String, uniqueId: String, body: MutableList<Status>?): Int {
        if (body?.isNotEmpty() == true) {
            val count: Int
            try {
                db.beginTransaction()
                for (status in body) {
                    db.statusDao().query(status.id, uniqueId)?.let { status.addPath(it.pathFlag) }
                    status.addPathFlag(path)
                    status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                    status.uid = uniqueId
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