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

package com.sinyuk.fanfou.ui.splash

import android.content.Intent
import android.os.Bundle
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.ui.HomeActivity
import com.sinyuk.fanfou.ui.account.AccountViewModel
import com.sinyuk.fanfou.ui.account.SignActivity
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/27.
 */
class SplashActivity : AbstractActivity() {

    override fun beforeInflate() {}

    override fun layoutId(): Int? = R.layout.splash_activity

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply {
            registration()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError({ toSign() })
                    .subscribe(Consumer {
                        if (it == null) {
                            toSign()
                        } else {
                            prepareLaunch()
                        }
                    })
        }
    }


    private fun toSign() {
        SignActivity.start(this, Intent.FLAG_ACTIVITY_CLEAR_TASK)
        finish()
    }

    private fun prepareLaunch() {
        toHome()
    }

    private fun toHome() {
        HomeActivity.start(this, Intent.FLAG_ACTIVITY_CLEAR_TASK)
        finish()
    }
}