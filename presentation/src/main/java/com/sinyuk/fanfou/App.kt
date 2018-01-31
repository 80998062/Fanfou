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

package com.sinyuk.fanfou

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.facebook.stetho.Stetho
import com.sinyuk.fanfou.di.AppInjector
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import me.yokeyword.fragmentation.Fragmentation
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by sinyuk on 2017/11/28.
 */
class App : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)

        initFragmentation()
        initStetho()

        configureNightMode()
    }


    private fun initFragmentation() {
        Fragmentation.builder().debug(BuildConfig.DEBUG)
                .stackViewMode(Fragmentation.BUBBLE)
                .handleException { Log.e("Fragmentation", "error", it) }
                .install()
    }

    private fun initStetho() {
        Stetho.initialize(Stetho.newInitializerBuilder(applicationContext)
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(applicationContext))
                .build())
    }

    @field:[Named(TYPE_GLOBAL) Inject]
    lateinit var sharedPreferences: SharedPreferences

    /**
     * Setup night mode
     */
    private fun configureNightMode() {
        if (sharedPreferences.getBoolean(NIGHT_MODE_AUTO, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        } else {
            if (sharedPreferences.getBoolean(NIGHT_MODE, false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}