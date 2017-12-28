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

package com.sinyuk.fanfou.domain.repo.InMemory

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/20.
 *
 */
@Singleton
class TimelineCacheRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor) : AbstractRepository(application, url, interceptor) {

    fun fetchAfterTop(path: String, uniqueId: String, max: String? = null, pageSize: Int): MutableLiveData<Resource<MutableList<Status>>> {
        val live = MutableLiveData<Resource<MutableList<Status>>>()
        live.value = Resource.loading(null)
        when (path) {
            TIMELINE_FAVORITES -> cacheAPI.fetch_favorites(id = uniqueId, max = max, count = pageSize)
            else -> cacheAPI.fetch_from_path(path = path, id = uniqueId, max = max, count = pageSize)
        }.enqueue(object : Callback<MutableList<Status>> {
            override fun onResponse(call: Call<MutableList<Status>>?, response: Response<MutableList<Status>>?) {
                if (response?.body() == null) {
                    live.value = Resource.error(null, null)
                } else {
                    live.value = Resource.success(response.body())
                }
            }

            override fun onFailure(call: Call<MutableList<Status>>?, t: Throwable?) {
                live.value = Resource.error(t?.message, null)
            }
        })

        return live
    }


}