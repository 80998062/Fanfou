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

package com.sinyuk.fanfou.domain.rest

import com.sinyuk.fanfou.domain.entities.Player
import com.sinyuk.fanfou.domain.entities.Status
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by sinyuk on 2017/11/28.
 */
interface RestAPI {

    @GET("users/show.json?format=html")
    fun user_show(@Query("id") uniqueId: String): Single<Response<Player>>


    @POST("account/update_profile.json")
    fun update_profile(): Single<Response<Player>>


    @GET("statuses/{path}.json?count=60&format=html")
    fun fetch_statuses(@Path("path") path: String, @Query("since_id") since: String?, @Query("max_id") max: String?): Single<Response<List<Status>>>


    @GET("statuses/{path}.json?count=60&format=html")
    fun fetch_statuses_call(@Path("path") path: String, @Query("since_id") since: String?, @Query("max_id") max: String?): Call<MutableList<Status>?>

}