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

package com.sinyuk.fanfou.ui.colormatchtabs.listeners

import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
import android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING
import com.sinyuk.fanfou.ui.colormatchtabs.colortabs.ColorMatchTabLayout
import java.lang.ref.WeakReference

/**
 * Created by anna on 11.05.17.
 *
 */
class ColorTabLayoutOnPageChangeListener(colorTabLayout: ColorMatchTabLayout) : ViewPager.OnPageChangeListener {

    private var previousScrollState: Int = 0
    private var scrollState: Int = 0

    private val tabLayoutReference: WeakReference<ColorMatchTabLayout> = WeakReference(colorTabLayout)

    override fun onPageScrollStateChanged(state: Int) {
        previousScrollState = scrollState
        scrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val tabLayout = tabLayoutReference.get()
        val updateIndicator = !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE)
        tabLayout?.setScrollPosition(position, positionOffset, updateIndicator)
    }

    override fun onPageSelected(position: Int) {
        val tabLayout = tabLayoutReference.get()
        if (tabLayout?.tabStripLayout?.isAnimate?.not() != false) {
            tabLayout?.select(tabLayout.getTabAt(position))
            tabLayout?.getSelectedTabView()?.clickedTabView = tabLayout?.getSelectedTabView()
        }
    }

}