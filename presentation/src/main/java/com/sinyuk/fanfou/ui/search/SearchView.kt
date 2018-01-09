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
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class SearchView : AbstractFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.public_view

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils

    var currentFragment = 0

    lateinit var fragments: MutableList<AbstractFragment>

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        fragments = if (findChildFragment(TrendingView::class.java) == null) {
            mutableListOf(TrendingView(), SuggestionView(), SearchResultView())
        } else {
            mutableListOf(findChildFragment(TrendingView::class.java), findChildFragment(SuggestionView::class.java), findChildFragment(SearchResultView::class.java))
        }

        loadMultipleRootFragment(R.id.rootView, 0, fragments[0], fragments[1], fragments[2])
        currentFragment = 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentFragment", currentFragment)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        currentFragment = savedInstanceState?.getInt("currentFragment") ?: 0
    }

    fun showResult() {
        if (currentFragment == 2) return
        showHideFragment(findChildFragment(SearchResultView::class.java), fragments[currentFragment])
        currentFragment = 2
    }

    fun showSuggestion() {
        if (currentFragment == 1) return
        showHideFragment(findChildFragment(SuggestionView::class.java), fragments[currentFragment])
        currentFragment = 1
    }


    fun showTrending() {
        if (currentFragment == 0) return
        showHideFragment(findChildFragment(TrendingView::class.java), fragments[currentFragment])
        currentFragment = 0
    }
}