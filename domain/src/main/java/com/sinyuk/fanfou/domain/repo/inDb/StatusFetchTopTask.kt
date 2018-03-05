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
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_HOME
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
                         private val uniqueId: String) : Runnable {

    companion object {
        const val TAG = "StatusFetchTopTask"
    }

    val livedata = MutableLiveData<Resource<MutableList<Status>>>()

    init {
        livedata.postValue(Resource.loading(null))
    }

    override fun run() {
        val first = db.statusDao().first(convertPathToFlag(path), uniqueId)?.id
        if (first.isNullOrBlank()) {
            livedata.postValue(Resource.success(mutableListOf()))
            return
        }
        try {
            val response = if (path == TIMELINE_HOME) {
                restAPI.fetch_from_path(path = path, count = pageSize, since = first)
            } else {
                restAPI.fetch_from_path(path = path, count = pageSize, since = first, id = uniqueId)
            }.execute()

            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful()) {
                val data = apiResponse.body
                if (insertResultIntoDb(data) > 0) {
                    livedata.postValue(Resource.success(data))
                } else {
                    livedata.postValue(Resource.success(mutableListOf()))
                }
            } else {
                livedata.postValue(Resource.error("error code: ${response.code()}", null))
            }
        } catch (e: IOException) {
            livedata.postValue(Resource.error("error code: ${e.message}", null))
        }
    }


    @WorkerThread
    private fun insertResultIntoDb(body: MutableList<Status>?): Int {
        if (body?.isEmpty() != false) return 0

        for (status in body) {
            status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            db.statusDao().query(status.id, uniqueId)?.let { status.addPath(it.pathFlag) }
            status.addPathFlag(path)
            status.uid = uniqueId
        }

        val size: Int
        try {
            db.beginTransaction()
            size = db.statusDao().inserts(body).size
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return size
    }

}