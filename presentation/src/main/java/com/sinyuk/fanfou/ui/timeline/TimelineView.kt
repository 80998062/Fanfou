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
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstractLazyFragment
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.injections.Injectable
import com.sinyuk.fanfou.utils.CustomLoadMoreView
import com.sinyuk.fanfou.utils.SingleHanlder
import com.sinyuk.fanfou.utils.obtainViewModel
import com.sinyuk.fanfou.viewmodels.ViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.timeline_view.*
import javax.inject.Inject


/**
 * Created by sinyuk on 2017/11/30.
 */
class TimelineView : AbstractLazyFragment(), Injectable {

    companion object {
        private val lock = Any()

        fun newInstance(path: String, playerId: String?): TimelineView {
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


    private lateinit var timelinePath: String
    private var targetPlayer: String? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        timelinePath = arguments?.getString("path")!!
        targetPlayer = arguments?.getString("playerId")
    }

    override fun layoutId(): Int? = R.layout.timeline_view

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var timelineViewModel: TimelineViewModel

    @Inject lateinit var toast: ToastUtils

    private val adapter: StatusAdapter? by lazy { StatusAdapter() }


    private var uniqueId: String? = null
    override fun lazyDo() {

        setupSwipeRefresh()
        setupRecyclerView()

        timelineViewModel = obtainViewModel(factory, TimelineViewModel::class.java).apply {
            accountRelay.observe(this@TimelineView, Observer<String> {
                Log.d(TimelineView::class.java.simpleName, it)
                if (it !== uniqueId) { // 当用户账号发生更新的时候重新订阅
                    subscribeLiveData(it!!)
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener { afterSinceId() }
    }


    private fun afterSinceId() {
        timelineViewModel.fetchTimeline(timelinePath, targetPlayer, since, null)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { swipeRefreshLayout.isRefreshing = false }
                .subscribeWith(object : SingleHanlder<List<Status>>(toast) {
                    override fun onSuccess(t: List<Status>) {
                        super.onSuccess(t)
                        if (t.isNotEmpty()) {
                            toast.toastShort(String.format(getString(R.string.status_fetch_succeed_format), t.size))
                        } else {
                            toast.toastShort(R.string.status_fetch_no_new_data)
                        }
                    }
                })
                .apply { addDisposable(this) }
    }

    private var isLoadMoreEnd = false

    private fun beforeMaxId() {
        if (isLoadMoreEnd) {
            return
        }

        timelineViewModel.fetchTimeline(timelinePath, targetPlayer, null, max)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : SingleHanlder<List<Status>>(toast) {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        adapter?.loadMoreFail()
                    }

                    override fun onSuccess(t: List<Status>) {
                        super.onSuccess(t)
                        if (t.size < PAGE_SIZE) {
                            isLoadMoreEnd = true
                            adapter?.loadMoreEnd(false)
                        } else {
                            adapter?.loadMoreComplete()
                        }
                    }
                })
                .apply { addDisposable(this) }
    }


    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }
        recyclerView.setHasFixedSize(true)

        adapter?.apply {
            setHeaderFooterEmpty(false, false)
            setLoadMoreView(CustomLoadMoreView())
            setEnableLoadMore(true)
            disableLoadMoreIfNotFullPage(recyclerView)
            setOnLoadMoreListener({ beforeMaxId() }, recyclerView)
            recyclerView.adapter = this
        }

    }

    private var liveTimeline: LiveData<MutableList<Status>>? = null

    private fun subscribeLiveData(it: String) {
        uniqueId = it
        adapter?.uniqueId = it
        since = null
        max = null
        liveTimeline?.removeObservers(this@TimelineView)
        liveTimeline = timelineViewModel.timeline(timelinePath, targetPlayer).apply {
            observe(this@TimelineView, timelineOB)
        }

        afterSinceId()
    }

    private val timelineOB: Observer<MutableList<Status>> = Observer { t ->
        if (t?.isNotEmpty() == true) {
            since = t.first().id
            max = t.last().id
        }
        adapter?.setNewData(t)
    }

    private var since: String? = null
    private var max: String? = null

}