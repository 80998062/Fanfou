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

import android.arch.paging.PagedList
import android.support.annotation.MainThread
import android.util.Log
import com.android.paging.PagingRequestHelper
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_PHOTO
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class StatusBoundaryCallback(
        private val webservice: RestAPI,
        private val handleResponse: (String, String, MutableList<Status>?) -> Int,
        private val path: String,
        private val appExecutors: AppExecutors,
        private val networkPageSize: Int,
        private val uniqueId: String)
    : PagedList.BoundaryCallback<Status>() {

    companion object {
        const val TAG = "StatusBoundaryCallback"

    }

    val helper = PagingRequestHelper(appExecutors.networkIO())
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        if (BuildConfig.DEBUG) Log.i(TAG, "onZeroItemsLoaded")
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onZeroItemsLoaded: true")
            if (path == TIMELINE_PHOTO) {
                webservice.photos(count = networkPageSize, id = uniqueId)
            } else {
                webservice.fetch_from_path(path = path, count = networkPageSize)
            }.enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Status) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onItemAtEndLoaded: " + itemAtEnd.id)
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onItemAtEndLoaded: true")
            if (path == TIMELINE_PHOTO) {
                webservice.photos(count = networkPageSize, id = uniqueId, max = itemAtEnd.id)
            } else {
                webservice.fetch_from_path(path = path, count = networkPageSize, max = itemAtEnd.id)
            }.enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<MutableList<Status>>,
            it: PagingRequestHelper.Request.Callback) {
        appExecutors.diskIO().execute {
            if (handleResponse(path, uniqueId, response.body()) == networkPageSize) {
                it.recordSuccess()
            } else {
                it.recordNoMore()
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Status) {}

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MutableList<Status>> {
        return object : Callback<MutableList<Status>> {
            override fun onFailure(call: Call<MutableList<Status>>, t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(call: Call<MutableList<Status>>, response: Response<MutableList<Status>>) {
                insertItemsIntoDb(response, it)
            }
        }
    }
}