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

package com.sinyuk.fanfou.ui.drawer

import android.content.res.Configuration
import android.os.Bundle
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.currentNightMode
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.drawer_view.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/31.
 *
 */
class DrawerView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.drawer_view

    @Inject
    lateinit var toast: ToastUtils

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        nightModeButton.setOnClickListener {
            when (currentNightMode(context!!.applicationContext)) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    toast.toastShort("夜间模式关闭")
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    toast.toastShort("夜间模式开启")
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    toast.toastShort("夜间模式不知道")
                }
            }
        }
    }


    private fun toggleDrawer(open: Boolean) {
        EventBus.getDefault().post(DrawerToggleEvent(open))
    }
}