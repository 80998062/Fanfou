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
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.addFragmentInActivity
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */
class MainActivity : AbstractActivity(), View.OnClickListener {
    companion object {

        @JvmStatic
        fun start(context: Context, flags: Int?) {
            val intent = Intent(context, MainActivity::class.java)
            flags?.let { intent.flags = flags }
            context.startActivity(intent)
        }
    }

    override fun beforeInflate() {
    }

    override fun layoutId(): Int? = R.layout.main_activity

    @Inject lateinit var factory: ViewModelProvider.Factory

    private lateinit var accountViewModel: AccountViewModel

    @Inject lateinit var toast: ToastUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java)

        setupActionBar()
        setupViewPager()

        supportFragmentManager.addOnBackStackChangedListener {
        }
    }


    private fun setupActionBar() {
        avatar.setOnClickListener {
            addFragmentInActivity(PlayerView(), R.id.fragment_container, true)
        }
    }

    private fun setupViewPager() {
        val homePage = TimelineView.newInstance(TIMELINE_HOME)
        val searchPage = SearchView()
        val signView = SignInView()
        val messagePage = MessageView()

        val adapter = RootPageAdapter(supportFragmentManager, mutableListOf(homePage, searchPage, signView, messagePage))

        viewPager.offscreenPageLimit = 4
        viewPager.adapter = adapter

        homeTab.setOnClickListener(this)
        publicTab.setOnClickListener(this)
        notificationTab.setOnClickListener(this)
        messageTab.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.homeTab -> {
                viewPager.setCurrentItem(0, false)
            }
            R.id.publicTab -> {
                viewPager.setCurrentItem(1, false)
            }
            R.id.notificationTab -> {
                viewPager.setCurrentItem(2, false)
            }
            R.id.messageTab -> {
                viewPager.setCurrentItem(3, false)
            }
        }
    }


}
