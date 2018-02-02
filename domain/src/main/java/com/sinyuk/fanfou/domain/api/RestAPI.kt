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
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.DO.Trend
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.*

/**
 * Created by sinyuk on 2017/11/28.
 *
 */
@Suppress("FunctionName")
interface RestAPI {

    @GET("users/show.json?format=html")
    fun show_user(@Query("id") uniqueId: String): LiveData<ApiResponse<Player>>

    @GET("account/verify_credentials.json?format=html")
    fun verify_credentials(): LiveData<ApiResponse<Player>>

    @GET("account/verify_credentials.json?format=html")
    fun fetch_profile(): Call<Player>

    @GET("statuses/{path}.json?format=html")
    fun fetch_from_path(@Path("path") path: String,
                        @Query("count") count: Int,
                        @Query("since_id") since: String? = null,
                        @Query("max_id") max: String? = null,
                        @Query("id") id: String? = null,
                        @Query("page") page: Int? = null): Call<MutableList<Status>>


    @GET("favorites/id.json?format=html")
    fun fetch_favorites(@Query("id") id: String? = null,
                        @Query("count") count: Int,
                        @Query("page") page: Int): Call<MutableList<Status>>


//    @GET("/search/public_timeline.json?format=html")
//    fun search_statuses(@Query(value = "q", encoded = false) query: String,
//                        @Query("count") count: Int,
//                        @Query("page") page: Int? = null): Call<MutableList<Status>>

    companion object {
        fun buildQueryUrl(query: String, count: Int, page: Int? = null): String = "http://api.fanfou.com/search/public_timeline.json?format=html&q=\"圣诞\"&count=50&page=1"
    }

    @GET
    fun search_statuses(@Url url: String): Call<MutableList<Status>>


    @GET("/search/user_timeline.json?format=html")
    fun search_user_statuses(@Query(value = "q", encoded = false) query: String,
                             @Query("count") count: Int,
                             @Query("id") id: String,
                             @Query("page") page: Int? = null): Call<MutableList<Status>>


    @GET("/search/users.json?format=html")
    fun search_users(@Query(value = "q", encoded = false) query: String,
                     @Query("count") count: Int,
                     @Query("id") id: String,
                     @Query("page") page: Int? = null): Call<PlayerList>

    data class PlayerList @JvmOverloads constructor(@SerializedName("total_number") var total: Int = 0,
                                                    @SerializedName("users") var data: MutableList<Player> = mutableListOf())

    @GET("/users/friends.json?format=html")
    fun fetch_friends(@Query("id") id: String?,
                      @Query("count") count: Int,
                      @Query("page") page: Int? = null): Call<MutableList<Player>>


    @GET("/users/followers.json?format=html")
    fun fetch_followers(@Query("id") id: String?,
                        @Query("count") count: Int,
                        @Query("page") page: Int? = null): Call<MutableList<Player>>

    @GET("trends/list.json")
    fun trends(): Call<TrendList>

    data class TrendList @JvmOverloads constructor(@SerializedName("as_of") var date: Date? = null,
                                                   @SerializedName("trends") var data: MutableList<Trend> = mutableListOf())
}