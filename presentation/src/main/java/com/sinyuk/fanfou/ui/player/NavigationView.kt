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

package com.sinyuk.fanfou.ui.player

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.TIMELINE_USER
import com.sinyuk.fanfou.ui.photo.PhotoGridView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import kotlinx.android.synthetic.main.navigation_view.*

/**
 * Created by sinyuk on 2017/12/23.
 *
 */
class NavigationView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.navigation_view

    companion object {
        fun newInstance(uniqueId: String) = NavigationView().apply {
            arguments = Bundle().apply { putString("uniqueId", uniqueId) }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uniqueId = arguments?.getString("uniqueId")!!
        fragmentList = arrayListOf(TimelineView.newInstance(TIMELINE_USER, uniqueId), PhotoGridView.newInstance(uniqueId), TimelineView.newInstance(TIMELINE_FAVORITES, uniqueId))
        setupViewPager()
    }

    lateinit var fragmentList: MutableList<Fragment>

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = fragmentList.size - 1
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragmentList[position]

            override fun getCount() = fragmentList.size
        }
        tabStrip.setTabIndex(0, true)
        tabStrip.onTabStripSelectedIndexListener = object : NavigationTabStrip.OnTabStripSelectedIndexListener {
            override fun onStartTabSelected(title: String?, index: Int) {

            }

            override fun onEndTabSelected(title: String?, index: Int) {
                viewPager.setCurrentItem(index, false)
            }
        }
    }
}