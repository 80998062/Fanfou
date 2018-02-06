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

package com.sinyuk.fanfou.ui.player

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.isOnline
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import kotlinx.android.synthetic.main.player_list_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/10.
 *
 */
class PlayerListView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(path: String, uniqueId: String? = null, forceUpdated: Boolean = true) = PlayerListView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("uniqueId", uniqueId)
                putBoolean("forceUpdated", forceUpdated)
            }
        }
    }

    override fun layoutId() = R.layout.player_list_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val playerViewModel by lazy { obtainViewModel(factory, PlayerViewModel::class.java) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        playerViewModel.setParams(PlayerViewModel.PlayerPath(path, uniqueId))
        setupRecyclerView()
    }

    lateinit var adapter: PlayerPagedListAdapter

    private val forceUpdated by lazy { arguments!!.getBoolean("forceUpdated") }
    private val uniqueId by lazy { arguments!!.getString("uniqueId") }
    private val path by lazy { arguments!!.getString("path") }

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        val cached = if (uniqueId == null) {
            !(forceUpdated && isOnline(activity!!.application))
        } else {
            false
        }

        adapter = if (cached) {
            PlayerPagedListAdapter(this@PlayerListView, {},path)
        } else {
            PlayerPagedListAdapter(this@PlayerListView, { playerViewModel.retry() }, path)
        }

        val imageWidthPixels = resources.getDimensionPixelSize(R.dimen.player_list_item_avatar_size)
        val modelPreloader = PlayerPagedListAdapter.PlayerPreloadProvider(adapter, this, imageWidthPixels)
        val sizePreloader = FixedPreloadSizeProvider<Player>(imageWidthPixels, imageWidthPixels)
        val preloader = RecyclerViewPreloader<Player>(Glide.with(this@PlayerListView), modelPreloader, sizePreloader, 10)
        recyclerView.addOnScrollListener(preloader)

        recyclerView.adapter = adapter

        if (cached) {
            playerViewModel.cached(path = path).observe(this@PlayerListView, Observer { adapter.setList(it) })
        } else {  // network
            playerViewModel.players.observe(this@PlayerListView, Observer { adapter.setList(it) })
            playerViewModel.networkState.observe(this@PlayerListView, Observer { adapter.setNetworkState(it) })
        }

        playerViewModel.setParams(PlayerViewModel.PlayerPath(path, uniqueId))
    }

}