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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
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
import com.sinyuk.fanfou.ui.search.SuggestionView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.addFragmentInActivity
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ImeUtils
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
        searchEt.setOnClickListener {
            if (!searchEt.isFocusable) {
                expandSearchView()
            }
        }
        searchBg.setOnClickListener {
            if (!searchEt.isFocusable) {
                expandSearchView()
            }
        }
        searchCloseButton.setOnClickListener { collapseSearchView() }

        searchEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.textColorHint))
                }
            }
        }
    }

    /**
     * 收起🔍栏
     */
    private fun collapseSearchView() {
        val animator = ObjectAnimator.ofFloat(searchEt, View.TRANSLATION_X, 0f)
        animator.duration = 200
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                onCollapse()
            }

            override fun onAnimationCancel(animation: Animator?) {
                onCollapse()
            }

            private fun onCollapse() {
                actionButtonSwitcher.displayedChildId = R.id.searchPlayerButton

//                searchEt.layoutParams.apply {
//                    width = WRAP_CONTENT
//                    searchEt.layoutParams = this
//                }
                searchEt.text = null
                searchEt.isFocusable = false
                searchEt.isFocusableInTouchMode = false
                ImeUtils.hideIme(searchEt)
                searchEt.clearFocus()

                supportFragmentManager.findFragmentByTag(SuggestionView::class.java.simpleName)?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }
        })
        animator.start()
    }

    private var searchTextOffset: Float? = null
    /**
     * 展开🔍栏
     */
    private fun expandSearchView() {
        if (searchTextOffset == null) {
            searchTextOffset = (searchBg.left - searchEt.left).toFloat()
        }
        val animator = ObjectAnimator.ofFloat(searchEt, View.TRANSLATION_X, 0f, searchTextOffset!!)
        animator.duration = 250
        animator.interpolator = AnticipateOvershootInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                onExpand()
            }

            override fun onAnimationCancel(animation: Animator?) {
                onExpand()
            }

            private fun onExpand() {
                actionButtonSwitcher.displayedChildId = R.id.searchCloseButton
//                searchEt.layoutParams.apply {
//                    width = MATCH_PARENT
//                    searchEt.layoutParams = this
//                }
                searchEt.isFocusable = true
                searchEt.isFocusableInTouchMode = true
                ImeUtils.showIme(searchEt)
                searchEt.requestFocus()
                addFragmentInActivity(SuggestionView(), R.id.secondLevelFragment, false)
            }
        })
        animator.start()
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

        onSwitchSearchView(0)
    }

    private fun onSwitchSearchView(current: Int) {
        if (current == 1) {
            viewAnimator.displayedChildId = R.id.searchLayout
            textSwitcher.setCurrentText(null)
            if (searchEt.isFocusableInTouchMode) {
                actionButtonSwitcher.displayedChildId = R.id.searchCloseButton
                ImeUtils.showIme(searchEt)
                searchEt.requestFocus()
            } else {
                actionButtonSwitcher.displayedChildId = R.id.searchPlayerButton
            }
        } else {
            if (searchEt.isFocusableInTouchMode) {
                ImeUtils.hideIme(searchEt)
                searchEt.clearFocus()
            }
            viewAnimator.displayedChildId = R.id.textSwitcher
            textSwitcher.setCurrentText(resources.getStringArray(R.array.tab_titles)[current])
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.homeTab -> {
                viewPager.setCurrentItem(0, false)
                actionButtonSwitcher.displayedChildId = R.id.postFanfouButton
                onSwitchSearchView(0)
            }
            R.id.publicTab -> {
                viewPager.setCurrentItem(1, false)
                onSwitchSearchView(1)
            }
            R.id.notificationTab -> {
                viewPager.setCurrentItem(2, false)
                actionButtonSwitcher.displayedChildId = R.id.inboxSettingsButton
                onSwitchSearchView(2)
            }
            R.id.messageTab -> {
                viewPager.setCurrentItem(3, false)
                actionButtonSwitcher.displayedChildId = R.id.sendMessageButton
                onSwitchSearchView(3)
            }
        }
    }


}
