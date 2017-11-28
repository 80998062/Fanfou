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

import android.support.annotation.NonNull
import com.sinyuk.fanfou.domain.entities.User
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import java.util.*

/**
 * Created by sinyuk on 2017/11/28.
 */
interface RestAPI {

    @GET("users/show.json?mode=lite&format=html")
    fun user_show(@NonNull @QueryMap params: SortedMap<String, Any>): Single<Response<User>>


    @POST("account/update_profile.json?mode=lite&format=html")
    fun update_profile(@NonNull @QueryMap params: SortedMap<String, Any>): Single<Response<User>>
}