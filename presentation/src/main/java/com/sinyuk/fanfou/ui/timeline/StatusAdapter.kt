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

import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.util.Attributes
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.util.QuickSwipeAdapter
import com.sinyuk.fanfou.util.addFragmentInActivity

/**
 * Created by sinyuk on 2017/12/1.
 *
 */
class StatusAdapter : QuickSwipeAdapter<Status, BaseViewHolder>(R.layout.timeline_view_list_item, null) {

    var uniqueId: String? = null


    override fun convert(holder: BaseViewHolder, status: Status?) {
        if (status == null) {
            clear(holder)
        } else {
            bindTo(holder, status, uniqueId)
        }
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


    private fun clear(holder: BaseViewHolder) {
//            itemView.avatar.setImageDrawable(null)
//            itemView.screenName.text = null
//            itemView.content.text = null
//            itemView.createdAt.text = null
//            itemView.image.setImageDrawable(null)
//            itemView.swipeLayout.close(false)
//            itemView.surfaceView.setBackgroundColor(Color.WHITE)
//            itemView.likeButton.setImageDrawable(null)
//            itemView.deleteButton.visibility = View.GONE
    }

    private fun bindTo(holder: BaseViewHolder, status: Status, uniqueId: String?) {
        holder.getView<ImageView>(R.id.avatar).setImageResource(R.mipmap.ic_launcher_round)
        holder.getView<ImageView>(R.id.avatar).setOnClickListener {
            @Suppress("CAST_NEVER_SUCCEEDS")
            (it.context as AppCompatActivity).addFragmentInActivity(PlayerView.newInstance(status.playerExtracts?.uniqueId), R.id.fragment_container, true)
        }

        holder.setText(R.id.screenName, status.playerExtracts?.screenName)
        holder.setText(R.id.content, status.text)
        holder.setText(R.id.createdAt, status.createdAt?.toString())
        holder.setImageResource(R.id.image, R.mipmap.ic_launcher_round)
        holder.getView<SwipeLayout>(R.id.swipeLayout).isClickToClose = true

//
//        if (status.playerExtracts?.uniqueId == uniqueId || status.repostUserId == uniqueId || status.inReplyToUserId == uniqueId) {
//            itemView.surfaceView.setBackgroundColor(Color.GRAY)
//        } else {
//            itemView.surfaceView.setBackgroundColor(Color.WHITE)
//        }
//
//
//        if (status.playerExtracts?.uniqueId == uniqueId) {
//            itemView.deleteButton.visibility = View.VISIBLE
//        } else {
//            itemView.deleteButton.visibility = View.GONE
//        }
//
//
//        if (status.favorited) {
//            itemView.likeButton.setImageResource(R.mipmap.ic_launcher_round)
//        } else {
//            itemView.likeButton.setImageDrawable(null)
//        }
//
//
//        itemView.likeButton.setOnClickListener {
//            itemView.swipeLayout.close()
//        }
//        itemView.repostButton.setOnClickListener {
//            itemView.swipeLayout.close()
//        }
//        itemView.overflowButton.setOnClickListener {
//            itemView.swipeLayout.close()
//        }
//        itemView.deleteButton.setOnClickListener {
//            itemView.swipeLayout.close()
//        }

    }
}