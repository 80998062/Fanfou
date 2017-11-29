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

package com.sinyuk.fanfou.utils

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface
import com.daimajia.swipe.util.Attributes

/**
 * @author sinyuk
 * @date 2017/10/25
 */

abstract class QuickSwipeAdapter<T, K : BaseViewHolder> : BaseQuickAdapter<T, K>, SwipeItemMangerInterface, SwipeAdapterInterface {

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


    constructor(layoutResId: Int, data: List<T>?) : super(layoutResId, data)

    constructor(data: List<T>?) : super(data)

    constructor(layoutResId: Int) : super(layoutResId)


    override fun onBindViewHolder(holder: K, position: Int, payloads: List<Any>?) {
        if (payloads == null || payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            throw TODO("onBindViewHolder( payloads ) Method not implement")
        }
    }

    override fun onBindViewHolder(holder: K, positions: Int) {
        super.onBindViewHolder(holder, positions)
    }

    fun swapData(data: List<T>) {
        getData().clear()
        getData().addAll(data)
    }
}
