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
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.search.event.InputEvent
import com.sinyuk.fanfou.ui.search.event.QueryEvent
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.main_activity.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 *
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

    @Inject lateinit var factory: ViewModelProvider.Factory

    override fun layoutId() = R.layout.main_activity

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
        setupKeyboard()
        setupViewPager()
        setupSearchWidget()
    }

    private fun renderAccount(data: Player?) {
        if (data == null) {

        } else {
            Glide.with(avatar).asDrawable().load(data.profileImageUrl).apply(RequestOptions().circleCrop()).transition(withCrossFade()).into(avatar)
        }
    }

    private fun setupActionBar() {
        viewAnimator.displayedChildId = R.id.textSwitcher
        navigationAnimator.displayedChildId = R.id.avatar
        avatar.setOnClickListener { loadRootFragment(R.id.rootFragmentContainer, PlayerView.newInstance()) }

        postFanfouButton.setOnClickListener { toast.toastShort("发送饭否") }
    }


    private fun setupKeyboard() {
        KeyboardUtil.attach(this, panelRoot) {
            if (it) {
                if (currentFragment == 1) searchEt.requestFocus()
            } else {
                searchEt.clearFocus()
            }
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
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent))
                } else {
                    searchEt.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.textColorHint))
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
                    (fragments[1] as SearchView).showResult()
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
        fragments = if (findFragment(TimelineView::class.java) == null) {
            mutableListOf(TimelineView.newInstance(TIMELINE_HOME), SearchView(), SignInView(), MessageView())
        } else {
            mutableListOf(findFragment(TimelineView::class.java), findFragment(SearchView::class.java), findFragment(SignInView::class.java), findFragment(MessageView::class.java))
        }

        loadMultipleRootFragment(R.id.fakeViewPager, 0, fragments[0], fragments[1], fragments[2], fragments[3])
        onPageSwitched(0)

        homeTab.setOnClickListener(this)
        publicTab.setOnClickListener(this)
        notificationTab.setOnClickListener(this)
        messageTab.setOnClickListener(this)
    }


    private var currentFragment = 0
    private fun onPageSwitched(to: Int) {
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

        showHideFragment(fragments[to], fragments[currentFragment])
        currentFragment = to
    }


    override fun onClick(v: View?) {
        val to = when (v?.id) {
            R.id.homeTab -> 0
            R.id.publicTab -> 1
            R.id.notificationTab -> 2
            R.id.messageTab -> 3
            else -> TODO()
        }

        when (to) {
            0 -> actionButtonSwitcher.displayedChildId = R.id.postFanfouButton
            2 -> actionButtonSwitcher.displayedChildId = R.id.inboxSettingsButton
            3 -> actionButtonSwitcher.displayedChildId = R.id.sendMessageButton
        }

        if (to != currentFragment) onPageSwitched(to)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (panelRoot.visibility == View.VISIBLE) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
