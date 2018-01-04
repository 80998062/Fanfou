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

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.suggestion_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
class SuggestionView : AbstractFragment(), Injectable {


    override fun layoutId() = R.layout.suggestion_view

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils

    private val seachViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }


    val adapter = KeywordAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()


//        if (collapsed) {
//            seachViewModel.listing()
//        } else {
//            seachViewModel.listing()
//        }.asLiveData().observe(this@SuggestionView, Observer {
//            when (it?.states) {
//                States.SUCCESS -> {
//                    adapter.setNewData(it.data)
//                }
//                States.ERROR -> {
//                    it.message?.let { toast.toastShort(it) }
//                }
//                States.LOADING -> {
//
//                }
//            }
//        })
    }

    var footer: View? = null

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }
        recyclerView.setHasFixedSize(true)

        val header = LayoutInflater.from(context).inflate(R.layout.keyword_list_header, recyclerView, false)
        adapter.addHeaderView(header)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter.itemCount == 5) {
                    if (footer == null) {
                        footer = View.inflate(context, R.layout.keyword_list_footer, recyclerView)
                    }
                    adapter.addFooterView(footer)
                } else {
                    footer?.let { adapter.removeFooterView(it) }
                }
            }
        })


        recyclerView.adapter = adapter
    }
}