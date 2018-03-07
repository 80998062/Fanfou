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

package com.sinyuk.fanfou.ui.search

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import com.sinyuk.fanfou.ui.search.event.QueryEvent
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.Objects
import kotlinx.android.synthetic.main.search_result_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by sinyuk on 2018/1/5.
 *
 */
class SearchResultView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(query: String) = SearchResultView().apply {
            arguments = Bundle().apply { putString("query", query) }
        }
    }

    override fun layoutId() = R.layout.search_result_view

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST)

        query = arguments!!.getString("query")
//        fragmentList = if (findChildFragment(TimelineView::class.java) == null) {
//            mutableListOf(TimelineView.newInstance(SEARCH_TIMELINE_PUBLIC), SignInView())
//        } else {
//            (findChildFragment(TimelineView::class.java) as TimelineView).search(query)
//            mutableListOf(findChildFragment(TimelineView::class.java), findChildFragment(SignInView::class.java))
//        }
        setupViewPager()
    }


    private lateinit var query: String

    lateinit var fragmentList: MutableList<Fragment>

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = fragmentList.size
        viewPager.setPagingEnabled(false)
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragmentList[position]

            override fun getCount() = fragmentList.size
        }
        tabStrip.setTabIndex(0, true)
        tabStrip.onTabStripSelectedIndexListener = object : NavigationTabStrip.OnTabStripSelectedIndexListener {
            override fun onStartTabSelected(title: String?, index: Int) {

            }

            override fun onEndTabSelected(title: String?, index: Int) {
                viewPager.setCurrentItem(index, true)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuery(event: QueryEvent) {
        if (Objects.equals(event.query, query)) {
            // TODO
        } else {
            query = event.query!!
            findChildFragment(TimelineView::class.java)?.search(query)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}