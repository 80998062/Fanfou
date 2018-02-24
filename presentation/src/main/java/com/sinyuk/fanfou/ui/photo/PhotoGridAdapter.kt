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

import android.arch.paging.PagedListAdapter
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.photo_grid_list_item.view.*
import java.util.*

/**
 * Created by sinyuk on 2018/2/24.
 */
class PhotoGridAdapter(private val fragment: Fragment,
                       private val retryCallback: () -> Unit) : PagedListAdapter<Status, RecyclerView.ViewHolder>(PhotoGridAdapter.COMPARATOR) {

    private var networkState: NetworkState? = null
    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED
    private val glide: GlideRequests = GlideApp.with(fragment)

    //
    private val initialGifBadgeColor = ContextCompat.getColor(fragment.context!!, R.color.scrim)


    override fun getItemViewType(position: Int) = if (hasExtraRow() && position == itemCount - 1) {
        R.layout.network_state_item_photo_grid
    } else {
        R.layout.photo_grid_list_item
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int, payloads: MutableList<Any>?) {
        if (payloads?.isNotEmpty() == true) {
            TODO()
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.photo_grid_list_item -> PhotoGridItemHolder.create(parent, glide, fragment)
        R.layout.network_state_item_photo_grid -> NetwordStateGridViewHolder.create(parent, retryCallback)
        else -> throw IllegalArgumentException("unknown view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            R.layout.photo_grid_list_item -> {
                holder as PhotoGridItemHolder
                if (getItem(position) == null) {
                    holder.clear()
                } else {
                    val status = getItem(position)!!
                    holder.bind(status)
                }
            }
            R.layout.network_state_item_photo_grid -> (holder as NetwordStateGridViewHolder).bind(networkState)
        }
    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        if (holder is PhotoGridItemHolder) {
            holder.itemView.image.setBadgeColor(initialGifBadgeColor)
            holder.itemView.image.drawBadge = false
        } else {
            super.onViewRecycled(holder)
        }
    }

    companion object {
        val COMPARATOR = object : DiffCallback<Status>() {
            override fun areContentsTheSame(oldItem: Status, newItem: Status) = true
            override fun areItemsTheSame(oldItem: Status, newItem: Status) = oldItem.id == newItem.id
        }
    }


    class PhotoPreloadProvider constructor(private val adapter: PhotoGridAdapter, private val fragment: Fragment, private val imageWidthPixels: Int) :
            ListPreloader.PreloadModelProvider<Status> {

        override fun getPreloadRequestBuilder(item: Status): RequestBuilder<*>? {
            val url = item.photos?.size(ConvertUtils.dp2px(fragment.context, Photos.SMALL_SIZE))
            return GlideApp.with(fragment).load(url).override(imageWidthPixels, imageWidthPixels)
        }

        override fun getPreloadItems(position: Int): MutableList<Status> = if (adapter.currentList?.isNotEmpty() == true && position < adapter.currentList?.size ?: 0) {
            val status = adapter.currentList!![position]
            if (status == null) {
                Collections.emptyList<Status>()
            } else {
                val url = status.photos?.size(ConvertUtils.dp2px(fragment.context, Photos.SMALL_SIZE))
                if (url == null) {
                    Collections.emptyList<Status>()
                } else {
                    Collections.singletonList(status)
                }
            }

        } else {
            Collections.emptyList<Status>()
        }
    }
}