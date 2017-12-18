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

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.sinyuk.fanfou.domain.BuildConfig
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

    val liveData = MutableLiveData<Resource<MutableList<Status>>>()

    companion object {
        const val HAS_BREAK_POINT = "has_break_point"
        const val TAG = "FetchTimeline"
    }

    init {
        liveData.value = Resource.loading(null)
    }

    override fun run() {
        try {
            val response = restAPI.fetch_from_path_call(TIMELINE_HOME, null, max).execute()
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful()) {
                if (apiResponse.body == null) {
                    liveData.postValue(null)
                } else {
                    val data = apiResponse.body
                    when {
                        db.statusDao().query(data.first().id).value != null -> {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "全都缓存了")
                            }
                            liveData.postValue(Resource.success(null))
                        }
                        db.statusDao().query(data.last().id).value == null -> // 如果最后一条未缓存 说明中间仍有未缓存的状态 全部删除重来
                            try {
                                if (BuildConfig.DEBUG) {
                                    Log.w(TAG, "除了这个还有未缓存的")
                                }
                                db.beginTransaction()
                                data.add(Status(breakPoint = true, sibling = data.last().id))
                                saveStatus(data)
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                                liveData.postValue(Resource.error(HAS_BREAK_POINT, data))
                            }
                        else -> // 没有下一页数据了 过滤重复数据
                            try {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "除了这个没有未缓存的")
                                }
                                db.beginTransaction()
                                filterDuplicatedStatusesThenSave(data)
                                db.statusDao().inserts(data)
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                                liveData.postValue(Resource.success(data))
                            }
                    }
                }
            } else {
                liveData.postValue(Resource.error(null, null))
            }
        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, null))
        }
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