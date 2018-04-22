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

package com.sinyuk.fanfou.domain.repo.timeline

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.SharedPreferences
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 *
 * Notice: you may page from local storage, which itself pages additional data from the network.
 * This is often done to minimize network loads and provide a better low-connectivity experience:
 * the database is used as a cache of data stored in the backend.
 *
 */
@Singleton
class TimelineRepository @Inject constructor(
        application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(TYPE_GLOBAL) private val sharedPreferences: SharedPreferences,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
        @Named(DATABASE_IN_MEMORY) private val memory: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    companion object {
        const val TAG = "TimelineRepository"
    }


    /**
     * 加载保存的所有状态,并自动请求新的旧状态
     *
     * <b>只能用来获取当前登录用户的TL,收藏,发送的消息</b>
     *
     * @param pageSize 一次请求的条数
     */
    @MainThread
    fun statuses(path: String, pageSize: Int, uniqueId: String): Listing<Status> {
        // TODO: What if account has updated?
        val account = sharedPreferences.getString(UNIQUE_ID, null)
        // create a data source factory from Room
        // if we're fetching user's data, loaded it from disk (local storage)
        // otherwise loaded it from memory (network)
        val isCached = when (path) {
            TIMELINE_HOME -> true
            TIMELINE_USER, TIMELINE_PHOTO -> uniqueId == account
            else -> false
        }
        val dataSourceFactory = if (isCached) {
            db.statusDao().loadAll(uniqueId = uniqueId, path = convertPathToFlag(path))
        } else {
            memory.statusDao().loadAll(uniqueId = uniqueId, path = convertPathToFlag(path))
        }

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = StatusBoundaryCallback(
                webservice = restAPI,
                path = path,
                uniqueId = uniqueId,
                handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors,
                networkPageSize = pageSize)

        val config = PagedList.Config.Builder().setPageSize(pageSize).setEnablePlaceholders(true).setPrefetchDistance(PREFETCH_DISTANCE).setInitialLoadSizeHint(pageSize)

        val builder = LivePagedListBuilder(dataSourceFactory, config.build()).setBoundaryCallback(boundaryCallback).setFetchExecutor(appExecutors.diskIO())

        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val trigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(trigger, { fetchTop(path, pageSize, uniqueId) })

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = { boundaryCallback.helper.retryAllFailed() },
                refresh = { trigger.value = null },
                refreshState = refreshState
        )
    }

    private fun fetchTop(path: String, pageSize: Int, uniqueId: String): LiveData<Resource<MutableList<Status>>> {
        if (BuildConfig.DEBUG) Log.d(TAG, "fetchTop() path: $path pageSize: $pageSize uniqueId: $uniqueId")
        val task = StatusFetchTopTask(restAPI = restAPI, path = path, pageSize = pageSize, db = db, uniqueId = uniqueId)
        appExecutors.networkIO().execute(task)
        return task.livedata
    }

    @WorkerThread
    private fun insertResultIntoDb(path: String, uniqueId: String, body: MutableList<Status>?): Int {
        val isSelf = uniqueId == sharedPreferences.getString(UNIQUE_ID, null)
        val dao = if (isSelf) db.statusDao() else memory.statusDao()
        if (body?.isNotEmpty() == true) {
            val count: Int
            try {
                if (isSelf) db.beginTransaction() else memory.beginTransaction()
                for (status in body) {
                    dao.query(status.id, uniqueId)?.let { status.addPath(it.pathFlag) }
                    status.addPathFlag(path)
                    status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                    status.uid = uniqueId
                }
                count = dao.inserts(body).size
                if (isSelf) db.setTransactionSuccessful() else memory.setTransactionSuccessful()
            } finally {
                if (isSelf) db.endTransaction() else memory.endTransaction()
            }
            return count
        } else {
            return 0
        }
    }


    /**
     * @param id status id
     */
    fun createFavorite(id: String): LiveData<Resource<Status>> {
        val liveData = MutableLiveData<Resource<Status>>()
        liveData.postValue(Resource.loading(null))
        appExecutors.networkIO().execute {
            val uniqueId = sharedPreferences.getString(UNIQUE_ID, null)
            try {
                val response = restAPI.createFavorite(id).execute()
                if (response.isSuccessful || response.body() != null) {
                    val data = response.body()!!
                    appExecutors.diskIO().execute {
                        db.runInTransaction {
                            db.statusDao().query(id, uniqueId)?.let {
                                it.addPathFlag(TIMELINE_FAVORITES)
                                it.favorited = true
                                db.statusDao().update(it)
                            }
                        }
                    }
                    liveData.postValue(Resource.success(data))
                } else {
                    liveData.postValue(Resource.error("error code: ${response.code()}", null))
                }
            } catch (e: IOException) {
                liveData.postValue(Resource.error("error msg: ${e.message}", null))
            }

        }
        return liveData
    }

    /**
     * @param id status id
     */
    fun destroyFavorite(id: String): LiveData<Resource<Status>> {
        val liveData = MutableLiveData<Resource<Status>>()
        liveData.postValue(Resource.loading(null))
        appExecutors.networkIO().execute {
            val uniqueId = sharedPreferences.getString(UNIQUE_ID, null)
            try {
                val response = restAPI.deleteFavorite(id).execute()
                if (response.isSuccessful || response.body() != null) {
                    val data = response.body()!!
                    appExecutors.diskIO().execute {
                        db.statusDao().query(id, uniqueId)?.let {
                            it.removePath(convertPathToFlag(TIMELINE_FAVORITES))
                            if (it.pathFlag == NO_FLAG) {
                                db.runInTransaction { db.statusDao().delete(it) }
                            } else {
                                it.favorited = false
                                db.runInTransaction { db.statusDao().update(it) }
                            }
                        }
                    }
                    liveData.postValue(Resource.success(data))
                } else {
                    liveData.postValue(Resource.error("error code: ${response.code()}", null))
                }
            } catch (e: IOException) {
                liveData.postValue(Resource.error("error msg: ${e.message}", null))
            }
        }



        return liveData
    }

    /**
     * 删除某条状态，只能删除自己的
     *
     * @param id status id
     */
    fun delete(id: String): MutableLiveData<Resource<Status>> {
        val liveData = MutableLiveData<Resource<Status>>()
        liveData.postValue(Resource.loading(null))
        appExecutors.networkIO().execute {
            try {
                val response = restAPI.deleteStatus(id).execute()
                if (response.isSuccessful || response.body() != null) {
                    val data = response.body()!!
                    appExecutors.diskIO().execute {
                        db.runInTransaction { db.statusDao().delete(data) }
                    }
                    liveData.postValue(Resource.success(data))
                } else {
                    liveData.postValue(Resource.error("error code: ${response.code()}", null))
                }
            } catch (e: IOException) {
                liveData.postValue(Resource.error("error msg: ${e.message}", null))
            }
        }
        return liveData
    }
}