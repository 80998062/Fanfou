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
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.ui.CustomLoadMoreView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.StatusesViewModel
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.statuses_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/21.
 *
 */
class StatusesView : AbstractLazyFragment(), Injectable {

    override fun layoutId() = R.layout.statuses_view

    companion object {
        fun newInstance(path: String, uniqueId: String) = StatusesView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("uniqueId", uniqueId)
            }
        }
    }

    lateinit var uniqueId: String

    lateinit var path: String

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        uniqueId = arguments!!.getString("uniqueId")
        path = arguments!!.getString("path")
    }

    override fun lazyDo() {
        setupRecyclerView()
        statusesViewModel.listing.observe(this@StatusesView, Observer {
            when (it?.states) {
                States.SUCCESS -> {
                    if (it.data == null) {
                        adapter.loadMoreEnd(false)
                    } else {
                        adapter.loadMoreComplete()
                        adapter.setNewData(it.data)
                    }
                }
                States.LOADING -> {

                }
                States.ERROR -> {
                    adapter.loadMoreFail()
                }
            }
        })

        refresh()
    }

    private lateinit var adapter: StatusAdapter

    @Inject lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModelFromActivity(factory, TimelineViewModel::class.java) }
    private val statusesViewModel by lazy { obtainViewModel(factory, StatusesViewModel::class.java) }

    @Inject lateinit var toast: ToastUtils

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }
        recyclerView.setHasFixedSize(true)
        adapter = StatusAdapter(Glide.with(this@StatusesView), uniqueId)
        adapter.setLoadMoreView(CustomLoadMoreView())
        adapter.setOnLoadMoreListener({ fetchAfterTop() }, recyclerView)
//        adapter.disableLoadMoreIfNotFullPage(recyclerView)

        recyclerView.adapter = adapter
    }


    private fun refresh() {
        statusesViewModel.refresh(path = path, uniqueId = uniqueId, owner = this@StatusesView)

    }

    private fun fetchAfterTop() {
        statusesViewModel.fetchAfterTop(path = path, uniqueId = uniqueId, max = adapter.data.last().id, owner = this@StatusesView)
    }
}