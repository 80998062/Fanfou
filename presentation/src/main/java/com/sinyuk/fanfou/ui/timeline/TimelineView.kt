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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.vo.Resource
import com.sinyuk.fanfou.domain.vo.States
import com.sinyuk.fanfou.domain.vo.Status
import com.sinyuk.fanfou.util.CustomLoadMoreView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view.*
import javax.inject.Inject


/**
 * Created by sinyuk on 2017/11/30.
 */
class TimelineView : AbstractLazyFragment(), Injectable {

    companion object {
        private val lock = Any()

        fun newInstance(path: String, playerId: String? = null): TimelineView {
            synchronized(lock) {
                val instance = TimelineView()
                val args = Bundle()
                args.putString("path", path)
                playerId?.let { args.putString("playerId", it) }
                instance.arguments = args
                return instance
            }
        }
    }


    private val timelinePath: String by lazy { arguments?.getString("path")!! }
    private val targetPlayer: String?  by lazy { arguments?.getString("playerId") }
    private var since: String? = null
    private var max: String? = null

    override fun layoutId(): Int? = R.layout.timeline_view

    @Inject lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

    private val accountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }

    @Inject lateinit var toast: ToastUtils

    private val adapter: StatusAdapter by lazy { StatusAdapter() }


    override fun lazyDo() {
        setupSwipeRefresh()
        setupRecyclerView()
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener { beforeMaxId() }
    }


    private val refreshOB: Observer<Resource<MutableList<Status>>> by lazy {
        Observer<Resource<MutableList<Status>>> { t ->
            when (t?.states) {
                States.SUCCESS -> {
                    insertBefore(t.data)
                }
                States.ERROR -> {
                    t.message?.let { toast.toastShort(it) }
                }
                States.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
                null -> TODO()
            }
            swipeRefreshLayout.isRefreshing = false
            resourceLive?.removeObserver(refreshOB)
        }
    }


    private val loadmoreOB: Observer<Resource<MutableList<Status>>> by lazy {
        Observer<Resource<MutableList<Status>>> { t ->
            when (t?.states) {
                States.SUCCESS -> {
                    isLoadMore = false
                    if (t.data?.size == PAGE_SIZE) {
                        adapter.loadMoreComplete()
                    } else {
                        adapter.loadMoreEnd(true)
                    }
                    appendAfter(t.data)
                }
                States.ERROR -> {
                    isLoadMore = false
                    adapter.loadMoreFail()
                    t.message?.let { toast.toastShort(it) }
                }
                States.LOADING -> { isLoadMore = true }
                null -> TODO()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private var resourceLive: LiveData<Resource<MutableList<Status>>>? = null

    private fun afterSinceId() {

    }

    private fun insertBefore(data: MutableList<Status>?) {
        data?.let {
            since = data.first().id
            adapter.data.addAll(0, it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun appendAfter(data: MutableList<Status>?) {
        data?.let {
            max = data.last().id
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }


    private var isLoadMore = false

    private fun beforeMaxId() {
        if (isLoadMore) return
        if (targetPlayer == null) {
            when (timelinePath) {
                TIMELINE_HOME -> resourceLive = accountViewModel.loadmore(max).apply { observe(this@TimelineView, loadmoreOB) }
                else -> TODO()
            }.run {
//                resourceLive?.removeObserver(loadmoreOB)
            }

        } else {
            TODO()
        }
    }


    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }
        recyclerView.setHasFixedSize(true)

        adapter.apply {
            setHeaderFooterEmpty(false, false)
            setLoadMoreView(CustomLoadMoreView())
            setEnableLoadMore(true)
            disableLoadMoreIfNotFullPage(recyclerView)
            setOnLoadMoreListener({ beforeMaxId() }, recyclerView)
            recyclerView.adapter = this
        }

    }


}