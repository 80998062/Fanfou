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

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.sinyuk.fanfou.viewmodel.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


/**
 * Created by sinyuk on 2017/12/6.
 *
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun accountViewModel(accountViewModel: AccountViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(TimelineViewModel::class)
    abstract fun timelineViewModel(timelineViewModel: TimelineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel::class)
    abstract fun playerViewModel(playerViewModel: PlayerViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(StatusesViewModel::class)
    abstract fun statusesViewModel(statusesViewModel: StatusesViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: FanfouViewModelFactory): ViewModelProvider.Factory
}