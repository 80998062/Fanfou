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

package com.sinyuk.fanfou.domain.repo.InCache

import android.arch.lifecycle.MutableLiveData
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.RestAPI

/**
 * Created by sinyuk on 2017/12/21.
 *
 */
class TimelineCacheTask(private val path: String, val uniqueId: String, val max: String, private val page: Int, private val webservice: RestAPI) : Runnable {

    val liveData = MutableLiveData<Resource<MutableList<Status>>>()

    init {
        liveData.value = Resource.loading(null)
    }

    override fun run() {
        val response = when (path) {
            TIMELINE_FAVORITES -> webservice.fetch_favorites(id = uniqueId, max = max, count = page)
            else -> webservice.fetch_from_path(path = path, id = uniqueId, max = max, count = page)
        }.execute()

        val apiResponse = ApiResponse(response)
        if (apiResponse.isSuccessful()) {
            if (apiResponse.body == null || apiResponse.body.isEmpty()) {
                liveData.postValue(null)
            } else {
                liveData.postValue(Resource.success(apiResponse.body))
            }
        } else {
            liveData.postValue(Resource.error(apiResponse.errorMessage, null))
        }
    }
}