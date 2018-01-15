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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.search.event.InputEvent
import com.sinyuk.fanfou.ui.search.event.QueryEvent
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.home_tab_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/11.
 *
 */
class TabView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.home_tab_view

    @Inject lateinit var factory: ViewModelProvider.Factory
    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }
    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }
    @Inject lateinit var toast: ToastUtils


    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)

        renderUI()

        EventBus.getDefault().register(this@TabView)

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
        setupKeyboard()
        setupViewPager()
        setupSearchWidget()
    }

    private fun renderAccount(data: Player?) {
        if (data == null) {

        } else {
            GlideApp.with(avatar).asDrawable().load(data.profileImageUrl).avatar().transition(withCrossFade()).into(avatar)
        }
    }


    private fun setupActionBar() {
        viewAnimator.displayedChildId = R.id.textSwitcher
        navigationAnimator.displayedChildId = R.id.avatar
        avatar.setOnClickListener { (activity as AbstractActivity).start(PlayerView.newInstance()) }
        postFanfouButton.setOnClickListener { toast.toastShort("发送饭否") }
    }


    private fun setupKeyboard() {
        KeyboardUtil.attach(activity, panelRoot) {
            if (it) {
                if (currentFragment == 1) searchEt.requestFocus()
            } else {
                searchEt.clearFocus()
            }
        }

        coordinator.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            return@setOnTouchListener false
        }
    }

    private fun setupSearchWidget() {
        searchEt.setOnClickListener { if (!searchEt.isFocusableInTouchMode) expandSearchView() }
        searchBg.setOnClickListener { if (!searchEt.isFocusableInTouchMode) expandSearchView() }

        navBack.setOnClickListener {
            collapseSearchView()
            (fragments[1] as SearchView).showTrending()
        }
        searchCloseButton.setOnClickListener {
            collapseSearchView()
            (fragments[1] as SearchView).showTrending()
        }

        searchEt.setOnFocusChangeListener { _, hasFocus ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasFocus) {
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorAccent))
                } else {
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.textColorHint))
                }
            }
        }
        searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                EventBus.getDefault().post(InputEvent(s.toString()))
            }
        })

        searchEt.setOnEditorActionListener { _, id, _ ->
            if (arrayOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_NULL).contains(id)) {
                val query = searchEt.text.toString()
                collapseSearchView(query)
                if (query.isNotBlank()) {
                    (fragments[1] as SearchView).showResult(query)
                    searchViewModel.save(query)
                    EventBus.getDefault().post(QueryEvent(query))
                }
            }
            return@setOnEditorActionListener false
        }
    }

    /**
     * 收起🔍栏
     */
    private fun collapseSearchView(query: String? = null) {
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
//                searchEt.layoutParams.apply {
//                    width = WRAP_CONTENT
//                    searchEt.layoutParams = this
//                }
                searchEt.setText(query)
                if (query?.isNotBlank() == true) searchEt.setSelection(query.length)
                KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
                searchEt.isFocusableInTouchMode = false

                if (query?.isNotBlank() == true) {
                    navigationAnimator.displayedChildId = R.id.navBack
                    actionButtonSwitcher.displayedChildId = R.id.postFanfouButton
                } else {
                    navigationAnimator.displayedChildId = R.id.avatar
                    actionButtonSwitcher.displayedChildId = R.id.searchPlayerButton
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
                (fragments[1] as SearchView).showSuggestion()
                searchEt.isFocusableInTouchMode = true
                KPSwitchConflictUtil.showKeyboard(panelRoot, searchEt)
            }
        })
        animator.start()
    }


    private lateinit var fragments: MutableList<AbstractFragment>

    private fun setupViewPager() {
        fragments = if (findChildFragment(TimelineView::class.java) == null) {
            mutableListOf(TimelineView.newInstance(TIMELINE_HOME), SearchView(), SignInView(), MessageView())
        } else {
            mutableListOf(
                    findChildFragment(TimelineView::class.java),
                    findChildFragment(SearchView::class.java),
                    findChildFragment(SignInView::class.java),
                    findChildFragment(MessageView::class.java))
        }

        loadMultipleRootFragment(R.id.fakeViewPager, 0, fragments[0], fragments[1], fragments[2], fragments[3])
        onPageSwitched(TabEvent(index = 0))
    }


    private var currentFragment: Int? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPageSwitched(event: TabEvent) {
        val to = event.index
        if (to == currentFragment) return
        when (to) {
            0 -> actionButtonSwitcher.displayedChildId = R.id.postFanfouButton
            2 -> actionButtonSwitcher.displayedChildId = R.id.inboxSettingsButton
            3 -> actionButtonSwitcher.displayedChildId = R.id.sendMessageButton
        }

        if (to == 1) {
            viewAnimator.displayedChildId = R.id.searchLayout
            textSwitcher.setCurrentText(null)
            when ((fragments[1] as SearchView).currentFragment) {
                0 -> {
                    actionButtonSwitcher.displayedChildId = R.id.searchPlayerButton
                    navigationAnimator.displayedChildId = R.id.avatar
                }
                1 -> {
                    actionButtonSwitcher.displayedChildId = R.id.searchCloseButton
                    navigationAnimator.displayedChildId = R.id.avatar
                }
                2 -> {
                    actionButtonSwitcher.displayedChildId = R.id.postFanfouButton
                    navigationAnimator.displayedChildId = R.id.navBack
                }
            }
        } else {
            navigationAnimator.displayedChildId = R.id.avatar
            KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            viewAnimator.displayedChildId = R.id.textSwitcher
            textSwitcher.setCurrentText(resources.getStringArray(R.array.tab_titles)[to])
        }
        currentFragment?.let { showHideFragment(fragments[to], fragments[it]) }
        currentFragment = to
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@TabView)
    }
}