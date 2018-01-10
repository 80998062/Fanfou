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
import android.util.Log
import android.view.LayoutInflater
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.SUGGESTION_HISTORY_LIMIT
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.search.event.InputEvent
import com.sinyuk.fanfou.util.Objects
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.histyory_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/4.
 *
 */
class HistoryView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(collapsed: Boolean, query: String? = null) = HistoryView().apply {
            arguments = Bundle().apply {
                putBoolean("collapsed", collapsed)
                putString("query", query)
            }
        }
    }

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils

    private val searchViewModel by lazy { obtainViewModel(factory, SearchViewModel::class.java) }

    private var query: String? = null

    private val adapter = SuggestionAdapter()

    private val collapsed by lazy { arguments!!.getBoolean("collapsed") }

    override fun layoutId() = R.layout.histyory_view

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        setupRecyclerView()

        val limit = if (collapsed) {
            SUGGESTION_HISTORY_LIMIT
        } else {
            null
        }
        searchViewModel.setQuery(SearchViewModel.QueryParams(query = arguments!!.getString("query"), limit = limit))
        searchViewModel.listing.observe(this@HistoryView, Observer {
            it?.observe(this@HistoryView, Observer {
                adapter.setNewData(it)
                if (collapsed) showMore(it?.size ?: 0)
            })
        })
    }

    private fun showMore(size: Int) {
        if (size < SUGGESTION_HISTORY_LIMIT) {
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
            setOnClickListener {
                (activity as AbstractActivity).also {
                    it.loadRootFragment(R.id.rootFragmentContainer, HistoryManagerView.newInstance(query))
                }
                KPSwitchConflictUtil.hidePanelAndKeyboard(recyclerView)
            }
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


    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQueryChanged(event: InputEvent) {
        if (!collapsed) return
        if (BuildConfig.DEBUG) Log.d("HistoryView", "onQueryChanged: " + event.text)
        if (!Objects.equals(event.text, query)) {
            searchViewModel.setQuery(SearchViewModel.QueryParams(query = event.text, limit = SUGGESTION_HISTORY_LIMIT))
        }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        if (BuildConfig.DEBUG) Log.d("HistoryView", "Invisible")

    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        if (BuildConfig.DEBUG) Log.d("HistoryView", "Visible")
    }

}
