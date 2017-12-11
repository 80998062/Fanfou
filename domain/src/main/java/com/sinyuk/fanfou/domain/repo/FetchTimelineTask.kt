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
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.vo.Resource
import com.sinyuk.fanfou.domain.vo.Status
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/11.
 */
class FetchTimelineTask constructor(private val db: LocalDatabase, private val fetch: () -> LiveData<ApiResponse<MutableList<Status>>>) : Runnable {

    val liveData: MutableLiveData<Resource<Boolean>> = MutableLiveData()


    override fun run() {
        try {
            liveData.postValue(Resource.loading(false))
            val apiResponse = fetch.invoke().value
            if (apiResponse?.body == null) {
                liveData.postValue(null)
                return
            } else {
                if (apiResponse.isSuccessful()) {
                    try {
                        db.beginTransaction()
                        db.statusDao().inserts(apiResponse.body)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                    liveData.postValue(Resource.success(true))
                } else {
                    liveData.postValue(Resource.error(apiResponse.errorMessage, false))
                }

            }

        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, false))
        }
    }
}