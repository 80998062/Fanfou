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
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.TIMELINE_MENTIONS
import com.sinyuk.fanfou.domain.TIMELINE_USER
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.db.LocalDatabase
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/13.
 *
 *
 */
class FetchTimelineTask(private val restAPI: RestAPI,
                        private val db: LocalDatabase,
                        private val max: String,
                        private val pageSize: Int) : Runnable {

    val liveData = MutableLiveData<Resource<Boolean>>()

    init {
        liveData.value = Resource.loading(null)
    }

    override fun run() {
        try {
            val response = restAPI.fetch_from_path(TIMELINE_HOME, pageSize, null, max).execute()
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful()) {
                removeBreakChain()
                if (apiResponse.body == null || apiResponse.body.isEmpty()) {
                    liveData.postValue(null)
                } else {
                    val data = apiResponse.body
                    insertResultIntoDb(data)
                    if (data.size < pageSize) {
                        liveData.postValue(Resource.success(true))
                    } else {
                        val newResponse = when (path) {
                            TIMELINE_HOME, TIMELINE_USER, TIMELINE_MENTIONS -> restAPI.fetch_from_path(path = path, count = 1, max = data.last().id)
                            TIMELINE_FAVORITES -> restAPI.fetch_favorites(count = 1, max = data.last().id)
                            else -> TODO("Can't fetch data from path: $path")
                        }.execute()

                        if (newResponse.isSuccessful && newResponse.body()?.isNotEmpty() == true) {
                            val status = newResponse.body()!!.last()
                            try {
                                db.beginTransaction()
                                if (nextItem(status.id) != null && db.statusDao().query(status.id) == null) { // 如果 item 之后有数据 但是 item 不在数据库里 so it's a break point
                                    when (path) {
                                        TIMELINE_HOME -> status.breakInPublic = true
                                        TIMELINE_USER -> status.breakInPost = true
                                        TIMELINE_FAVORITES -> status.breakInFavorites = true
                                        else -> TODO("Can't save data of $path in Disk")
                                    }
                                    status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                                    db.statusDao().insert(status)
                                }
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                                liveData.postValue(Resource.success(true))
                            }
                        }
                    }
                }
            } else {
                liveData.postValue(Resource.error(null, false))
            }
        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, false))
        }
    }

    private fun nextItem(id: String) = when (path) {
        TIMELINE_HOME -> db.statusDao().queryNextHome(id)
        TIMELINE_USER -> db.statusDao().queryNextSelf(id)
        TIMELINE_FAVORITES -> db.statusDao().queryNextFavorites(id)
        else -> TODO("Can't query next item from path: $path")
    }

    private fun removeBreakChain() {
        db.runInTransaction {
            db.statusDao().query(max)?.let {
                when (path) {
                    TIMELINE_HOME -> it.breakInPublic = false
                    TIMELINE_USER -> it.breakInPost = false
                    TIMELINE_FAVORITES -> it.breakInFavorites = false
                    else -> TODO("Can't remove break flag of $path in Disk")
                }
                db.statusDao().update(it)
            }
        }
    }

    @WorkerThread
    private fun insertResultIntoDb(body: MutableList<Status>?) {
        if (body?.isNotEmpty() == true) {
            for (status in body) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }

            }
            db.statusDao().inserts(body)
        }
    }
}