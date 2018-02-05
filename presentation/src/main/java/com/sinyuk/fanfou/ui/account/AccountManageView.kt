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
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.account_manage_view.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2018/1/31.
 *
 */
class AccountManageView : AbstractFragment(), Injectable, AccountListView.OnAccountListListener {


    override fun layoutId() = R.layout.account_manage_view

    @field:[Named(TYPE_GLOBAL) Inject]
    lateinit var sharedPreferences: SharedPreferences


    @Inject
    lateinit var toast: ToastUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (findChildFragment(AccountListView::class.java) == null) {
            val fragment = AccountListView()
            fragment.listener = this@AccountManageView
            loadRootFragment(R.id.accountListContainer, fragment, false, false)
        } else {
            showHideFragment(findChildFragment(AccountListView::class.java))
        }

        doneButton.setOnClickListener {
            if (findChildFragment(AccountListView::class.java)?.onDone() != false) {
                pop()
            } else {
                findChildFragment(AccountListView::class.java)?.onSwitch()?.observe(this@AccountManageView, Observer {
                    when (it?.states) {
                        States.SUCCESS -> pop()
                        States.ERROR -> {
                            it.message?.let { toast.toastShort(it) }
                            onSignIn()
                        }
                        States.LOADING -> {
                            toast.toastShort(R.string.hint_switch_account_ing)
                        }
                    }
                })
            }
        }

        navBack.setOnClickListener { pop() }

        cancelButton.setOnClickListener { onManageView() }
    }

    private fun onManageView() {
        toolbarTitle.setCurrentText(getString(R.string.label_account_manage))
        doneButton.visibility = View.VISIBLE
        actionButtonSwitcher.displayedChildId = R.id.navBack
        showHideFragment(findChildFragment(AccountListView::class.java), findChildFragment(SignInView::class.java))
    }

    override fun onBackPressedSupport() = if (doneButton.visibility == View.INVISIBLE) {
        onManageView()
        true
    } else {
        false
    }


    override fun onSignUp() {
        // TODO: open webView
    }

    override fun onSignIn() {
        if (findChildFragment(SignInView::class.java) == null) {
            val fragment = SignInView()
            loadRootFragment(R.id.accountListContainer, fragment, false, false)
            showHideFragment(fragment, findChildFragment(AccountListView::class.java))
        } else {
            showHideFragment(findChildFragment(SignInView::class.java), findChildFragment(AccountListView::class.java))
        }
        toolbarTitle.setCurrentText(getString(R.string.label_sign_in))
        doneButton.visibility = View.INVISIBLE
        actionButtonSwitcher.displayedChildId = R.id.cancelButton
    }
}