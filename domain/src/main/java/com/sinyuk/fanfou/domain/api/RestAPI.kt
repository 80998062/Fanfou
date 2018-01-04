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
import com.sinyuk.fanfou.domain.DO.Keyword
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.DO.Trend
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
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

    @GET("saved_searches/list.json")
    fun list_searches(): LiveData<ApiResponse<MutableList<Keyword>>>

    @POST("saved_searches/create.json")
    fun create_search(@Query("query") query: String): Call<Keyword>


    @POST("saved_searches/destroy.json")
    fun delete_search(@Query("id") id: String): Call<Keyword>

    @GET("trends/list.json")
    fun trends(): Call<TrendList>

    data class TrendList constructor(@SerializedName("as_of") var date: Date, @SerializedName("trends") var data: MutableList<Trend>)
}