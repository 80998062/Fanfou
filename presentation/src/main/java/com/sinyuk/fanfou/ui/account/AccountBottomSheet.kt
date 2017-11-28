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
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstracBottomSheetFragment
import com.sinyuk.fanfou.domain.entities.User
import com.sinyuk.fanfou.injections.Injectable
import com.sinyuk.fanfou.lives.AutoClearedValue
import com.sinyuk.fanfou.utils.QuickAdapter
import com.sinyuk.fanfou.utils.obtainViewModel
import com.sinyuk.fanfou.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.account_bottomsheet.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */
class AccountBottomSheet : AbstracBottomSheetFragment(), Injectable {

    override fun layoutId(): Int? = R.layout.account_bottomsheet


    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeButton.setOnClickListener({
            dismissAllowingStateLoss()
        })


        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply {
            allAccounts.observe(this@AccountBottomSheet, accountsOB)
        }
    }

    private var adapter: AutoClearedValue<AccountAdapter>? = null

    private fun initListView(data: List<User>?) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        val accountAdapter = AccountAdapter(R.layout.item_switch_account, data)
        recyclerView.adapter = accountAdapter
        adapter = AutoClearedValue(this, accountAdapter)
    }


    private val accountsOB: Observer<List<User>> = Observer { t ->
        t?.let {
            if (adapter == null) {
                initListView(t)
            } else if (adapter!!.get() != null) {
                adapter!!.get()!!.setNewData(t)
            }
        }
    }

    class AccountAdapter constructor(res: Int, data: List<User>?) : QuickAdapter<User, BaseViewHolder>(res, data) {
        override fun convert(helper: BaseViewHolder, item: User?) {
            helper.setText(R.id.id, item?.id)
            helper.setText(R.id.screenName, "@"+item?.screenName)
        }
    }
}