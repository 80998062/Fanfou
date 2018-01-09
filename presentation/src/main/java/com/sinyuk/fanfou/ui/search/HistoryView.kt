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
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.histyory_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/4.
 *
 */
class HistoryView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(collapsed: Boolean) = HistoryView().apply {
            arguments = Bundle().apply { putBoolean("collapsed", collapsed) }
        }
    }

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils

    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }


    private val adapter = SuggestionAdapter()

    private val collapsed by lazy { arguments!!.getBoolean("collapsed") }

    override fun layoutId() = R.layout.histyory_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        if (collapsed) {
            searchViewModel.listing(5)
        } else {
            searchViewModel.listing()
        }.observe(this@HistoryView, Observer {
            adapter.setNewData(it)
            if (collapsed) {
                showMore(it?.size ?: 0)
            }
        })
    }

    private fun showMore(size: Int) {
        if (size <= 5) {
            if (adapter.footerLayoutCount != 0) {
                adapter.removeFooterView(footer!!)
            }
        } else {
            if (adapter.footerLayoutCount == 0) {
                adapter.addFooterView(footer)
            }
        }
    }

    private val footer by lazy {
        LayoutInflater.from(context).inflate(R.layout.suggestion_list_footer, recyclerView, false).apply {
            setOnClickListener { (activity as AbstractActivity).loadRootFragment(R.id.rootFragmentContainer, HistoryManagerView()) }
        }
    }

    private fun setupRecyclerView() {
        if (collapsed) {
            recyclerView.layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }.apply { isAutoMeasureEnabled = true }
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context).apply { isAutoMeasureEnabled = true }
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))
        if (collapsed) {
            LayoutInflater.from(context).inflate(R.layout.suggestion_list_header, recyclerView, false).apply {
                adapter.addHeaderView(this)
            }
        } else {
            LayoutInflater.from(context).inflate(R.layout.suggestion_list_footer_swipe, recyclerView, false).apply {
                adapter.addFooterView(this)
            }
        }
        adapter.setHeaderFooterEmpty(false, false)
        LayoutInflater.from(context).inflate(R.layout.suggestion_list_empty, recyclerView, false).apply {
            adapter.emptyView = this
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.deleteButton -> {
                    adapter.closeItem(position)
                    adapter.getItem(position)?.let { searchViewModel.delete(it.query) }
                }
            }
        }
        recyclerView.adapter = adapter
    }


}
