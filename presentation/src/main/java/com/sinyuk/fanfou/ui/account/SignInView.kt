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

import android.os.Bundle
import android.util.Log
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstractFragment
import com.sinyuk.fanfou.injections.Injectable
import com.sinyuk.fanfou.ui.HomeActivity
import com.sinyuk.fanfou.utils.CompletableHandler
import com.sinyuk.fanfou.utils.obtainViewModel
import com.sinyuk.fanfou.viewmodels.ViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.signin_view.*;
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */

class SignInView : AbstractFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.signin_view


    @Inject lateinit var factory: ViewModelFactory

    @Inject lateinit var toast: ToastUtils

    private lateinit var accountViewModel: AccountViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java)



        loginButton.setOnClickListener({
            val account = accountEt.text.toString()
            val password = passwordEt.text.toString()
            val d = accountViewModel.login("80998062@qq.com", "rabbit7run")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : CompletableHandler(toast) {
                        override fun onComplete() {
                            toHome()
                        }
                    })

            addDisposable(d)
        })
    }

    private fun toHome() {
        context?.let {
            HomeActivity.start(context!!)
            activity!!.finish()
        }
    }
}