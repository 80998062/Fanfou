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

package com.sinyuk.fanfou.injections

import com.sinyuk.fanfou.App
import dagger.android.support.AndroidSupportInjectionModule
import com.sinyuk.fanfou.injections.modules.AppModule
import dagger.Component
import javax.inject.Singleton


/**
 * Created by sinyuk on 2017/11/28.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, AndroidSupportInjectionModule::class, ActivityBuildersModule::class))
public interface AppComponent {
    fun inject(instance: App)
}
