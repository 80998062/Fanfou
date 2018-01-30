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

import android.arch.lifecycle.Observer
import android.os.Bundle
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.ui.refresh.RefreshCallback
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import kotlinx.android.synthetic.main.index_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/30.
 *
 */
class IndexView : AbstractFragment(), Injectable, RefreshCallback {


    override fun layoutId() = R.layout.index_view

    @Inject
    lateinit var factory: FanfouViewModelFactory


    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        accountViewModel.user.observe(this@IndexView, Observer {
            when (it?.states) {
                States.SUCCESS -> render(it.data)
            }
        })
    }

    private fun render(data: Player?) {
        viewAnimator.displayedChildId = if (data?.friendsCount == 0) {
            R.id.emptyLayout
        } else {
            if (findChildFragment(TimelineView::class.java) == null) {
                val fragment = TimelineView.newInstance(TIMELINE_HOME)
                fragment.refreshCallback = this@IndexView
                loadRootFragment(R.id.homeTimelineViewContainer, fragment)
            } else {
                showHideFragment(findChildFragment(TimelineView::class.java))
            }

            pullRefreshLayout.setOnRefreshListener { (findChildFragment(TimelineView::class.java) as TimelineView).refresh() }

            R.id.pullRefreshLayout
        }
    }


    override fun toggle(enable: Boolean) {
        pullRefreshLayout.isRefreshing = enable
    }

    override fun error(throwable: Throwable) {
        pullRefreshLayout.isRefreshing = false
    }
}