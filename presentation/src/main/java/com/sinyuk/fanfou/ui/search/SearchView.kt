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
import android.view.View
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (findChildFragment(TrendingView::class.java) == null) {
            loadMultipleRootFragment(R.id.rootView, 0, TrendingView(), SuggestionView())
        } else {
            loadMultipleRootFragment(R.id.rootView, 0, findChildFragment(TrendingView::class.java), findChildFragment(SuggestionView::class.java))
        }
    }

    fun showSearchResult(query: String) {
        if (findChildFragment(SearchResultView::class.java) == null) {
            showHideFragment(SearchResultView.newInstance(query))
        } else {
            (findChildFragment(SearchResultView::class.java) as SearchResultView).setQuery(query)
            showHideFragment(findChildFragment(SearchResultView::class.java))
        }
    }
}