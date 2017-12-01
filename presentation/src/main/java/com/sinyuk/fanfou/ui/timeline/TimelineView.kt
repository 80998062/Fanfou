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
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstractLazyFragment
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.injections.Injectable
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
        fun newInstance(type: String? = TIMELINE_HOME): TimelineView {
            val instance = TimelineView()
            val args = Bundle()
            args.putString("type", type)
            instance.arguments = args
            return instance
        }
    }

    override fun layoutId(): Int? = R.layout.timeline_view

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var timelineViewModel: TimelineViewModel

    @Inject lateinit var toast: ToastUtils

    var adapter: StatusAdapter? = null

    override fun lazyDo() {
        timelineViewModel = obtainViewModel(factory, TimelineViewModel::class.java).apply {
            accountRelay.observe(this@TimelineView, Observer<String> {
                Log.d(TimelineView::class.java.simpleName, it)

                setupSwipeRefresh(it!!)
                setupRecyclerView(it)
                afterSinceId(it)

            })
        }

    }

    private fun setupSwipeRefresh(uniqueId: String) {
        swipeRefreshLayout.setOnRefreshListener { afterSinceId(uniqueId) }
    }

    private fun afterSinceId(id: String) {
        val d = timelineViewModel.fetchTimeline(arguments?.getString("type")!!, id, since, null)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { swipeRefreshLayout.isRefreshing = false }
                .subscribeWith(object : SingleHanlder<List<Status>>(toast) {
                    override fun onSuccess(t: List<Status>) {
                        super.onSuccess(t)
                    }
                })
        addDisposable(d)
    }


    private fun beforeMaxId(id: String) {
        val d = timelineViewModel.fetchTimeline(arguments?.getString("type")!!, id, null, max)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : SingleHanlder<List<Status>>(toast) {
                    override fun onSuccess(t: List<Status>) {
                        super.onSuccess(t)

                    }
                })
        addDisposable(d)
    }


    private var liveTimeline: LiveData<PagedList<Status>>? = null

    private fun setupRecyclerView(uniqueId: String) {
        if (recyclerView.layoutManager == null) {
            val lm = LinearLayoutManager(context)
            lm.initialPrefetchItemCount = 10
            lm.isAutoMeasureEnabled = true
            recyclerView.layoutManager = lm
            recyclerView.setHasFixedSize(true)
        }

        if (adapter == null) {
            adapter = StatusAdapter()
        }

        liveTimeline?.removeObserver(timelineOB)
        liveTimeline = timelineViewModel.timeline(uniqueId).apply { observe(this@TimelineView, timelineOB) }

        if (recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }
    }

    private val timelineOB: Observer<PagedList<Status>> = Observer { t -> adapter?.setList(t) }

    private var since: String? = null
    private var max: String? = null

}