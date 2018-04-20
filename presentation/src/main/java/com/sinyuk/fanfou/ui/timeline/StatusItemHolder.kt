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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.util.FanfouFormatter
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.status_list_item.view.*

/**
 * Created by sinyuk on 2018/4/20.
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│        _______. __  .__   __. ____    ____  __    __   __  ___   │
│       /       ||  | |  \ |  | \   \  /   / |  |  |  | |  |/  /   │
│      |   (----`|  | |   \|  |  \   \/   /  |  |  |  | |  '  /    │
│       \   \    |  | |  . `  |   \_    _/   |  |  |  | |    <     │
│   .----)   |   |  | |  |\   |     |  |     |  `--'  | |  .  \    │
│   |_______/    |__| |__| \__|     |__|      \______/  |__|\__\   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
 */
class StatusItemHolder(private val view: View, private val glide: GlideRequests, private val uniqueId: String?, private val photoExtra: Bundle?) : BaseViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, uniqueId: String?, photoExtra: Bundle?): StatusItemHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.status_list_item, parent, false)
            return StatusItemHolder(view, glide, uniqueId, photoExtra)
        }
    }

    private val uidFormat = "@%s"
    private val sourceFormat = "来自%s"

    fun bind(status: Status) {
        glide.asDrawable().load(status.playerExtracts?.profileImageUrl).avatar()
                .transition(withCrossFade()).into(view.avatar)

        when (status.playerExtracts?.uniqueId) {
            null, uniqueId -> view.avatar.setOnClickListener(null)
            else -> view.avatar.setOnClickListener {
                (view.context as AbstractActivity).loadRootFragment(R.id.fragment_container,
                        PlayerView.newInstance(uniqueId = status.playerExtracts!!.uniqueId))
            }
        }

        view.screenName.text = status.playerExtracts?.screenName
        FanfouUtils.parseAndSetText(view.content, status.text)
        FanfouUtils.parseAndSetText(view.source, String.format(sourceFormat, status.source))
        val formattedId = String.format(uidFormat, status.playerExtracts?.id)
        view.userId.text = formattedId
        view.createdAt.text = FanfouFormatter.convertDateToStr(status.createdAt!!)
        if (status.photos == null) {
            glide.clear(view.image)
            view.image.visibility = View.GONE
        } else {
            view.image.visibility = View.VISIBLE
            photoExtra?.let {
                view.image.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        view.image.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val ratio = it.getInt("h", 0) * 1.0f / it.getInt("w", 1)
                        val lps = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        lps.height = (view.image.width * ratio).toInt()
                        view.image.layoutParams = lps
                    }
                })
            }

            glide.asDrawable()
                    .load(status.photos?.size(ConvertUtils.dp2px(view.context, Photos.LARGE_SIZE)))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(view.image)
        }

        view.moreButton.setOnClickListener { }
    }
}