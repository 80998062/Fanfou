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

package com.sinyuk.fanfou.domain.repo.favor

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.Favorite
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
 * Created by sinyuk on 2017/12/21.
 *
 */
@Singleton
class FavoritesRepo @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
        @Named(TYPE_GLOBAL) private val preferences: SharedPreferences) : AbstractRepository(application, url, interceptor) {


    /**
     * 加载最新的状态
     *
     * @param pageSize 一次请求的条数
     */
    fun refresh(pageSize: Int, id: String?): LiveData<NetworkState> {
        val uniqueId = id ?: preferences.getString(UNIQUE_ID, null)
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        restAPI.fetch_favorites(id = uniqueId, count = pageSize).enqueue(object : Callback<MutableList<Favorite>?> {
            override fun onFailure(call: Call<MutableList<Favorite>?>, t: Throwable) {
                // retrofit calls this on main thread so safe to call set value
                networkState.value = NetworkState.error(t.message)
            }

            override fun onResponse(
                    call: Call<MutableList<Favorite>?>,
                    response: Response<MutableList<Favorite>?>) {
                appExecutors.diskIO().execute { db.runInTransaction { insertResultIntoDb(uniqueId, response.body()) } }
                if (response.body()?.size != PAGE_SIZE) { // since we are in bg thread now, post the result.
                    networkState.value = NetworkState.LOADED
                } else {
                    fetchNextItem(networkState, response.body()!!.last().id)
                }
            }
        })
        return networkState
    }


    /**
     * 加载指定消息之后的状态
     *
     * @param pageSize 一次请求的条数
     * @param max 起始位置
     */
    fun fetchAfter(id: String?, max: String, pageSize: Int): MutableLiveData<Resource<Boolean>> {
        val uniqueId = id ?: preferences.getString(UNIQUE_ID, null)
        val task = FavoritesFetchTask(restAPI, uniqueId, db, max, pageSize)
        appExecutors.networkIO().execute(task)
        return task.liveData
    }

    /**
     * 加载保存的所有状态,并自动请求新的旧状态
     * @param pageSize 一次请求的条数
     */
    fun favorites(pageSize: Int, id: String?): Listing<Favorite> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val uniqueId = id ?: preferences.getString(UNIQUE_ID, null)

        val boundaryCallback = FavoritesBoundaryCallback(
                webservice = restAPI,
                uniqueId = uniqueId,
                handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors,
                networkPageSize = PAGE_SIZE)
        // create a data source factory from Room
        val dataSourceFactory = provideDataSourceFactory(uniqueId)
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize).setBoundaryCallback(boundaryCallback)
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            refresh(pageSize, uniqueId)
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
    private fun fetchNextItem(networkState: MutableLiveData<NetworkState>, max: String) {
        restAPI.fetch_from_path(path = TIMELINE_HOME, count = 1, max = max).enqueue(object : Callback<MutableList<Status>> {
            override fun onFailure(call: Call<MutableList<Status>>?, t: Throwable?) {
                networkState.value = NetworkState.error(t?.message)
            }

            override fun onResponse(call: Call<MutableList<Status>>?, response: Response<MutableList<Status>>?) {
                if (response?.body()?.isNotEmpty() == true) {
                    response.body()!!.last().let { appExecutors.diskIO().execute { saveBreakChain(it) } }
                }
                networkState.value = NetworkState.LOADED
            }
        })
    }

    @WorkerThread
    private fun saveBreakChain(status: Status) {
        try {
            db.beginTransaction()
            if (db.statusDao().query(status.id) == null) {
                status.breakChain = true
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                db.statusDao().insert(status)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    @WorkerThread
    private fun insertResultIntoDb(uniqueId: String?, body: MutableList<Favorite>?) {
        if (body?.isNotEmpty() == true) {
            for (status in body) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            }
            db.favoriteDao().inserts(body)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun provideDataSourceFactory(uniqueId: String) = db.favoriteDao().favorites(uniqueId)
}