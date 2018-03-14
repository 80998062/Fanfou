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

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.ui.inbox.InboxView
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.player.PlayerViewEvent
import com.sinyuk.fanfou.ui.search.SearchView
import com.sinyuk.fanfou.ui.status.StatusViewEvent
import com.sinyuk.fanfou.ui.timeline.FetTopEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by sinyuk on 2018/1/11.
 *
 */
class TabView : AbstractFragment() {

    companion object {
        const val TAG = "TabView"
    }

    override fun layoutId() = R.layout.home_tab_view


    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this@TabView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() $savedInstanceState")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView() $savedInstanceState")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private var currentFragment: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() $savedInstanceState")
        if (savedInstanceState == null) {
            loadMultipleRootFragment(R.id.rootView, currentFragment, IndexView(), SearchView(), InboxView(), MessageView())
        } else {
            currentFragment = savedInstanceState.getInt("currentFragment", 0)
            showHideFragment(getCurrentFragment(currentFragment))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentFragment", currentFragment)
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStatusViewEvent(event: StatusViewEvent) {
        Log.d(TAG, "onStatusViewEvent")
//        when (currentFragment) {
//            0 -> findChildFragment(IndexView::class.java)?.loadRootFragment(R.id.rootView, StatusView.newInstance(event.status, photoExtra = event.photoExtra))
//        }
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayerViewEvent(event: PlayerViewEvent) {
        Log.d(TAG, "onPlayerViewEvent")
        when (currentFragment) {
//            0 -> findChildFragment(IndexView::class.java)?.toPlayerView(event)
        }
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


    private fun onPageSwitched(to: Int) {
        if (to == currentFragment) return
        showHideFragment(getCurrentFragment(to))
        currentFragment = to
    }

    private fun getCurrentFragment(index: Int) = when (index) {
        0 -> findChildFragment(IndexView::class.java)
        1 -> findChildFragment(SearchView::class.java)
        2 -> findChildFragment(InboxView::class.java)
        3 -> findChildFragment(MessageView::class.java)
        else -> null
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

    override fun onPause() {
        super.onPause()
        view?.let { KeyboardUtil.hideKeyboard(it) }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        EventBus.getDefault().unregister(this@TabView)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}