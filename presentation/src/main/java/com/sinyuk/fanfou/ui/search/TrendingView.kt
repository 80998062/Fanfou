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

import android.arch.lifecycle.Observer
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import com.sinyuk.fanfou.ui.QMUIRoundButtonDrawable
import com.sinyuk.fanfou.ui.refresh.RefreshCallback
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view_list_header_public.*
import kotlinx.android.synthetic.main.trending_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/9.
 *
 */
class TrendingView : AbstractFragment(), Injectable, RefreshCallback {


    override fun layoutId() = R.layout.trending_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    @Inject
    lateinit var toast: ToastUtils


    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST)

        setupTrendList()

        searchViewModel.trends().observe(this@TrendingView, Observer {
            when (it?.states) {
                States.SUCCESS -> {
                    adapter.setNewData(it.data)
                }
                States.ERROR -> {
                    it.message?.let { toast.toastShort(it) }
                }
                States.LOADING -> {

                }
            }
//            if (findChildFragment(TimelineView::class.java) == null) {
//                val fragment = TimelineView.newInstance(TIMELINE_PUBLIC)
//                fragment.refreshCallback = this@TrendingView
//                loadRootFragment(R.id.publicViewContainer, fragment)
//                refreshButton.setOnClickListener {
//                    toggleRefreshButton(false)
//                    findChildFragment(TimelineView::class.java)?.refresh()
//                }
//            } else {
//                showHideFragment(findChildFragment(TimelineView::class.java))
//            }
        })
    }

    private fun toggleRefreshButton(enable: Boolean) {
        refreshButton.isEnabled = enable
        if (enable) {
            (refreshButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorAccent))
        } else {
            (refreshButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlDisable))
        }
    }

    private lateinit var adapter: TrendAdapter
    private lateinit var header: View

    private fun setupTrendList() {
        object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }.apply {
            isAutoMeasureEnabled = true
            trendList.layoutManager = this
        }

        trendList.setHasFixedSize(true)
        trendList.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))
        header = LayoutInflater.from(context).inflate(R.layout.trend_list_header, trendList, false)

        adapter = TrendAdapter().apply {
            addHeaderView(header)
            setOnItemClickListener { _, _, _ -> }
            trendList.adapter = this
        }
    }


    override fun toggle(enable: Boolean) {
        toggleRefreshButton(!enable)
    }

    override fun error(throwable: Throwable) {
        toggleRefreshButton(false)
    }
}