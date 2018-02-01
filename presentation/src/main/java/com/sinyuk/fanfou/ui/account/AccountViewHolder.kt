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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.util.setUserId
import kotlinx.android.synthetic.main.account_list_item.view.*
import kotlinx.android.synthetic.main.account_list_item_underlayer.view.*

/**
 * Created by sinyuk on 2018/1/31.
 *
 */
class AccountViewHolder constructor(private val view: View, private val uniqueId: String) : BaseViewHolder(view) {
    fun bind(player: Player?) {
        GlideApp.with(view.avatar).asBitmap().load(player?.profileImageUrl).apply(RequestOptions.circleCropTransform()).into(view.avatar)
        view.screenName.text = player?.screenName
        setUserId(view.userId, player?.id)
        view.checkbox.setChecked(uniqueId == player?.uniqueId, false)
        view.swipeLayout.isSwipeEnabled = (uniqueId != player?.uniqueId) // 无法删除当前登录的账号
        view.swipeLayout.addSwipeListener(object : SimpleSwipeListener() {
            override fun onOpen(layout: SwipeLayout?) {
                YoYo.with(Techniques.Tada).duration(500).delay(200).playOn(view.deleteIcon)
            }
        })
        view.deleteButton.setOnClickListener {

        }
    }

    companion object {
        fun create(parent: ViewGroup, uniqueId: String) =
                AccountViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.account_list_item, parent, false), uniqueId)
    }
}