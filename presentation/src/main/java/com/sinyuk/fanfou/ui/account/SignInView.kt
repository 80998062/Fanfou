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

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.HomeActivity
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.signin_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */

class SignInView : AbstractFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.signin_view


    @Inject lateinit var factory: ViewModelProvider.Factory

    @Inject lateinit var toast: ToastUtils

    private val accountViewModel: AccountViewModel by lazy { obtainViewModel(factory, AccountViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener({
            accountViewModel.repo.sign(
                    accountEt.text.toString(),
                    passwordEt.text.toString()
            )
        })
    }

    private fun toHome() {
        context?.let {
            HomeActivity.start(context!!, Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity!!.finish()
        }
    }
}