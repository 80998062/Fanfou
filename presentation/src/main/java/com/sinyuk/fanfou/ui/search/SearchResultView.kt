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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.TIMELINE_USER
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import com.sinyuk.fanfou.ui.timeline.TimelineView
import kotlinx.android.synthetic.main.search_result_view.*

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


    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST)

        fragmentList = mutableListOf(TimelineView.newInstance(TIMELINE_USER), TimelineView.newInstance(TIMELINE_USER))
        setupViewPager()
    }


    private lateinit var query: String

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        query = arguments!!.getString("query")
    }

    fun setQuery(it: String) {
        if (query === it) return
        query = it
    }

    lateinit var fragmentList: MutableList<Fragment>

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = fragmentList.size
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
}