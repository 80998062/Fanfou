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

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.domain.StatusCreation
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.account.SignInView
import com.sinyuk.fanfou.ui.colormatchtabs.adapter.ColorTabAdapter
import com.sinyuk.fanfou.ui.colormatchtabs.listeners.OnColorTabSelectedListener
import com.sinyuk.fanfou.ui.colormatchtabs.model.ColorTab
import com.sinyuk.fanfou.ui.drawer.DrawerView
import com.sinyuk.fanfou.ui.editor.EditorView
import com.sinyuk.fanfou.ui.home.IndexView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.timeline.FetTopEvent
import com.sinyuk.fanfou.util.ActionButton
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.ActionBarViewModel
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.home_activity.*
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                                                                  │
 * │        _______. __  .__   __. ____    ____  __    __   __  ___   │
 * │       /       ||  | |  \ |  | \   \  /   / |  |  |  | |  |/  /   │
 * │      |   (----`|  | |   \|  |  \   \/   /  |  |  |  | |  '  /    │
 * │       \   \    |  | |  . `  |   \_    _/   |  |  |  | |    <     │
 * │   .----)   |   |  | |  |\   |     |  |     |  `--'  | |  .  \    │
 * │   |_______/    |__| |__| \__|     |__|      \______/  |__|\__\   │
 * │                                                                  │
 * └──────────────────────────────────────────────────────────────────┘
 *
 */
class HomeActivity : AbstractActivity() {

    companion object {
        const val TAG = "HomeActivity"
    }

    override fun layoutId() = R.layout.home_activity

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

    private val delayHandler by lazy { Handler() }

//    @field:[Named(TYPE_GLOBAL) Inject]
//    lateinit var sharedPreferences: SharedPreferences

    /**
     * 监听软键盘
     */
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawerLayout(savedInstanceState)
        setupTabLayout(savedInstanceState)
        setupViewPager(savedInstanceState)

        onGlobalLayoutListener = KeyboardUtil.attach(this@HomeActivity, panelRoot, {

        })
    }


    private fun setupDrawerLayout(savedInstanceState: Bundle?) {
        drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            }
        })

        navImageView.setOnClickListener {
            if (findFragment(DrawerView::class.java) == null) {
                loadRootFragment(R.id.drawerViewContainer, DrawerView(), false, false)
            }
            drawerLayout.openDrawer()
        }
    }

    @SuppressLint("Recycle")
    private fun setupTabLayout(savedInstanceState: Bundle?) {
        val colors = resources.getStringArray(R.array.tab_colors)
        val icons = resources.obtainTypedArray(R.array.tab_icons)
        try {
            resources.getStringArray(R.array.tab_titles).apply {
                forEachIndexed { index, title ->
                    val color = Color.parseColor(colors[index])
                    val icon = icons.getDrawable(index)
                    tabLayout.addTab(ColorTabAdapter.createColorTab(tabLayout, title, color, icon))
                }
            }
        } finally {
            icons.recycle()
        }


        tabLayout.addOnColorTabSelectedListener(object : OnColorTabSelectedListener {
            override fun onReSelectedTab(tab: ColorTab?) {
                if (BuildConfig.DEBUG) Log.i("onReSelectedTab", "position: " + tab?.position)
            }

            override fun onDoubleClick(tab: ColorTab?) {
                if (BuildConfig.DEBUG) Log.i("onDoubleClick", "position: " + tab?.position)

            }

            override fun onSelectedTab(tab: ColorTab?) {
                if (BuildConfig.DEBUG) Log.i("onSelectedTab", "position: " + tab?.position)
                onPageSwitched(tab?.position ?: 0)
            }

            override fun onUnselectedTab(tab: ColorTab?) {
            }
        })
    }


    private lateinit var fragments: MutableList<AbstractFragment>

    private val titles by lazy { resources.getStringArray(R.array.tab_titles) }

    private var currentFragment: Int = 0

    private fun setupViewPager(savedInstanceState: Bundle?) {
        fragments = if (savedInstanceState == null) {
            mutableListOf(IndexView(), SearchView(), SignInView(), MessageView())
        } else {
            currentFragment = savedInstanceState.getInt("currentFragment", 0)
            mutableListOf(findFragment(IndexView::class.java), findFragment(SearchView::class.java), findFragment(SignInView::class.java), findFragment(MessageView::class.java))
        }

        loadMultipleRootFragment(R.id.viewPager, currentFragment, *fragments.toTypedArray())
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("currentFragment", currentFragment)
    }

    private fun onPageSwitched(to: Int) {
        if (to == currentFragment) return
        KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
        if (to == 1) {
            actionBarSwitcher.displayedChildId = searchView.id
        } else if (currentFragment == 1) {
            actionBarSwitcher.displayedChildId = titleView.id
        }

        actionBarTitle.text = titles[to]

        when (to) {
            0 -> {
                endButton.setOnClickListener { start(EditorView.newInstance(action = StatusCreation.CREATE_NEW)) }
                ActionButton.Rice
            }
            1 -> ActionButton.AddFriend
            2 -> ActionButton.Settings
            3 -> ActionButton.Send
            else -> null
        }?.let {
            GlideApp.with(this).load(it).into(endButton)
        }

        showHideFragment(fragments[to], fragments[currentFragment])
        currentFragment = to
    }


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


    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }


    private var renderingRunnable: Runnable? = Runnable {
        renderingRunnable = null
        accountViewModel.profile.observe(this@HomeActivity, Observer {
            it?.apply {
                GlideApp.with(navImageView).load(profileImageUrlLarge).avatar().transition(withCrossFade()).into(navImageView)
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
        if (renderingRunnable != null) {
            window.decorView.post { delayHandler.post(renderingRunnable) }
        }
    }

    override fun onDestroy() {
        delayHandler.removeCallbacksAndMessages(null)
        KeyboardUtil.detach(this, onGlobalLayoutListener)
        super.onDestroy()
    }


    override fun onCreateFragmentAnimator(): FragmentAnimator {
        return DefaultHorizontalAnimator()
    }

}
