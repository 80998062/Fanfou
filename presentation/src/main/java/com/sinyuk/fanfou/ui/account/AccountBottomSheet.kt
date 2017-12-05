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
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.util.Attributes
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstracBottomSheetFragment
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.util.AutoClearedValue
import com.sinyuk.fanfou.util.CompletableHandler
import com.sinyuk.fanfou.util.QuickSwipeAdapter
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.ViewModelFactory
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.account_bottomsheet.*
import kotlinx.android.synthetic.main.account_selectable_list_footer.view.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/28.
 */
class AccountBottomSheet : AbstracBottomSheetFragment(), Injectable {

    override fun layoutId(): Int? = R.layout.account_bottomsheet


    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var preferences: RxSharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListView()

        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply { admins.observe(this@AccountBottomSheet, adminsOB) }
    }

    private lateinit var adapter: AutoClearedValue<AccountAdapter>

    private fun initListView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 10
            isAutoMeasureEnabled = true
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)

        AccountAdapter(R.layout.account_selectable_list_item, null).apply {
            onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
                when (view.id) {
                    R.id.checkbox -> {
                        if (adapter.get()?.checkedPosition != position) {
                            onSwitch(position)
                            adapter.get()?.checkedPosition = position
                        }

                    }
                    R.id.deleteButton -> onDelete(position)
                }
            }

            val footer: View = LayoutInflater.from(context).inflate(R.layout.account_selectable_list_footer, recyclerView, false)
            footer.addNewAccount.setOnClickListener { addNewAccount() }
            addFooterView(footer)
            setHeaderFooterEmpty(false, true)
            recyclerView.adapter = this
            adapter = AutoClearedValue(this@AccountBottomSheet, this)
        }
    }

    private fun addNewAccount() {
        context?.let { SignActivity.start(context!!, null) }
        dismissAllowingStateLoss()
    }

    private fun onDelete(position: Int) {
        adapter.get()?.getItem(position)?.let {
            val d: Disposable = accountViewModel.deleteRegistration(it.uniqueId)
                    .subscribeWith(object : CompletableHandler() {
                        override fun onComplete() {
                            adapter.get()?.remove(position)
                        }
                    })
            addDisposable(d)
        }
    }

    private fun onSwitch(position: Int) {
        adapter.get()?.getItem(position)?.let {
            preferences.getString(UNIQUE_ID).set(it.uniqueId)
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    private val adminsOB: Observer<MutableList<Player>> = Observer { t ->
        // TODO: 要求当前登录用户在第一位
        if (adapter.get() != null) {
            adapter.get()!!.setNewData(t)
        }
    }

    class AccountAdapter constructor(res: Int, data: List<Player>?) : QuickSwipeAdapter<Player, BaseViewHolder>(res, data) {
        private var mItemManger = SwipeItemRecyclerMangerImpl(this)

        override fun openItem(position: Int) {
            mItemManger.openItem(position)
        }

        override fun closeItem(position: Int) {
            mItemManger.closeItem(position)
        }

        override fun closeAllExcept(layout: SwipeLayout) {
            mItemManger.closeAllExcept(layout)
        }

        override fun closeAllItems() {
            mItemManger.closeAllItems()
        }

        override fun getOpenItems(): List<Int> {
            return mItemManger.openItems
        }

        override fun getOpenLayouts(): List<SwipeLayout> {
            return mItemManger.openLayouts
        }

        override fun removeShownLayouts(layout: SwipeLayout) {
            mItemManger.removeShownLayouts(layout)
        }

        override fun isOpen(position: Int): Boolean {
            return mItemManger.isOpen(position)
        }

        override fun getMode(): Attributes.Mode {
            return mItemManger.mode
        }

        override fun setMode(mode: Attributes.Mode) {
            mItemManger.mode = mode
        }

        override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipeLayout

        override fun convert(helper: BaseViewHolder, item: Player?) {
            helper.setText(R.id.id, item?.id)
            helper.setText(R.id.screenName, "@" + item?.screenName)
            helper.getView<AppCompatRadioButton>(R.id.checkbox).isChecked = checkedPosition == helper.adapterPosition
            helper.getView<SwipeLayout>(R.id.swipeLayout).isSwipeEnabled = checkedPosition != helper.adapterPosition
            helper.getView<SwipeLayout>(R.id.swipeLayout).showMode = SwipeLayout.ShowMode.PullOut
            helper.getView<SwipeLayout>(R.id.swipeLayout).addSwipeListener(object : SimpleSwipeListener() {
                override fun onOpen(layout: SwipeLayout?) {
                    layout?.findViewById<View>(R.id.deleteIcon)?.apply {
                        YoYo.with(Techniques.Tada).interpolate(FastOutSlowInInterpolator()).duration(500).delay(200).playOn(this)
                    }
                }
            })
            helper.addOnClickListener(R.id.checkbox)
            helper.addOnClickListener(R.id.deleteButton)

        }

        var checkedPosition = 0
            set(value) {
                if (field != value && value in 0..(itemCount - 1)) {
                    val temp = field
                    field = value
                    notifyItemChanged(value)
                    if (temp != RecyclerView.NO_POSITION) {
                        notifyItemChanged(temp)
                    }
                }
            }

    }
}