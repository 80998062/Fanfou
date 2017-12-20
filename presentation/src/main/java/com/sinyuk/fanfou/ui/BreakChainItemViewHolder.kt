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

package com.sinyuk.fanfou.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Status

/**
 * Created by sinyuk on 2017/12/18.
 *
 */
class BreakChainItemViewHolder(view: View, private val callable: (max: String?) -> Unit) : RecyclerView.ViewHolder(view) {

    private val loadmore = view.findViewById<TextView>(R.id.loadmore)

    fun bind(status: Status?) {
        if (status != null) {
            loadmore.text = "LOAD MORE" + status.id
            loadmore.setOnClickListener { callable(status.id) }
        }
    }


    companion object {
        fun create(parent: ViewGroup, callable: (max: String?) -> Unit): BreakChainItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_break_chain_item, parent, false)
            return BreakChainItemViewHolder(view, callable)
        }

        fun toVisibility(constraint: Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}