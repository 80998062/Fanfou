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
package com.sinyuk.fanfou.ui.public

import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.TIMELINE_PUBLIC
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.addFragmentInFragment
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class PublicView : AbstractLazyFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.public_view

    @Inject lateinit var factory: FanfouViewModelFactory


    @Inject lateinit var toast: ToastUtils


    override fun lazyDo() {
        TimelineView.newInstance(TIMELINE_PUBLIC, null).let {
            addFragmentInFragment(it, R.id.fragment_container, false)
            it.userVisibleHint = true
        }
    }


}