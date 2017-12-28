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
import android.support.annotation.WorkerThread
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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


    /**
     * 加载指定消息之后的状态
     *
     * @param pageSize 一次请求的条数
     * @param max 起始位置
     */
    fun fetchAfter(path: String, max: String, pageSize: Int): MutableLiveData<Resource<Boolean>> {
        val task = TimelineFetchTask(restAPI, db, path, max, pageSize)
        appExecutors.networkIO().execute(task)
        return task.liveData
    }

    /**
     * 加载保存的所有状态,并自动请求新的旧状态
     * @param pageSize 一次请求的条数
     */
    fun timeline(path: String, pageSize: Int): Listing<Status> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = TimelineBoundaryCallback(
                webservice = restAPI,
                path = path,
                handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors,
                networkPageSize = PAGE_SIZE)
        // create a data source factory from Room
        val dataSourceFactory = when (path) {
            TIMELINE_HOME -> STATUS_PUBLIC_FLAG
            TIMELINE_FAVORITES -> STATUS_FAVORITED_FLAG
            TIMELINE_USER -> STATUS_POST_FLAG
            else -> TODO()
        }.let {
            provideDataSourceFactory(it)
        }
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize).setBoundaryCallback(boundaryCallback)
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            refresh(path, pageSize)
        })

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.helper.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }


    @WorkerThread
    private fun fetchNextItem(networkState: MutableLiveData<NetworkState>, path: String, max: String) {
        when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(count = 1, max = max)
            else -> restAPI.fetch_from_path(path = path, count = 1, max = max)
        }
                .enqueue(object : Callback<MutableList<Status>> {
                    override fun onFailure(call: Call<MutableList<Status>>?, t: Throwable?) {
                        networkState.value = NetworkState.error(t?.message)
                    }

                    override fun onResponse(call: Call<MutableList<Status>>?, response: Response<MutableList<Status>>?) {
                        if (response?.body()?.isNotEmpty() == true) {
                            response.body()!!.last().let { appExecutors.diskIO().execute { saveBreakChain(path, it) } }
                        }
                        networkState.value = NetworkState.LOADED
                    }
                })
    }

    @WorkerThread
    private fun saveBreakChain(path: String, status: Status) {
        try {
            db.beginTransaction()
            val flag = when (path) {
                TIMELINE_HOME -> STATUS_PUBLIC_FLAG
                TIMELINE_FAVORITES -> STATUS_FAVORITED_FLAG
                TIMELINE_USER -> STATUS_POST_FLAG
                else -> TODO()
            }
            if (db.statusDao().query(id = status.id, path = flag) == null) {
                status.addPathFlag(path)
                status.addBreakFlag(path)
                db.statusDao().insert(status)
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    @WorkerThread
    private fun insertResultIntoDb(path: String, body: MutableList<Status>?) {
        if (body?.isNotEmpty() == true) {
            for (status in body) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                status.addPathFlag(path)
            }
            db.statusDao().inserts(body)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun provideDataSourceFactory(path: Int) = db.statusDao().timeline(path)


    /**
     * 加载最新的状态
     *
     * @param pageSize 一次请求的条数
     */
    private fun refresh(path: String, pageSize: Int): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(count = pageSize)
            else -> restAPI.fetch_from_path(path = path, count = pageSize)
        }.enqueue(object : Callback<MutableList<Status>?> {
            override fun onFailure(call: Call<MutableList<Status>?>, t: Throwable) {
                // retrofit calls this on main thread so safe to call set value
                networkState.value = NetworkState.error(t.message)
            }

            override fun onResponse(
                    call: Call<MutableList<Status>?>,
                    response: Response<MutableList<Status>?>) {
                appExecutors.diskIO().execute { db.runInTransaction { insertResultIntoDb(path, response.body()) } }
                if (response.body()?.size != PAGE_SIZE) { // since we are in bg thread now, post the result.
                    networkState.value = NetworkState.LOADED
                } else {
                    fetchNextItem(networkState, path, response.body()!!.last().id)
                }
            }
        })
        return networkState
    }
}