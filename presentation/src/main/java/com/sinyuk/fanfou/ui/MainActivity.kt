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

package com.sinyuk.fanfou.ui

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.ui.home.HomeView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 *
 */
class MainActivity : AbstractActivity(){
    companion object {
        @JvmStatic
        fun start(context: Context, flags: Int? = null) {
            val intent = Intent(context, MainActivity::class.java)
            flags?.let { intent.flags = flags }
            context.startActivity(intent)
        }
    }

    override fun layoutId() = R.layout.main_activity

    @Inject lateinit var factory: ViewModelProvider.Factory
    @Suppress("unused")
    private val accountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }
    @Suppress("unused")
    private val searchViewModel by lazy { obtainViewModel(factory, SearchViewModel::class.java) }
    @Inject lateinit var toast: ToastUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (findFragment(HomeView::class.java) == null) {
            loadRootFragment(R.id.fragment_container, HomeView())
        } else {
            showHideFragment(findFragment(HomeView::class.java))
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?) = if (event?.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
        if (panelRoot.visibility == View.VISIBLE) {
            KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    } else {
        super.dispatchKeyEvent(event)
    }


}
