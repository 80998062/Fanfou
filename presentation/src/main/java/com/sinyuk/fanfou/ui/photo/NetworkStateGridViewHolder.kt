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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.Status
import com.sinyuk.fanfou.ui.BetterViewAnimator

/**
 * Created by sinyuk on 2018/2/24.
 *
 */
class NetworkStateGridViewHolder(view: View, private val retryCallback: () -> Unit) : RecyclerView.ViewHolder(view) {
    private val viewAnimator = view.findViewById<BetterViewAnimator>(R.id.viewAnimator)
    private val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
    private val retry = view.findViewById<TextView>(R.id.retryButton)

    init {
        retry.setOnClickListener { retryCallback() }
    }

    fun bind(networkState: NetworkState?) {
        when (networkState?.status) {
            Status.REACH_BOTTOM -> viewAnimator.displayedChildId = R.id.finishedLayout
            Status.RUNNING -> viewAnimator.displayedChildId = R.id.loadingLayout
            Status.FAILED -> viewAnimator.displayedChildId = R.id.errorLayout
            Status.SUCCESS, Status.REACH_TOP -> TODO()
        }

        progressBar.isIndeterminate = networkState?.status == Status.RUNNING
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateGridViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item_photo_grid, parent, false)
            return NetworkStateGridViewHolder(view, retryCallback)
        }
    }
}