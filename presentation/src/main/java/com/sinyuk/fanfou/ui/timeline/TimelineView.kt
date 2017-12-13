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
import com.chad.library.adapter.base.BaseQuickAdapter
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.repo.FetchTimelineTask
import com.sinyuk.fanfou.util.CustomLoadMoreView
import com.sinyuk.fanfou.util.Objects
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

    private val accountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }

    @Inject lateinit var toast: ToastUtils

    private val adapter: StatusAdapter by lazy { StatusAdapter() }


    override fun lazyDo() {
        setupSwipeRefresh()
        setupRecyclerView()

        loadMoreAfter()
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener { fetchNew() }
    }


    private var resourceLive: LiveData<Resource<MutableList<Status>>>? = null

    private fun fetchNew() {
        if (isLoading) return
        isLoading = true
        val offset = if (offsetBroken) {
            max
        } else {
            null
        }
        resourceLive = timelineViewModel.fetchTimelineAndFiltered(timelinePath, offset).apply { observe(this@TimelineView, fetchNewOB) }
    }


    private var isLoading = true
    private var isLoadmoreEnd = false
    private fun loadMoreAfter() {
        if (isLoading || isLoadmoreEnd) return
        isLoading = true
        resourceLive = if (offsetBroken) {
            timelineViewModel.fetchTimelineAndFiltered(timelinePath, max).apply { observe(this@TimelineView, fetchNewOB) }
        } else {
            timelineViewModel.loadTimelineFromDb(timelinePath, max).apply { observe(this@TimelineView, loadmoreOB) }
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
            setOnLoadMoreListener({ loadMoreAfter() }, recyclerView)
            recyclerView.adapter = this
            onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                }
            }
        }

    }

    private var offsetBroken = false
    private val fetchNewOB: Observer<Resource<MutableList<Status>>> by lazy {
        Observer<Resource<MutableList<Status>>> { t ->
            resourceLive?.removeObserver(fetchNewOB)
            when (t?.states) {
                States.SUCCESS -> {
                    offsetBroken = false
                    if (t.data?.isNotEmpty() == true) {
                        adapter.data.addAll(0, t.data!!)
                        adapter.notifyItemRangeInserted(0, t.data!!.size)
                    } else {
                        toast.toastShort(R.string.no_new_statuses)
                    }
                }
                States.ERROR -> {
                    offsetBroken = if (Objects.equals(t.message, FetchTimelineTask.HAS_BREAK_POINT)) { // 新的数据超过一页
                        adapter.data.clear()
                        adapter.data.addAll(t.data!!)
                        adapter.notifyDataSetChanged()
                        true
                    } else {
                        t.message?.let { toast.toastShort(it) }
                        false
                    }
                }
                States.LOADING -> {
                    setRefresh(true)
                }
                null -> TODO()
            }
            isLoading = false
            setRefresh(false)
            if (t.data?.isNotEmpty() == true) {
                max = adapter.data.last().id
            }
        }
    }

    private fun setRefresh(b: Boolean) {
        swipeRefreshLayout.isRefreshing = b
    }


    private val loadmoreOB: Observer<Resource<MutableList<Status>>> by lazy {
        Observer<Resource<MutableList<Status>>> { t ->
            resourceLive?.removeObserver(loadmoreOB)
            when (t?.states) {
                States.SUCCESS -> {
                    isLoading = false
                    isLoadmoreEnd = if (t.data?.size == PAGE_SIZE) {
                        adapter.loadMoreComplete()
                        adapter.addData(t.data!!) // no need to notify
                        max = t.data!!.last().id
                        false
                    } else {
                        adapter.loadMoreEnd(true)
                        if (t.data?.isNotEmpty() == true) {
                            adapter.addData(t.data!!)
                            max = t.data!!.last().id
                        }
                        true
                    }
                }
                States.ERROR -> {
                    isLoading = false
                    adapter.loadMoreFail()
                    t.message?.let { toast.toastShort(it) }
                }
                States.LOADING -> {
                }
                null -> TODO()
            }
        }
    }


}