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

import android.arch.paging.PagedListAdapter
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.fanfou.ui.NetworkStateItemViewHolder
import java.util.*

/**
 * Created by sinyuk on 2018/1/15.
 *
 */
class PlayerPagedListAdapter(fragment: Fragment, private val retryCallback: () -> Unit, private val path: String) : PagedListAdapter<Player, RecyclerView.ViewHolder>(PlayerPagedListAdapter.COMPARATOR) {


    override fun getItemViewType(position: Int) = if (hasExtraRow() && position == itemCount - 1) {
        R.layout.network_state_item
    } else {
        R.layout.player_list_item
    }


    override fun getItemCount() = super.getItemCount() + if (hasExtraRow()) 1 else 0

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            TODO()
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.player_list_item -> {
                holder as PlayerItemViewHolder
                if (getItem(position) == null) {
                    holder.clear()
                } else {
                    holder.bind(getItem(position)!!)
                }
            }
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.player_list_item -> PlayerItemViewHolder.create(parent, glide)
        R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback, path)
        else -> throw IllegalArgumentException("unknown view type $viewType")
    }

    private var networkState: NetworkState? = null
    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    private val glide: GlideRequests = GlideApp.with(fragment)

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<Player>() {
            override fun areContentsTheSame(oldItem: Player, newItem: Player) = true
            override fun areItemsTheSame(oldItem: Player, newItem: Player) = oldItem.id == newItem.id
        }
    }


    class PlayerPreloadProvider constructor(private val adapter: PlayerPagedListAdapter, private val fragment: Fragment, private val imageWidthPixels: Int) : ListPreloader.PreloadModelProvider<Player> {

        override fun getPreloadRequestBuilder(item: Player): RequestBuilder<*>? {
            return GlideApp.with(fragment).asBitmap().load(item.profileImageUrlLarge).avatar().override(imageWidthPixels, imageWidthPixels).transition(withCrossFade())
        }

        override fun getPreloadItems(position: Int): MutableList<Player> = if (adapter.currentList?.isNotEmpty() == true && position < adapter.currentList?.size ?: 0) {
            val player = adapter.currentList!![position]
            if (player == null) {
                Collections.emptyList<Player>()
            } else {
                Collections.singletonList(player)
            }
        } else {
            Collections.emptyList<Player>()
        }
    }
}