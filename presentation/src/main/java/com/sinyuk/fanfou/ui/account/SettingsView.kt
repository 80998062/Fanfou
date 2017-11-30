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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import android.view.View
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstractFragment
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.domain.entities.Player
import com.sinyuk.fanfou.injections.Injectable
import com.sinyuk.fanfou.utils.CompletableHandler
import com.sinyuk.fanfou.utils.obtainViewModel
import com.sinyuk.fanfou.viewmodels.ViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.settings_view.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/28.
 */
class SettingsView : AbstractFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.settings_view

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var preferences: RxSharedPreferences

    @Inject lateinit var toast: ToastUtils

    var adminLive: LiveData<Player>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply {
            accountRelay.observe(this@SettingsView, Observer<String> {
                Log.d("SettingsView", "UID改变")
                adminLive?.removeObserver(adminOB)
                adminLive = admin(it).apply { observe(this@SettingsView, adminOB) }
            })
        }

        switchAccount.setOnClickListener({
            activity?.let {
                val sheet = AccountBottomSheet()
                sheet.show(it.supportFragmentManager, AccountBottomSheet::class.java.simpleName)
            }
        })

        testButton.setOnClickListener {
            accountViewModel.updateProfile().subscribeWith(object : CompletableHandler(toast) {
            })
        }

        clearButton.setOnClickListener {
            preferences.getString(UNIQUE_ID).delete()
        }
    }

    private val adminOB: Observer<Player> = Observer { t ->
        t?.let {
            screenName.text = t.screenName
        }
    }
}