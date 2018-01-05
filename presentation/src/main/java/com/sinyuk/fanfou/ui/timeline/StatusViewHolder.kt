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

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.util.FanfouFormatter
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*

/**
 * Created by sinyuk on 2017/12/18.
 *
 * A RecyclerView ViewHolder that displays a status.
 */
class StatusViewHolder(private val view: View, private val glide: RequestManager, private val uniqueId: String?) : BaseViewHolder(view) {
    fun bind(status: Status) {
        view.swipeLayout.isRightSwipeEnabled = true
        view.swipeLayout.isClickToClose = true
        glide.asDrawable()
                .load(status.playerExtracts?.profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .transition(withCrossFade())
                .into(view.avatar)

        view.avatar.setOnClickListener {}

        view.screenName.background = null
        view.createdAt.background = null
        view.content.background = null
        view.screenName.text = status.playerExtracts?.screenName
        if (status.createdAt == null) {
            view.createdAt.text = null
        } else {
            view.createdAt.text = FanfouFormatter.convertDateToStr(status.createdAt!!)
        }
        view.content.text = status.text

        val url = when {
            status.photos?.thumburl != null -> status.photos?.thumburl
            status.photos?.largeurl != null -> status.photos?.largeurl
            else -> status.photos?.imageurl
        }

        if (url == null) {
            glide.clear(view.image)
            view.image.visibility = View.GONE
        } else {
            view.image.visibility = View.VISIBLE
            glide.asDrawable()
                    .load(url)
                    .apply(RequestOptions().centerCrop())
                    .transition(withCrossFade())
                    .into(view.image)
        }
    }


    fun clear() {
        view.swipeLayout.isRightSwipeEnabled = false
        view.avatar.setOnClickListener(null)
        view.screenName.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.createdAt.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.content.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.screenName.text = null
        view.createdAt.text = null
        view.content.text = null
        glide.clear(view.image)
        view.image.visibility = View.VISIBLE
        glide.clear(view.avatar)
    }

    companion object {
        fun create(parent: ViewGroup, glide: RequestManager, uniqueId: String?): StatusViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.timeline_view_list_item, parent, false)
            return StatusViewHolder(view, glide, uniqueId)
        }
    }

}