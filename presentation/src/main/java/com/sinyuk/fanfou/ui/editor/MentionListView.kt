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

package com.sinyuk.fanfou.ui.editor

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.MarginDecoration
import kotlinx.android.synthetic.main.mention_list_view.*

/**
 * Created by sinyuk on 2018/1/24.
 *
 */
class MentionListView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.mention_list_view

    companion object {
        fun newInstance(data: Array<Suggestible>) = MentionListView().apply {
            arguments = Bundle().apply { putParcelableArray("data", data) }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, item: Suggestible)
    }

    var onItemClickListener: OnItemClickListener? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            setData(it.getParcelableArray("data") as MutableList<out Suggestible>)
        }
    }

    fun setData(data: MutableList<out Suggestible>) {
        adapter.setNewData(data)
    }

    private lateinit var adapter: MentionAdapter

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 10
            recyclerView.layoutManager = this
        }

        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        adapter = MentionAdapter(this@MentionListView)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            adapter.getItem(position)?.let {
                onItemClickListener?.onItemClick(position, it)
            }
            adapter.setNewData(null)
        }
    }
}