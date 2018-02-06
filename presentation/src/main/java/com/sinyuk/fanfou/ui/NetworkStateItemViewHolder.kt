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
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.Status
import com.sinyuk.fanfou.domain.TIMELINE_CONTEXT

/**
 * Created by sinyuk on 2017/12/18.
 * A View Holder that can display a loading or have click action.
 * It is used to show the network state of paging.
 */
/**

 */
class NetworkStateItemViewHolder(view: View,
                                 private val retryCallback: () -> Unit,
                                 private val path: String)
    : RecyclerView.ViewHolder(view) {
    private val viewAnimator = view.findViewById<BetterViewAnimator>(R.id.viewAnimator)

    private val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
    private val retry = view.findViewById<Button>(R.id.retryButton)
    private val errorMsg = view.findViewById<TextView>(R.id.errorMsg)
    private val finishedMsg = view.findViewById<TextView>(R.id.finishedMsg)

    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }

    fun bind(networkState: NetworkState?) {
        when (networkState?.status) {
            Status.REACH_BOTTOM -> viewAnimator.displayedChildId = R.id.finishedLayout
            Status.RUNNING -> viewAnimator.displayedChildId = R.id.loadingLayout
            Status.FAILED -> viewAnimator.displayedChildId = R.id.errorLayout
            Status.SUCCESS, Status.REACH_TOP -> TODO()
        }

        when (path) {
            TIMELINE_CONTEXT -> R.string.hint_no_more_context
            else -> R.string.hint_no_more_home
        }.apply {
            finishedMsg.setText(this)
        }

        progressBar.isIndeterminate = networkState?.status == Status.RUNNING
        errorMsg.text = networkState?.msg
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit, path: String): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback, path)
        }
    }
}