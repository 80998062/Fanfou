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

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.OrientationHelper
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PHOTO_SIZE
import com.sinyuk.fanfou.domain.TIMELINE_PHOTO
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ScreenUtils
import kotlinx.android.synthetic.main.photo_grid_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/2/24.
 *
 */
class PhotoGridView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(id: String? = null) = PhotoGridView().apply {
            arguments = Bundle().apply { putString("id", id) }
        }

        const val TAG = "PhotoGridView"
    }

    override fun layoutId() = R.layout.photo_grid_view
    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        var id: String? = null
        arguments?.let {
            id = it.getString("id")
        }.run { timelineViewModel.params = TimelineViewModel.TimelinePath(path = TIMELINE_PHOTO, id = id) }

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
            initialPrefetchItemCount = PHOTO_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)


        adapter = PhotoGridAdapter(this@PhotoGridView, { timelineViewModel.retry() })

        val imageWidthPixels = ScreenUtils.getScreenWidth(context) / gridCount
        val modelPreloader = PhotoGridAdapter.PhotoPreloadProvider(adapter, this, imageWidthPixels)
        val sizePreloader = FixedPreloadSizeProvider<Status>(imageWidthPixels, imageWidthPixels)
        val preloader = RecyclerViewPreloader<Status>(Glide.with(this@PhotoGridView), modelPreloader, sizePreloader, PHOTO_SIZE)
        recyclerView.addOnScrollListener(preloader)
        recyclerView.adapter = adapter
        timelineViewModel.statuses.observe(this, pagedListConsumer)
        timelineViewModel.networkState.observe(this, networkConsumer)
    }

    private val pagedListConsumer = Observer<PagedList<Status>> {
        Log.i(TAG, "PagedList has changed , size: ${it?.size}")
        // Preserves the user's scroll position if items are inserted outside the viewable area:
        val recyclerViewState = recyclerView.layoutManager.onSaveInstanceState()
        adapter.setList(it)
        recyclerView.post { recyclerView.layoutManager.onRestoreInstanceState(recyclerViewState) }
    }

    private val networkConsumer = Observer<NetworkState> {
        if (it?.status == com.sinyuk.fanfou.domain.Status.REACH_TOP) {
            // never happen
        } else {
            adapter.setNetworkState(it)
        }
    }
}