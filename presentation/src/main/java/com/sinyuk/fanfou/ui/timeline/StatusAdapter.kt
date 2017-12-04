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
import android.graphics.Color
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.ui.recyclerview.DataLoadingSubject
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*
import kotlinx.android.synthetic.main.timeline_view_list_item_underlayer.view.*

/**
 * Created by sinyuk on 2017/12/1.
 */
final class StatusAdapter : PagedListAdapter<Status, RecyclerView.ViewHolder>(DIFF_CALLBACK), SwipeItemMangerInterface, SwipeAdapterInterface, DataLoadingSubject.DataLoadingCallbacks {
    override fun loadMoreStarted() {
    }

    override fun loadMoreFailed(e: Throwable) {
    }

    override fun loadMoreGone() {
    }

    override fun loadMoreFinished() {
    }

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

    lateinit var uniqueId: String

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_DATA) {
            holder as StatusViewHolder
            val status = getItem(position)
            if (status == null) {
                // Null defines a placeholder item - PagedListAdapter will automatically invalidate
                // this row when the actual object is loaded from the database
                holder.clear()
            } else {
                holder.bindTo(status, uniqueId)
            }
        } else if (holder.itemViewType == TYPE_FOOTER) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == TYPE_DATA) {
                StatusViewHolder(parent)
            } else {
                FooterViewHolder(parent)
            }


    fun addFooter() {
        if (!hasFooter) {
            hasFooter = true
            notifyItemInserted(currentList?.size ?: 0)
        }
    }

    fun removeFooter() {
        if (hasFooter) {
            hasFooter = false
            notifyItemRemoved(currentList?.size ?: 0)
        }
    }

    private var hasFooter = false

    override fun getItemCount(): Int = if (hasFooter) {
        (currentList?.size ?: 0) + 1
    } else {
        currentList?.size ?: 0
    }


    private val TYPE_FOOTER = Int.MAX_VALUE
    private val TYPE_DATA = 0
    override fun getItemViewType(position: Int): Int = if (position == currentList?.size ?: 0) {
        TYPE_FOOTER
    } else {
        TYPE_DATA
    }


    class StatusViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_view_list_item, parent, false)) {
        fun clear() {
            itemView.avatar.setImageDrawable(null)
            itemView.screenName.text = null
            itemView.content.text = null
            itemView.createdAt.text = null
            itemView.image.setImageDrawable(null)
            itemView.swipeLayout.close(false)
            itemView.surfaceView.setBackgroundColor(Color.WHITE)
            itemView.likeButton.setImageDrawable(null)
            itemView.deleteButton.visibility = View.GONE
        }

        fun bindTo(status: Status, uniqueId: String) {
            itemView.avatar.setImageResource(R.mipmap.ic_launcher_round)
            itemView.screenName.text = status.playerExtracts?.screenName
            itemView.content.text = status.text
            itemView.createdAt.text = status.createdAt?.toString()
            itemView.image.setImageResource(R.mipmap.ic_launcher_round)
            itemView.swipeLayout.isClickToClose = true


            if (status.playerExtracts?.uniqueId == uniqueId || status.repostUserId == uniqueId || status.inReplyToUserId == uniqueId) {
                itemView.surfaceView.setBackgroundColor(Color.GRAY)
            } else {
                itemView.surfaceView.setBackgroundColor(Color.WHITE)
            }


            if (status.playerExtracts?.uniqueId == uniqueId) {
                itemView.deleteButton.visibility = View.VISIBLE
            } else {
                itemView.deleteButton.visibility = View.GONE
            }


            if (status.collectorIds?.contains(uniqueId.toRegex()) == true) {
                itemView.likeButton.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                itemView.likeButton.setImageDrawable(null)
            }

            itemView.likeButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.repostButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.overflowButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.deleteButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
        }
    }


    class FooterViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_view_list_footer, parent, false))


    companion object {
        val DIFF_CALLBACK: DiffCallback<Status> = object : DiffCallback<Status>() {
            override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean = oldItem.id === newItem.id

            override fun areContentsTheSame(oldItem: Status, newItem: Status): Boolean = true
        }
    }
}