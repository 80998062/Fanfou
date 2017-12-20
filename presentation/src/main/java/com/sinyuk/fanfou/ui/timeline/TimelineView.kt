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
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view.*
import javax.inject.Inject


/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class TimelineView : AbstractLazyFragment(), Injectable {

    companion object {
        private val lock = Any()

        fun newInstance(path: String): TimelineView {
            synchronized(lock) {
                val instance = TimelineView()
                val args = Bundle()
                args.putString("path", path)
                instance.arguments = args
                return instance
            }
        }
    }


    private val timelinePath: String by lazy { arguments?.getString("path")!! }
    private var max: String? = null

    override fun layoutId(): Int? = R.layout.timeline_view

    @Inject lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    @Inject lateinit var toast: ToastUtils

    lateinit var adapter: StatusAdapter

    override fun lazyDo() {
        timelineViewModel.setPath(timelinePath)
        setupSwipeRefresh()
        setupRecyclerView()
    }

    private fun setupSwipeRefresh() {
        timelineViewModel.refreshState.observe(this, Observer {
            if (it?.status == com.sinyuk.fanfou.domain.Status.FAILED) {
                setRefresh(false)
                it.msg?.let { toast.toastShort(it) }
            } else {
                setRefresh(NetworkState.LOADING == it)
            }
        })
        swipeRefreshLayout.setOnRefreshListener { timelineViewModel.refresh() }
    }


    private fun setRefresh(constraint: Boolean) {
        swipeRefreshLayout.isRefreshing = constraint
    }

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }
        recyclerView.setHasFixedSize(true)

        adapter = StatusAdapter(Glide.with(this@TimelineView), { timelineViewModel.retry() }, { load(it) })
        recyclerView.adapter = adapter

        timelineViewModel.statuses.observe(this, Observer<PagedList<Status>> {
            adapter.setList(it)
        })

        timelineViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun load(id: String?) {
        id?.let {
            timelineViewModel.load(id).observe(this@TimelineView, Observer<Resource<Boolean>> {
                when (it?.states) {
                    States.ERROR -> it.message?.let { toast.toastShort(it) }
                    else -> {
                    }
                }
            })
        }
    }
}