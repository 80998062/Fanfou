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

import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.home.HomeView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.search.PublicView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * Created by sinyuk on 2017/11/28.
 */
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun signInView(): SignInView


    @ContributesAndroidInjector
    abstract fun homeView(): HomeView

    @ContributesAndroidInjector
    abstract fun publicView(): PublicView

    @ContributesAndroidInjector
    abstract fun playerView(): PlayerView

    @ContributesAndroidInjector
    abstract fun messageView(): MessageView


    @ContributesAndroidInjector
    abstract fun timelineView(): TimelineView
}