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
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.entities.Status
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*

/**
 * Created by sinyuk on 2017/12/1.
 */
class StatusAdapter : PagedListAdapter<Status, StatusAdapter.StatusViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = getItem(position)
        if (status == null) {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            holder.clear()
        } else {
            holder.bindTo(status)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder = StatusViewHolder(parent)


    class StatusViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_view_list_item, parent, false)) {
        fun clear() {
            itemView.avatar.setImageDrawable(null)
            itemView.screenName.text = null
            itemView.content.text = null
            itemView.createdAt.text = null
            itemView.image.setImageDrawable(null)
        }

        fun bindTo(status: Status) {
            itemView.avatar.setImageResource(R.mipmap.ic_launcher_round)
            itemView.screenName.text = status.user?.screenName
            itemView.content.text = status.text
            itemView.createdAt.text = status.createdAt?.toLocaleString()
            itemView.image.setImageResource(R.mipmap.ic_launcher_round)
        }
    }


    companion object {
        val DIFF_CALLBACK: DiffCallback<Status> = object : DiffCallback<Status>() {
            override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean = oldItem.id === newItem.id

            override fun areContentsTheSame(oldItem: Status, newItem: Status): Boolean = true
        }
    }
}