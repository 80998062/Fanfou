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
import com.sinyuk.fanfou.base.AbstractSwipeFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.history_manage_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/5.
 *
 */
class HistoryManagerView : AbstractSwipeFragment(), Injectable {
    override fun layoutId() = R.layout.history_manage_view

    @Inject lateinit var factory: FanfouViewModelFactory

    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)

        navBack.setOnClickListener { pop() }
        deleteButton.setOnClickListener { searchViewModel.clear() }
        loadRootFragment(R.id.historyViewContainer, HistoryView.newInstance(false))
    }


    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }
}