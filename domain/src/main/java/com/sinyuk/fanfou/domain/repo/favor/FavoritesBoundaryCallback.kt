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

import android.arch.paging.PagedList
import android.support.annotation.MainThread
import com.android.paging.PagingRequestHelper
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Favorite
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.util.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by sinyuk on 2017/12/21.
 *
 */
class FavoritesBoundaryCallback(
        private val webservice: RestAPI,
        private val uniqueId: String,
        private val handleResponse: (String, MutableList<Favorite>?) -> Unit,
        private val appExecutors: AppExecutors,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Favorite>() {

     val helper = PagingRequestHelper(appExecutors.networkIO())
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.fetch_favorites(id = uniqueId, count = networkPageSize).enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Favorite) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.fetch_favorites(id = uniqueId, count = networkPageSize, max = itemAtEnd.id).enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<MutableList<Favorite>>,
            it: PagingRequestHelper.Request.Callback) {
        appExecutors.diskIO().execute {
            handleResponse(uniqueId, response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Favorite) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MutableList<Favorite>> {
        return object : Callback<MutableList<Favorite>> {
            override fun onFailure(
                    call: Call<MutableList<Favorite>>,
                    t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                    call: Call<MutableList<Favorite>>,
                    response: Response<MutableList<Favorite>>) {
                insertItemsIntoDb(response, it)
            }
        }
    }

}