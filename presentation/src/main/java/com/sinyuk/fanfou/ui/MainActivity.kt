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

import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.addFragmentInActivity
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */
class MainActivity : AbstractActivity(), View.OnClickListener {
    companion object {

        @JvmStatic
        fun start(context: Context, flags: Int? = null) {
            val intent = Intent(context, MainActivity::class.java)
            flags?.let { intent.flags = flags }
            context.startActivity(intent)
        }
    }

    override fun beforeInflate() {
    }

    override fun layoutId(): Int? = R.layout.main_activity

    @Inject lateinit var factory: ViewModelProvider.Factory

    private val accountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }
    private val playerViewModel by lazy { obtainViewModel(factory, PlayerViewModel::class.java) }
    private val searchViewModel by lazy { obtainViewModel(factory, SearchViewModel::class.java) }


    @Inject lateinit var toast: ToastUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        renderUI()

        accountViewModel.user.observe(this, Observer {
            when (it?.states) {
                States.SUCCESS -> renderAccount(it.data)
                else -> {
                    renderAccount(null)
                }
            }
        })
    }

    private fun renderUI() {
        setupActionBar()
        setupViewPager()
        setupSearchWidget()
        supportFragmentManager.addOnBackStackChangedListener {
        }
    }

    private fun renderAccount(data: Player?) {
        if (data == null) {

        } else {
            Glide.with(avatar)
                    .asDrawable()
                    .load(data.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .transition(withCrossFade())
                    .into(avatar)
        }
    }

    private fun setupActionBar() {
        avatar.setOnClickListener { addFragmentInActivity(PlayerView(), R.id.firstLevelFragment, true) }
    }


    private fun setupSearchWidget() {
        searchBg.setOnClickListener {
            if (!searchEt.isFocusableInTouchMode) {
                expandSearchView()
            }
        }
        searchCloseButton.setOnClickListener {
            collapseSearchView()
        }
    }

    /**
     * 收起🔍栏
     */
    private fun collapseSearchView() {
        actionButtonSwitcher.displayedChild = R.id.searchPlayerButton
    }

    /**
     * 展开🔍栏
     */
    private fun expandSearchView() {
        actionButtonSwitcher.displayedChild = R.id.searchCloseButton
        val hideAvatar = ObjectAnimator.ofFloat(avatar, View.TRANSLATION_X, 0f, -resources.getDimensionPixelOffset(R.dimen.action_bar_height).toFloat())
    }

    private fun setupViewPager() {
        val homePage = TimelineView.newInstance(TIMELINE_HOME)
        val searchPage = SearchView()
        val signView = SignInView()
        val messagePage = MessageView()

        val adapter = RootPageAdapter(supportFragmentManager, mutableListOf(homePage, searchPage, signView, messagePage))

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = adapter

        homeTab.setOnClickListener(this)
        publicTab.setOnClickListener(this)
        notificationTab.setOnClickListener(this)
        messageTab.setOnClickListener(this)

        onPageChangedAndIdle(0)
    }

    private fun onPageChangedAndIdle(current: Int) {
        if (current == 1) {
            viewAnimator.displayedChildId = R.id.searchLayout
            textSwitcher.setCurrentText(null)
        } else {
            viewAnimator.displayedChildId = R.id.textSwitcher
            textSwitcher.setCurrentText(resources.getStringArray(R.array.tab_titles)[current])
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.homeTab -> {
                viewPager.setCurrentItem(0, false)
                actionButtonSwitcher.displayedChild = R.id.postFanfouButton
                onPageChangedAndIdle(0)
            }
            R.id.publicTab -> {
                viewPager.setCurrentItem(1, false)
                onPageChangedAndIdle(1)
                if (searchEt.isFocusableInTouchMode) {
                    actionButtonSwitcher.displayedChild = R.id.searchCloseButton
                    searchEt.requestFocus()
                } else {
                    actionButtonSwitcher.displayedChild = R.id.searchPlayerButton
                }
            }
            R.id.notificationTab -> {
                viewPager.setCurrentItem(2, false)
                actionButtonSwitcher.displayedChild = R.id.inboxSettingsButton
                onPageChangedAndIdle(2)
            }
            R.id.messageTab -> {
                viewPager.setCurrentItem(3, false)
                actionButtonSwitcher.displayedChild = R.id.sendMessageButton
                onPageChangedAndIdle(3)
            }
        }
    }


}
