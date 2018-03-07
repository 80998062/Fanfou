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

import android.arch.paging.PagedListAdapter
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.fanfou.ui.NetworkStateItemViewHolder
import com.sinyuk.fanfou.ui.QuickSwipeListener
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*
import kotlinx.android.synthetic.main.timeline_view_list_item_underlayer.view.*
import java.util.*

/**
 * Created by sinyuk on 2017/12/18.
 *
 * Adapter implementation that shows status.
 */
class StatusPagedListAdapter(
        fragment: Fragment,
        private val retryCallback: () -> Unit,
        private val uniqueId: String,
        private val path: String) : PagedListAdapter<Status, RecyclerView.ViewHolder>(COMPARATOR), SwipeItemMangerInterface, SwipeAdapterInterface {
    private var networkState: NetworkState? = null
    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    private val glide: GlideRequests = GlideApp.with(fragment)


    override fun getItemViewType(position: Int) = if (hasExtraRow() && position == itemCount - 1) {
        R.layout.network_state_item
    } else {
        R.layout.timeline_view_list_item
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.timeline_view_list_item -> StatusViewHolder.create(parent, glide, uniqueId)
        R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback, path)
        else -> throw IllegalArgumentException("unknown view type $viewType")
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
            R.layout.timeline_view_list_item -> {
                holder as StatusViewHolder
                if (getItem(position) == null) {
                    holder.clear()
                } else {
                    val status = getItem(position)!!
                    holder.bind(status)
                    if (uniqueId == status.playerExtracts?.uniqueId) {
                        glide.load(R.drawable.ic_empty).into(holder.itemView.actionButton)
                        holder.itemView.actionButton.setOnClickListener { v ->
                            statusOperationListener?.onDeleted(v, position, status)
                        }
                    } else {

                        holder.itemView.actionButton.setImageResource(R.drawable.trimclip_heart)
                        val checked = status.favorited
                        val stateSet = intArrayOf(android.R.attr.state_checked * if (checked) 1 else -1)
                        holder.itemView.actionButton.setImageState(stateSet, true)

                        @Suppress("NAME_SHADOWING")
                        holder.itemView.actionButton.setOnClickListener { _ ->
                            val checked = !status.favorited
                            setFavorited(checked, holder)
                            statusOperationListener?.onFavorited(checked, holder.itemView, holder.adapterPosition, status)
                        }
                    }

                    //

                    holder.itemView.swipeLayout.addSwipeListener(object : QuickSwipeListener() {
                        override fun onClose(layout: SwipeLayout?) {
                        }

                        override fun onOpen(layout: SwipeLayout?) {
                            mItemManger.closeAllExcept(layout)
                        }
                    })
                }
            }
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }


    private fun setFavorited(checked: Boolean, holder: StatusViewHolder) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (checked) 1 else -1)
        holder.itemView?.actionButton?.setImageState(stateSet, true)
        getItem(holder.adapterPosition)?.favorited = checked
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<Status>() {
            override fun areContentsTheSame(oldItem: Status, newItem: Status) = newItem.favorited == oldItem.favorited
            override fun areItemsTheSame(oldItem: Status, newItem: Status) = oldItem.id == newItem.id
        }
    }

    var statusOperationListener: StatusOperationListener? = null

    interface StatusOperationListener {
        fun onFavorited(favorited: Boolean, v: View?, p: Int, status: Status)

        fun onDeleted(v: View?, p: Int, status: Status)

    }

    /**
     * Swipe implementing
     */

    private var mItemManger = SwipeItemRecyclerMangerImpl(this)

    override fun openItem(position: Int) {
        mItemManger.openItem(position)
    }

    override fun closeItem(position: Int) {
        mItemManger.closeItem(position)
    }

    override fun closeAllExcept(layout: SwipeLayout) {
        mItemManger.closeAllExcept(layout)
    }

    override fun closeAllItems() {
        mItemManger.closeAllItems()
    }

    override fun getOpenItems(): List<Int> {
        return mItemManger.openItems
    }

    override fun getOpenLayouts(): List<SwipeLayout> {
        return mItemManger.openLayouts
    }

    override fun removeShownLayouts(layout: SwipeLayout) {
        mItemManger.removeShownLayouts(layout)
    }

    override fun isOpen(position: Int): Boolean {
        return mItemManger.isOpen(position)
    }

    override fun getMode(): Attributes.Mode {
        return mItemManger.mode
    }

    override fun setMode(mode: Attributes.Mode) {
        mItemManger.mode = mode
    }

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipeLayout


    class StatusPreloadProvider constructor(private val adapter: StatusPagedListAdapter, private val fragment: Fragment, private val imageWidthPixels: Int) : ListPreloader.PreloadModelProvider<Status> {

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