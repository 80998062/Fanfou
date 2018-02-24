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

package com.sinyuk.fanfou.ui.photo

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.Status
import com.sinyuk.fanfou.ui.timeline.StatusPagedListAdapter
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ScreenUtils
import kotlinx.android.synthetic.main.photo_grid_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/2/24.
 */
class PhotoGridView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.photo_grid_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
    }


    private fun setupSwipeRefresh() {
    }


    private lateinit var adapter: PhotoGridAdapter

    private var gridCount = 3
    private fun setupRecyclerView() {
        GridLayoutManager(context, gridCount, OrientationHelper.VERTICAL, false).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 10
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)


        adapter = PhotoGridAdapter(this@PhotoGridView, { timelineViewModel.retry() })

        val imageWidthPixels = ScreenUtils.getScreenWidth(context) / gridCount
        val modelPreloader = PhotoGridAdapter.PhotoPreloadProvider(adapter, this, imageWidthPixels)
        val sizePreloader = FixedPreloadSizeProvider<Status>(imageWidthPixels, imageWidthPixels)
        val preloader = RecyclerViewPreloader<Status>(Glide.with(this@PhotoGridView), modelPreloader, sizePreloader, 10)
        recyclerView.addOnScrollListener(preloader)
        recyclerView.adapter = adapter
        timelineViewModel.statuses.observe(this, pagedListConsumer)
        timelineViewModel.networkState.observe(this, networkConsumer)
    }
}