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

package com.sinyuk.fanfou.domain.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_HOME
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
                        private val path: String,
                        private val max: String? = null) : Runnable {

    val liveData: MutableLiveData<Resource<MutableList<Status>>> = MutableLiveData()

    companion object {
        const val HAS_BREAK_POINT = "has_break_point"
    }

    override fun run() {
        try {
            liveData.postValue(Resource.loading(null))
            val apiResponse = createCall().value
            if (apiResponse?.body == null) {
                liveData.postValue(null)
                return
            } else {
                if (apiResponse.isSuccessful()) {
                    if (apiResponse?.body.isEmpty() ||
                            db.statusDao().query(apiResponse.body.first().id).value != null) {
                        // 如果连第一条都缓存了 quick return
                        liveData.postValue(Resource.success(null))
                        return
                    }

                    // 如果最后一条未缓存 说明中间仍有未缓存的状态
                    val broken = db.statusDao().query(apiResponse.body.last().id).value == null
                    if (broken) {
                        try {
                            db.beginTransaction()
                            saveStatus(apiResponse.body)
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                            liveData.postValue(Resource.error(HAS_BREAK_POINT, apiResponse.body))
                        }
                    } else {
                        // 没有下一页数据了 过滤重复数据
                        try {
                            db.beginTransaction()
                            db.setTransactionSuccessful()
                            filterDuplicatedStatusesThenSave(apiResponse.body)
                            db.statusDao().inserts(apiResponse.body)
                        } finally {
                            db.endTransaction()
                            liveData.postValue(Resource.success(apiResponse.body))
                        }
                    }
                } else {
                    liveData.postValue(Resource.error(apiResponse.errorMessage, null))
                }

            }

        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, null))
        }
    }

    private fun createCall(): LiveData<ApiResponse<MutableList<Status>>> = when (path) {
        TIMELINE_HOME -> restAPI.fetch_from_path(TIMELINE_HOME, null, max)
        else -> restAPI.fetch_from_path(TIMELINE_HOME, null, max)
    }

    /**
     * 过滤已经保存的
     */
    private fun filterDuplicatedStatusesThenSave(t: MutableList<Status>) {
        t.takeWhile { db.statusDao().query(it.id).value != null }.forEach { t.remove(it) }
        saveStatus(t)
    }

    private fun saveStatus(t: MutableList<Status>) {
        for (status in t) {
            status.user?.let { status.playerExtracts = PlayerExtracts(it) }
            Log.d("saveStatus", "in disk: " + db.statusDao().insert(status))
        }
    }
}