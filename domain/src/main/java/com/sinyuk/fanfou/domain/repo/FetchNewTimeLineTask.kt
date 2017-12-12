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
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.db.LocalDatabase
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/11.
 *
 * A task that reads the result in the database and fetches the new page, if it has one.
 */
class FetchNewTimeLineTask constructor(private val db: LocalDatabase, private val fetch: () -> LiveData<ApiResponse<MutableList<Status>>>) : Runnable {

    val liveData: MutableLiveData<Resource<MutableList<Status>>> = MutableLiveData()

    companion object {
        const val HAS_NEXT = "has_next_sinyuk"
    }

    override fun run() {
        try {
            liveData.postValue(Resource.loading(null))
            val apiResponse = fetch.invoke().value
            if (apiResponse?.body == null) {
                liveData.postValue(null)
                return
            } else {
                if (apiResponse.isSuccessful()) {
                    val hasNext = db.statusDao().query(apiResponse.body.last().id).value == null

                    if (hasNext) {
                        liveData.postValue(Resource.success(apiResponse.body))
                        try {
                            db.beginTransaction()
                            db.statusDao().inserts(apiResponse.body)
                            db.setTransactionSuccessful()
                        } finally {
                            db.endTransaction()
                        }
                    } else {
                        // 没有下一页数据了
                        // 过滤重复数据
                        try {
                            db.beginTransaction()
                            db.setTransactionSuccessful()
                            apiResponse.body.takeWhile { db.statusDao().query(it.id).value != null }.forEach { apiResponse.body.remove(it) }
                            db.statusDao().inserts(apiResponse.body)
                        } finally {
                            db.endTransaction()
                        }
                        liveData.postValue(Resource.error(HAS_NEXT, apiResponse.body))
                    }

                } else {
                    liveData.postValue(Resource.error(apiResponse.errorMessage, null))
                }

            }

        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, null))
        }
    }

}