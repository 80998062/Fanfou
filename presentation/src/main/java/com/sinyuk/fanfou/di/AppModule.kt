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

package com.sinyuk.fanfou.di

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.*
import com.sinyuk.fanfou.App
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.util.LiveDataCallAdapterFactory
import com.sinyuk.fanfou.util.Oauth1SigningInterceptor
import com.sinyuk.myutils.system.ToastUtils
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


/**
 * Created by sinyuk on 2017/11/28.
 *
 */
@Module(includes = [(ViewModelModule::class)])
class AppModule constructor(private val app: App) {

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Suppress("unused")
    @Provides
    @Singleton
    @Named(DATABASE_IN_DISK)
    fun provideDatabase(): LocalDatabase = LocalDatabase.getInstance(app)


    @Suppress("unused")
    @Provides
    @Singleton
    @Named(DATABASE_IN_MEMORY)
    fun provideMemoryDatabase(): LocalDatabase = LocalDatabase.getInMemory(app)


    @Suppress("unused")
    @Provides
    @Singleton
    @Named(TYPE_GLOBAL)
    fun provideGlobalPrefs(): SharedPreferences = app.getSharedPreferences(TYPE_GLOBAL, MODE_PRIVATE)


    @Suppress("unused")
    @Provides
    @Singleton
    fun provideToastUtils(): ToastUtils = ToastUtils(app)


    @Suppress("unused")
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
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
    }

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideEndpoint() = Endpoint("http://api.fanfou.com/")

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideAuthenticator(@Named(TYPE_GLOBAL) p: SharedPreferences) =
            Oauth1SigningInterceptor(p.getString(ACCESS_TOKEN, null), p.getString(ACCESS_SECRET, null))

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideOkHttp(interceptor: Oauth1SigningInterceptor): OkHttpClient {
        @Suppress("UNUSED_VARIABLE")
        val max = (1024 * 1024 * 100).toLong()
        val timeout: Long = 30
        return OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
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


    @Suppress("unused")
    @Provides
    @Singleton
    fun provideRestAPI(gson: Gson, endpoint: Endpoint, okHttpClient: OkHttpClient): RestAPI {
        return Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(RestAPI::class.java)
    }


    companion object {
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
    }

}