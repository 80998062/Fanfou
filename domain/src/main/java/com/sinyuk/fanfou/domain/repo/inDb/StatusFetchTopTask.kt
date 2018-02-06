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

import android.arch.lifecycle.MutableLiveData
import android.support.annotation.WorkerThread
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.convertPathToFlag
import com.sinyuk.fanfou.domain.db.LocalDatabase
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/13.
 *
 *
 */
class StatusFetchTopTask(private val restAPI: RestAPI,
                         private val db: LocalDatabase,
                         private val path: String,
                         private val pageSize: Int,
                         private val uniqueId: String,
                         private val since: String? = null) : Runnable {

    val networkState = MutableLiveData<NetworkState>()

    init {
        networkState.postValue(NetworkState.LOADING)
    }

    override fun run() {
        if (since == null) {
            try {
                db.beginTransaction()
                db.statusDao().deleteAll(convertPathToFlag(path), uniqueId)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        try {
            val response = restAPI.fetch_from_path(path = path, count = pageSize, since = since, id = uniqueId).execute()
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful()) {
                val data = apiResponse.body
                if (insertResultIntoDb(data) == pageSize) {
                    networkState.postValue(NetworkState.LOADED)
                } else {
                    networkState.postValue(NetworkState.REACH_TOP)
                }
            } else {
                networkState.postValue(NetworkState.error("error code: ${response.code()}"))
            }
        } catch (e: IOException) {
            val error = NetworkState.error(e.message ?: "unknown error")
            networkState.postValue(error)
        }
    }


    @WorkerThread
    private fun insertResultIntoDb(body: MutableList<Status>?) = if (body?.isNotEmpty() == true) {
        for (status in body) {
            status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            db.statusDao().query(status.id, uniqueId)?.let { status.addPath(it.pathFlag) }
            status.addPathFlag(path)
            status.uid = uniqueId
        }
        db.statusDao().inserts(body).size
    } else {
        0
    }

}