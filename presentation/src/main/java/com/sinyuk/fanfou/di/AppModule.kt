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
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.App
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DATABASE_IN_MEMORY
import com.sinyuk.fanfou.domain.repo.Repository
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.api.RemoteDataSource
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.myutils.system.ToastUtils
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


/**
 * Created by sinyuk on 2017/11/28.
 *
 */
@Module
class AppModule constructor(private val app: App) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    @Named(DATABASE_IN_DISK)
    fun provideDatabase(): LocalDatabase = LocalDatabase.getInstance(app)



    @Provides
    @Singleton
    @Named(DATABASE_IN_MEMORY)
    fun provideMemoryDatabase(): LocalDatabase = LocalDatabase.getInMemory(app)

    @Provides
    @Singleton
    fun provideEndpoint() = Endpoint("http://api.fanfou.com/")

    @Provides
    @Singleton
    fun provideAuthenticator() = Oauth1SigningInterceptor(null)


    @Provides
    @Singleton
    @Named(TYPE_GLOBAL)
    fun provideGlobalPrefs(): SharedPreferences = app.getSharedPreferences(TYPE_GLOBAL, MODE_PRIVATE)

    @Provides
    @Singleton
    @Named(TYPE_GLOBAL)
    fun provideGlobalRxPrefs(@Named(TYPE_GLOBAL) sp: SharedPreferences): RxSharedPreferences = RxSharedPreferences.create(sp)

    @Provides
    @Singleton
    fun provideToastUtils(): ToastUtils = ToastUtils(app)


    @Provides
    @Singleton
    fun provideRepository(@Named(DATABASE_IN_DISK) d1: LocalDatabase,
                          @Named(DATABASE_IN_MEMORY) d2: LocalDatabase, e: Endpoint,
                          i: Oauth1SigningInterceptor,
                          @Named(TYPE_GLOBAL) sp: RxSharedPreferences): Repository =
            Repository(RemoteDataSource(app, e, i), d1, d2, i, sp)

}