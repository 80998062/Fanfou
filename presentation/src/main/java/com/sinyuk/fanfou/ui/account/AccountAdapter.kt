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

package com.sinyuk.fanfou.ui.account

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.SmoothCheckBox
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.util.QuickAdapter
import kotlinx.android.synthetic.main.account_list_item.view.*

/**
 * Created by sinyuk on 2018/1/31.
 *
 */
class AccountAdapter(var uniqueId: String) : QuickAdapter<Player, AccountViewHolder>(null), SwipeItemMangerInterface, SwipeAdapterInterface {
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

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int) = AccountViewHolder.create(parent!!, uniqueId)

    var checked = RecyclerView.NO_POSITION

    override fun convert(helper: AccountViewHolder, item: Player) {
        helper.addOnClickListener(R.id.deleteButton)
        if (item.uniqueId == uniqueId) {
            checked = helper.adapterPosition
            helper.itemView.swipeLayout.isSwipeEnabled = false
        } else {
            helper.itemView.swipeLayout.isSwipeEnabled = true
        }

        helper.itemView.checkbox.setChecked(uniqueId == item.uniqueId, false)

        helper.itemView.checkbox.setOnClickListener { v ->
            v as SmoothCheckBox
            if (!v.isChecked) {
                v.setChecked(true, true)
                uniqueId = item.uniqueId
                helper.itemView.swipeLayout.isSwipeEnabled = false
                Log.i("onSwitch", "from $checked to ${helper.adapterPosition}")
                if (checked != RecyclerView.NO_POSITION) notifyItemChanged(checked)
                checked = helper.adapterPosition
                listener?.onSwitch(item.uniqueId)
            }
        }
        helper.bind(item)
    }

    var listener: AccountListView.OnAccountListListener? = null
}