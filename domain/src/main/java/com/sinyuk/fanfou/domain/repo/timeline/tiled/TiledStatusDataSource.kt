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

package com.sinyuk.fanfou.domain.repo.timeline.tiled

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.TIMELINE_CONTEXT
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.api.RestAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/29.
 *
 * 由于statuses.context_timeline / favorites 接口不支持since_id/max_id的查询,所以只能只用分页来查询
 *
 */
/**
 * @param uniqueId 可以是用户id或者是msg id
 */
class TiledStatusDataSource(private val restAPI: RestAPI,
                            private val path: String,
                            private val uniqueId: String,
                            private val appExecutors: AppExecutors) : PageKeyedDataSource<Int, Status>() {


    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Status>) {}

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Status>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(Resource.loading(null))
        loadInitialFromNetwork(params, callback)
    }

//    private fun loadInitialFromLocal(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Status>) {
//        appExecutors.diskIO().execute {
//            localDatabase.runInTransaction {
//                val statuses = localDatabase.statusDao().loadInitial(uniqueId = uniqueId, path = convertPathToFlag(path), limit = params.requestedLoadSize)
//                callback.onResult(statuses, null, null)
//            }
//        }
//    }

    private fun loadInitialFromNetwork(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Status>) {
        when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.requestedLoadSize, page = 1)
            TIMELINE_CONTEXT -> restAPI.fetch_from_path(path = TIMELINE_CONTEXT, count = params.requestedLoadSize, page = 1, id = uniqueId)
            else -> TODO()
        }.enqueue(object : Callback<MutableList<Status>> {
            override fun onResponse(call: Call<MutableList<Status>>?, response: Response<MutableList<Status>>) {
                if (response.isSuccessful) {
                    val items = mapResponse(response.body())
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(Resource.success(items))
                    val nextPageKey = if (items.size < params.requestedLoadSize) {
                        null
                    } else {
                        2
                    }
                    callback.onResult(items, null, nextPageKey)
                } else {
                    retry = {
                        loadInitial(params, callback)
                    }

                    val msg = "error code: ${response.code()}"
                    networkState.postValue(NetworkState.error(msg))
                    initialLoad.postValue(Resource.error(msg, null))
                }
            }

            override fun onFailure(call: Call<MutableList<Status>>?, t: Throwable) {
                // keep a lambda for future retry
                retry = {
                    loadInitial(params, callback)
                }
                // publish the error
                val msg = t.message ?: "unknown error"
                networkState.postValue(NetworkState.error(msg))
                initialLoad.postValue(Resource.error(msg, null))
            }

        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Status>) {
        networkState.postValue(NetworkState.LOADING)
        loadAfterFromNetwork(params, callback)

    }

    @Suppress("unused")
    private fun loadAfterFromLocal(params: LoadParams<Int>, callback: LoadCallback<Int, Status>) {
//        appExecutors.diskIO().execute {
//            localDatabase.runInTransaction {
//                val statuses = localDatabase.statusDao().loadAfter(uniqueId = uniqueId, path = convertPathToFlag(path), limit = params.requestedLoadSize,
//                        offset = params.requestedLoadSize * params.key)
//                val next = params.key + 1
//                callback.onResult(statuses, next)
//            }
//        }
    }

    private fun loadAfterFromNetwork(params: LoadParams<Int>, callback: LoadCallback<Int, Status>) {
        try {
            val response = when (path) {
                TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.requestedLoadSize, page = params.key)
                TIMELINE_CONTEXT -> restAPI.fetch_from_path(path = TIMELINE_CONTEXT, count = params.requestedLoadSize, page = params.key, id = uniqueId)
                else -> TODO()
            }.execute()

            if (response.isSuccessful) {
                val items = mapResponse(response.body())
                retry = null
                networkState.postValue(NetworkState.LOADED)
                val nextPageKey = if (items.size < params.requestedLoadSize) {
                    null
                } else {
                    params.key + 1
                }
                callback.onResult(items, nextPageKey)
            } else {
                retry = { loadAfter(params, callback) }
                networkState.postValue(NetworkState.error("error code: ${response.code()}"))
            }


        } catch (e: IOException) {
            retry = { loadAfter(params, callback) }
            networkState.postValue(NetworkState.error(e.message ?: "unknown error"))
        }
    }


    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     * <p>
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
     */
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<Resource<MutableList<Status>>>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            appExecutors.networkIO().execute {
                it.invoke()
            }
        }
    }

    fun mapResponse(response: MutableList<Status>?) = if (response == null) {
        mutableListOf()
    } else {
        for (item in response) {
            item.user?.let { item.playerExtracts = PlayerExtracts(it) }
        }
        response
    }


}