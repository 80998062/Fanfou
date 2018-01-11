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
import android.support.v4.app.FragmentPagerAdapter
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.AccordionTransformer
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.public_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class SearchView : AbstractFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.public_view

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils


    private lateinit var fragments: MutableList<AbstractFragment>

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        fragments = if (findChildFragment(TrendingView::class.java) == null) {
            mutableListOf(TrendingView(), SuggestionView(), SearchResultView())
        } else {
            mutableListOf(findChildFragment(TrendingView::class.java), findChildFragment(SuggestionView::class.java), findChildFragment(SearchResultView::class.java))
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setPagingEnabled(false)
        viewPager.setPageTransformer(false, AccordionTransformer())
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]
            override fun getCount() = fragments.size
        }
    }

    var currentFragment = 0
        get() = viewPager.currentItem

    fun showResult() {
        viewPager.setCurrentItem(2, false)
    }

    fun showSuggestion() {
        viewPager.setCurrentItem(1, false)
    }

    fun showTrending() {
        viewPager.setCurrentItem(0, false)
    }
}