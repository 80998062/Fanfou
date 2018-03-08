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
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.StatusCreation
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.drawer.DrawerToggleEvent
import com.sinyuk.fanfou.ui.editor.EditorView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.player.PlayerViewEvent
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.search.event.InputEvent
import com.sinyuk.fanfou.ui.search.event.QueryEvent
import com.sinyuk.fanfou.ui.status.StatusView
import com.sinyuk.fanfou.ui.status.StatusViewEvent
import com.sinyuk.fanfou.ui.timeline.FetTopEvent
import com.sinyuk.fanfou.util.ActionBarUi
import com.sinyuk.fanfou.util.ActionButton
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.ActionBarViewModel
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

    companion object {
        const val TAG = "TabView"
    }

    override fun layoutId() = R.layout.home_tab_view

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }
    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }
    private val actionBarViewModel by lazy { obtainViewModelFromActivity(factory, ActionBarViewModel::class.java) }
    @Inject
    lateinit var toast: ToastUtils


    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this@TabView)
    }


    private var player: Player? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            accountViewModel.profile.observe(this, Observer {
                player = it
            })

            actionBarViewModel.actionBarUiPayload.observe(this@TabView, Observer {
                if (it != null) applyPayloads(it)
            })
        }

        renderUI()
    }


    private fun applyPayloads(payLoads: ActionBarUi.PayLoads) {
        if (payLoads.get().isEmpty) return
        if (payLoads.get().containsKey(ActionBarUi.TITLE)) {
            Log.d(TAG, "Update title")
            actionBarTitle.text = payLoads.get().getString(ActionBarUi.TITLE)
        }

        if (payLoads.get().containsKey(ActionBarUi.DISPLAYED_CHILD_INDEX)) {
            val index = payLoads.get().getInt(ActionBarUi.DISPLAYED_CHILD_INDEX)
            Log.d(TAG, "Update displayedChild: $index")
            actionBarSwitcher.displayedChildId = if (index == 0) titleView.id else searchView.id
        }

        if (payLoads.get().containsKey(ActionBarUi.SUBTITLE)) {
            Log.d(TAG, "Update subtitle")
            actionBarSubTitle.text = payLoads.get().getString(ActionBarUi.SUBTITLE)
        }

        if (payLoads.get().containsKey(ActionBarUi.BACKGROUND_COLOR)) {
            Log.d(TAG, "Update background")
            val color = payLoads.get().getInt(ActionBarUi.BACKGROUND_COLOR)
            setBackground(color)
        }
        if (payLoads.get().containsKey(ActionBarUi.START_BUTTON_TYPE)) {
            Log.d(TAG, "Update start button")
            val startButtonType = payLoads.get().getInt(ActionBarUi.START_BUTTON_TYPE)
            when (startButtonType) {
                ActionButton.Avatar -> {
                    GlideApp.with(navImageView).load(player?.profileImageUrl).avatar().transition(withCrossFade()).into(navImageView)
                    navImageView.setOnClickListener { EventBus.getDefault().post(DrawerToggleEvent()) }
                }
                ActionButton.Back -> {
                    GlideApp.with(this).load(R.drawable.ic_back).into(navImageView)
                    navImageView.setOnClickListener { onBackPressedSupport() }
                }
            }
        }
        if (payLoads.get().containsKey(ActionBarUi.END_BUTTON_TYPE)) {
            Log.d(TAG, "Update end button")
            val endButtonType = payLoads.get().getInt(ActionBarUi.END_BUTTON_TYPE)
            when (endButtonType) {
                ActionButton.Rice -> {
                    endButton.setOnClickListener { (activity as AbstractActivity).start(EditorView.newInstance(action = StatusCreation.CREATE_NEW)) }
                    R.drawable.ic_rice
                }
                ActionButton.Send -> {
                    endButton.setOnClickListener { }
                    R.drawable.ic_sendmessage
                }
                ActionButton.Settings -> {
                    endButton.setOnClickListener { }
                    R.drawable.ic_settings_ac
                }
                ActionButton.AddFriend -> {
                    endButton.setOnClickListener { }
                    R.drawable.ic_addfriend
                }
                else -> -1
            }.let {
                loadDrawable(endButton, it)
            }
        }
    }

    private fun loadDrawable(view: ImageView?, res: Int) {
        if (view == null || res == -1) return
        GlideApp.with(view).load(res).transition(withCrossFade()).into(view)
    }


    private fun setBackground(@ColorInt color: Int) {
        val navBackground = if (color == Color.TRANSPARENT) {
            R.color.scrim
        } else {
            android.R.color.transparent
        }

        actionBar.setBackgroundColor(color)
        navImageView.setBackgroundColor(ContextCompat.getColor(context!!, navBackground))
    }


    private fun renderUI() {
        setupKeyboard()
        setupViewPager()
        setupSearchWidget()
    }


    private fun setupKeyboard() {
        KeyboardUtil.attach(activity, panelRoot) {
            if (it) {
                if (currentFragment == 1) searchEt?.requestFocus()
            } else {
                searchEt?.clearFocus()
            }
        }
    }

    private fun setupSearchWidget() {
        searchEt.setOnClickListener { if (!searchEt.isFocusableInTouchMode) expandSearchView() }
        searchBg.setOnClickListener { if (!searchEt.isFocusableInTouchMode) expandSearchView() }

//        navBack.setOnClickListener {
//            collapseSearchView()
//            (fragments[1] as SearchView).showTrending()
//        }
//        searchCloseButton.setOnClickListener {
//            collapseSearchView()
//            (fragments[1] as SearchView).showTrending()
//        }

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
                    actionBarViewModel.apply(ActionBarUi.PayLoads().startButtonType(ActionButton.Back).get())
                } else {
                    actionBarViewModel.apply(ActionBarUi.PayLoads().startButtonType(ActionButton.Avatar).get())
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
//                actionButtonSwitcher.displayedChildId = R.id.searchCloseButton
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
        fragments = if (findChildFragment(IndexView::class.java) == null) {
            mutableListOf(IndexView(), SearchView(), SignInView(), MessageView())
        } else {
            mutableListOf(findChildFragment(IndexView::class.java), findChildFragment(SearchView::class.java), findChildFragment(SignInView::class.java), findChildFragment(MessageView::class.java))
        }

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = fragments.size
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size
        }

        currentFragment = 0
        actionBarViewModel.apply(ActionBarUi.PayLoads().background(ContextCompat.getColor(context!!, R.color.colorPrimary))
                .startButtonType(ActionButton.Avatar).title(titles[0]).endButtonType(ActionButton.Rice).get())
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStatusViewEvent(event: StatusViewEvent) {
        if (currentFragment == null) return
        val toFragment = StatusView.newInstance(status = event.status, photoExtra = event.photoExtra)
        fragments[currentFragment!!].childFragmentManager.beginTransaction()
                .addSharedElement(navImageView, navImageView.transitionName)
                .addSharedElement(actionBar, actionBar.transitionName)
                .addSharedElement(endButton, endButton.transitionName)
                .addSharedElement(actionBarTitle, actionBarTitle.transitionName)
                .replace(R.id.fragment_container, toFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayerViewEvent(event: PlayerViewEvent) {
        if (currentFragment == null) return
        val toFragment = PlayerView.newInstance(uniqueId = event.uniqueId)
        fragments[currentFragment!!].childFragmentManager.beginTransaction()
                .addSharedElement(navImageView, navImageView.transitionName)
                .addSharedElement(actionBar, actionBar.transitionName)
                .addSharedElement(endButton, endButton.transitionName)
                .addSharedElement(actionBarTitle, actionBarTitle.transitionName)
                .replace(R.id.fragment_container, toFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabEvent(event: TabEvent) {
        val to = event.index
        if (event.again) {

        } else {
            onPageSwitched(to)
        }
    }

    private val titles by lazy { resources.getStringArray(R.array.tab_titles) }

    private var currentFragment: Int? = null

    private fun onPageSwitched(to: Int) {
        if (to == currentFragment) return

        val payload = ActionBarUi.PayLoads()
        if (to == 1) {
            KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            payload.displayedChildIndex(1)
        } else if (currentFragment == 1) {
            payload.displayedChildIndex(0)
        }

        val endButton = when (to) {
            0 -> ActionButton.Rice
            1 -> ActionButton.AddFriend
            2 -> ActionButton.Settings
            3 -> ActionButton.Send
            else -> null
        }

        endButton?.let { payload.endButtonType(endButton) }

        actionBarViewModel.apply(payload.title(titles[to]).get())
        viewPager.setCurrentItem(to, false)
        currentFragment = to
    }


    private val handler by lazy { Handler() }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFetchTop(event: FetTopEvent) {
//        when (event.type) {
//            TYPE.TOAST -> {
//                val toast = View.inflate(context, R.layout.toast_fetch_top, null)
//                toast.textView.text = event.message
//                val popup = PopupWindow(toast, WRAP_CONTENT, WRAP_CONTENT)
//                toast.textView.setOnClickListener {
//                    EventBus.getDefault().post(ScrollToTopEvent())
//                    popup.dismiss()
//                }
//                popup.isFocusable = false
//                popup.showAtLocation(coordinator, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, ConvertUtils.dp2px(context, 72f))
//                handler.postDelayed({ popup.dismiss() }, 10000)
//            }
//            TYPE.ACTIONBAR -> {
//                toastSwitcher.setCurrentText(event.message)
//                handler.postDelayed({ toastSwitcher.setCurrentText("") }, 2000)
//            }
//        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@TabView)
        handler.removeCallbacksAndMessages(null)
    }
}