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

package com.sinyuk.fanfou.ui.activities

import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.home.HomeView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.ActionBarViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.main_activity.*
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/28.
 *
 */
class MainActivity : AbstractActivity() {

    override fun layoutId() = R.layout.main_activity

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    @Suppress("unused")
    private val accountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }
    @Suppress("unused")
    private val searchViewModel by lazy { obtainViewModel(factory, SearchViewModel::class.java) }

    @Suppress("unused")
    private val actionBarViewModel by lazy { obtainViewModel(factory, ActionBarViewModel::class.java) }

    @Suppress("unused")
    @Inject
    lateinit var toast: ToastUtils

    @field:[Named(TYPE_GLOBAL) Inject]
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KeyboardUtil.attach(this@MainActivity, panelRoot)
        if (sharedPreferences.getString(UNIQUE_ID, null) == null) {
            if (findFragment(SignInView::class.java) == null) {
                loadRootFragment(R.id.fragment_container, SignInView())
            } else {
                showHideFragment(findFragment(SignInView::class.java))
            }
        } else {
            if (findFragment(HomeView::class.java) == null) {
                loadRootFragment(R.id.fragment_container, HomeView())
            } else {
                showHideFragment(findFragment(HomeView::class.java))
            }
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

    override fun onCreateFragmentAnimator(): FragmentAnimator {
        return DefaultHorizontalAnimator()
    }

}
