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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.util.FanfouFormatter
import com.sinyuk.fanfou.util.addFragmentInActivity
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*

/**
 * Created by sinyuk on 2017/12/18.
 *
 * A RecyclerView ViewHolder that displays a status.
 */
class StatusViewHolder(private val view: View, private val glide: RequestManager, private val uniqueId: String?) : BaseViewHolder(view) {
    fun bind(status: Status?) {
        if (status == null) {

        } else {
            view.swipeLayout.isClickToClose = true
            view.avatar.setImageResource(R.mipmap.ic_launcher_round)
            view.avatar.setOnClickListener {
                @Suppress("CAST_NEVER_SUCCEEDS")
                (it.context as AppCompatActivity).addFragmentInActivity(PlayerView.newInstance(status.playerExtracts?.uniqueId), R.id.fragment_container, true)
            }
            view.screenName.text = status.playerExtracts?.screenName
            if (status.createdAt == null) {
                view.createdAt.text = null
            } else {
                view.createdAt.text = FanfouFormatter.convertDateToStr(status.createdAt!!)
            }
            view.content.text = status.text
            view.image.setImageResource(R.mipmap.ic_launcher_round)
        }
    }


    companion object {
        fun create(parent: ViewGroup, glide: RequestManager, uniqueId: String?): StatusViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.timeline_view_list_item, parent, false)
            return StatusViewHolder(view, glide, uniqueId)
        }
    }

}