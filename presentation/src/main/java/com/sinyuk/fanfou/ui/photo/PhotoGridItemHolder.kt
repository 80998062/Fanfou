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

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.photo_grid_list_item.view.*

/**
 * Created by sinyuk on 2018/2/24.
 */
class PhotoGridItemHolder(private val view: View, private val glide: GlideRequests, private val fragment: Fragment) : BaseViewHolder(view) {


    fun bind(status: Status) {
        val url = status.photos?.size(ConvertUtils.dp2px(fragment.context, Photos.SMALL_SIZE))
        glide.load(url).into(view.image)
    }

    fun clear() {
        view.image.setOnClickListener(null)
        glide.clear(view.image)
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, fragment: Fragment): PhotoGridItemHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.photo_grid_list_item, parent, false)
            return PhotoGridItemHolder(view, glide, fragment)
        }
    }
}