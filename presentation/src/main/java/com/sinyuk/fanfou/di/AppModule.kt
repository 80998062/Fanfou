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
import com.sinyuk.fanfou.App
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DATABASE_IN_MEMORY
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
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
@Module(includes = arrayOf(ViewModelModule::class))
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
    fun provideAuthenticator(@Named(TYPE_GLOBAL) p: SharedPreferences) = Oauth1SigningInterceptor(p)


    @Provides
    @Singleton
    @Named(TYPE_GLOBAL)
    fun provideGlobalPrefs(): SharedPreferences = app.getSharedPreferences(TYPE_GLOBAL, MODE_PRIVATE)


    @Provides
    @Singleton
    fun provideToastUtils(): ToastUtils = ToastUtils(app)


}