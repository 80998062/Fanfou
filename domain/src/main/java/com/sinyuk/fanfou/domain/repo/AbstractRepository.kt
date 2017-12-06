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

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.util.LiveDataCallAdapterFactory
import com.sinyuk.fanfou.domain.util.RateLimiter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by sinyuk on 2017/11/28.
 */
abstract class AbstractRepository constructor(endpoint: Endpoint, interceptor: Oauth1SigningInterceptor) {

    private val MAX_HTTP_CACHE = (1024 * 1024 * 100).toLong()
    private val TIMEOUT: Long = 10
    protected var okHttpClient: OkHttpClient
    private val gson: Gson = GsonBuilder()
            // Blank fields are included as null instead of being omitted.
            .serializeNulls()
            // convert timestamp to date
            .setDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy")
            .create()
    protected val restAPI: RestAPI

    protected val rateLimiter = RateLimiter<String>(30, TimeUnit.MINUTES)

    protected val KEY = "for_all_request"

    init {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .addInterceptor(logging)
                .addNetworkInterceptor(StethoInterceptor())
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()


        restAPI = Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(RestAPI::class.java)
    }
}