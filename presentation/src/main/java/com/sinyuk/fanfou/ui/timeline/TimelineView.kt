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

package com.sinyuk.fanfou.ui.timeline

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.refresh.RefreshCallback
import com.sinyuk.fanfou.util.Objects
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view.*
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class TimelineView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(path: String, id: String? = null) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("id", id)
            }
        }

        fun search(path: String, query: String, id: String? = null) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("query", query)
                putString("id", id)
            }
        }
    }

    override fun layoutId(): Int? = R.layout.timeline_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

//    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    @Inject
    lateinit var toast: ToastUtils

    private lateinit var timelinePath: String
    private var id: String? = null
    private var query: String? = null


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        arguments?.let {
            timelinePath = it.getString("path")
            id = it.getString("id")
            query = it.getString("query")
        }.run {
                    timelineViewModel.setParams(TimelineViewModel.TimelinePath(path = timelinePath, id = id, query = query))
                }

        setupRecyclerView()
        setupSwipeRefresh()
    }


    var refreshCallback: RefreshCallback? = null

    private fun setupSwipeRefresh() {
        timelineViewModel.refreshState.observe(this@TimelineView, Observer {
            val refresh = it?.status == com.sinyuk.fanfou.domain.Status.RUNNING
            refreshCallback?.toggle(refresh)
            if (it?.status == com.sinyuk.fanfou.domain.Status.FAILED) {
                it.msg?.let { refreshCallback?.error(Throwable(it)) }
            }
        })
    }

    fun search(q: String) {
        if (Objects.equals(q, query)) {
            // TODO
        } else {
            query = q
            timelineViewModel.setParams(TimelineViewModel.TimelinePath(path = timelinePath, id = id, query = query))
            timelineViewModel.refresh()
        }
    }

    fun refresh() {
        timelineViewModel.refresh()
    }


    private lateinit var adapter: StatusPagedListAdapter

    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var sharedPreferences: SharedPreferences

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        adapter = StatusPagedListAdapter(this@TimelineView, { timelineViewModel.retry() }, sharedPreferences.getString(UNIQUE_ID, null))

        val imageWidthPixels = resources.getDimensionPixelSize(R.dimen.timeline_illustration_size)
        val modelPreloader = StatusPagedListAdapter.StatusPreloadProvider(adapter, this, imageWidthPixels)
        val sizePreloader = FixedPreloadSizeProvider<Status>(imageWidthPixels, imageWidthPixels)
        val preloader = RecyclerViewPreloader<Status>(Glide.with(this@TimelineView), modelPreloader, sizePreloader, 10)
        recyclerView.addOnScrollListener(preloader)

        recyclerView.adapter = adapter

        timelineViewModel.statuses.observe(this, pagedListConsumer)
        timelineViewModel.networkState.observe(this, networkConsumer)
    }


    private val pagedListConsumer = Observer<PagedList<Status>> {
        // record the last scroll position
        val lastPos = (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        adapter.setList(it)
        // TODO: Unsupported, can this be less tricky?
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                recyclerView.removeOnLayoutChangeListener(this)
                if (lastPos == RecyclerView.NO_POSITION) recyclerView.scrollToPosition(0)
            }
        })
    }

    private val networkConsumer = Observer<NetworkState> {
        if (it?.status == com.sinyuk.fanfou.domain.Status.REACH_TOP) {
            // never happen
        } else {
            adapter.setNetworkState(it)
        }
    }


}