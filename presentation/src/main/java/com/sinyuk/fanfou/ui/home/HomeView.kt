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

package com.sinyuk.fanfou.ui.home

import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class HomeView : AbstractLazyFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.home_view

    @Inject lateinit var factory: FanfouViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    @Inject lateinit var toast: ToastUtils


    override fun lazyDo() {
        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java)
    }


}