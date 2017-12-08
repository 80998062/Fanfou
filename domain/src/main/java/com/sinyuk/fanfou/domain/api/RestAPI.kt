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

package com.sinyuk.fanfou.domain.api

import android.arch.lifecycle.LiveData
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.domain.vo.Status
import retrofit2.http.*

/**
 * Created by sinyuk on 2017/11/28.
 */
interface RestAPI {

    @GET("users/show.json?format=html")
    fun user_update(@Query("id") uniqueId: String): LiveData<ApiResponse<Player>>

    @GET("account/verify_credentials.json?format=html")
    fun verify_credentials(): LiveData<ApiResponse<Player>>

    @FormUrlEncoded
    @POST("account/update_profile.json?format=html")
    fun update_profile(@Body player: Player): LiveData<ApiResponse<Player>>


    @GET("statuses/{path}.json?count=10&format=html")
    fun fetch_from_path(@Path("path") path: String, @Query("since_id") since: String?, @Query("max_id") max: String?): LiveData<ApiResponse<MutableList<Status>>>


    @GET("favorites/id.json?count=10&format=html")
    fun fetch_favorites(@Query("id") id: String?, @Query("since_id") since: String?, @Query("max_id") max: String?): LiveData<ApiResponse<MutableList<Status>>>

}