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

package com.sinyuk.fanfou.domain.repo.base

import android.app.Application
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.*
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.api.CacheInterceptor
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.util.LiveDataCallAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by sinyuk on 2017/11/28.
 *
 */
abstract class AbstractRepository constructor(
        application: Application,
        endpoint: Endpoint,
        interceptor: Oauth1SigningInterceptor) {

    private val MAX_HTTP_CACHE = (1024 * 1024 * 100).toLong()
    private val TIMEOUT: Long = 30
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .also {
                    val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                        Log.d("FANFOU", it)
                    })
                    if (BuildConfig.DEBUG) {
                        logging.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(logging).addNetworkInterceptor(StethoInterceptor())
                    } else {
                        logging.level = HttpLoggingInterceptor.Level.HEADERS
                        it.addInterceptor(logging)
                    }

                }.build()
    }

    private val cacheClient: OkHttpClient by lazy {
        val cache = Cache(File("okhttp"), 1024 * 1024 * 10)
        OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .cache(cache)
                .addNetworkInterceptor(CacheInterceptor(application))
                .retryOnConnectionFailure(false)
                .also {
                    val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                        Log.d("CACHE", it)
                    })
                    if (BuildConfig.DEBUG) {
                        logging.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(logging).addNetworkInterceptor(StethoInterceptor())
                    } else {
                        logging.level = HttpLoggingInterceptor.Level.HEADERS
                        it.addInterceptor(logging)
                    }

                }.build()
    }
    private val gson: Gson = GsonBuilder()
            // Blank fields are included as null instead of being omitted.
            .serializeNulls()
            .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
                if (json == null) {
                    Date()
                } else {
                    formatter.timeZone = TimeZone.getDefault()
                    formatter.parse(json.asString)
                }
            })
            .registerTypeAdapter(Date::class.java, JsonSerializer<Date> { src, _, _ ->
                formatter.timeZone = TimeZone.getTimeZone("UTC+8")
                JsonPrimitive(formatter.format(src))
            })
            .create()

    protected val restAPI: RestAPI by lazy {
        Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(RestAPI::class.java)
    }


    protected val cacheAPI: RestAPI by lazy {
        Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(cacheClient)
                .build()
                .create(RestAPI::class.java)
    }


    companion object {
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
    }
}