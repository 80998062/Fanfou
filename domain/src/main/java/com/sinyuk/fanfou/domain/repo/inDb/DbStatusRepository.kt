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
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.repo.AbstractRepository
import com.sinyuk.fanfou.domain.repo.Listing
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
class DbStatusRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase) : AbstractRepository(url, interceptor) {


    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(path: String, body: MutableList<Status>?) {
        body?.let {
            db.runInTransaction { db.statusDao().inserts(it) }
        }
    }

    // override
    private fun provideDataSourceFactory(path: String) = db.statusDao().home()

    // override
    fun refresh(path: String): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        restAPI.fetch_from_path_call(path).enqueue(object : Callback<MutableList<Status>?> {
            override fun onFailure(call: Call<MutableList<Status>?>, t: Throwable) {
                // retrofit calls this on main thread so safe to call set value
                networkState.value = NetworkState.error(t.message)
            }

            override fun onResponse(
                    call: Call<MutableList<Status>?>,
                    response: Response<MutableList<Status>?>) {
                appExecutors.diskIO().execute {
                    db.runInTransaction { insertResultIntoDb(path, response.body()) }
                    // since we are in bg thread now, post the result.
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        })
        return networkState
    }


    // override
    fun statusesInPath(path: String, pageSize: Int): Listing<Status> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = StatusBoundaryCallback(
                webservice = restAPI,
                path = path,
                handleResponse = this::insertResultIntoDb,
                appExecutors = appExecutors,
                networkPageSize = PAGE_SIZE)
        // create a data source factory from Room
        // create a data source factory from Room
        val dataSourceFactory = provideDataSourceFactory(path)
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize).setBoundaryCallback(boundaryCallback)
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            refresh(path)
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
}