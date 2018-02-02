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

import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.domain.util.stringLiveData
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.account_list_footer.view.*
import kotlinx.android.synthetic.main.account_list_view.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2018/1/31.
 */
class AccountListView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance() = AccountListView()
    }

    override fun layoutId() = R.layout.account_list_view


    @Inject
    lateinit var factory: FanfouViewModelFactory

    @Inject
    lateinit var toast: ToastUtils
    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupList()

        accountViewModel.admins().observe(this@AccountListView, Observer {
            adapter.setNewData(it)
        })
    }

    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var sharedPreferences: SharedPreferences

    lateinit var adapter: AccountAdapter

    private fun setupList() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)

        adapter = AccountAdapter(sharedPreferences.getString(UNIQUE_ID, null))

        val footer = LayoutInflater.from(context!!).inflate(R.layout.account_list_footer, recyclerView, false)
        adapter.addFooterView(footer)
        footer.addButton.setOnClickListener { listener?.onSignIn() }
        footer.registerButton.setOnClickListener { listener?.onSignUp() }

        recyclerView.adapter = adapter

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.deleteButton -> {
                    adapter.collapse(position)
                }
            }
        }


        sharedPreferences.stringLiveData(UNIQUE_ID, "").observe(this@AccountListView, Observer {
            if (it?.isNotEmpty() == true) {
                if (adapter.uniqueId !== it) {
                    adapter.uniqueId = it
                    adapter.notifyDataSetChanged()
                }
            } else {

            }
        })
    }

    /**
     *
     */
    interface OnAccountListListener {
        fun onSignUp()
        fun onSignIn()
    }

    val listener: OnAccountListListener? = null
}