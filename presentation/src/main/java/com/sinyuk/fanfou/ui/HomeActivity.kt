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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.ui.account.AccountBottomSheet
import com.sinyuk.fanfou.ui.account.AccountViewModel
import com.sinyuk.fanfou.ui.account.ProfileView
import com.sinyuk.fanfou.ui.home.HomeView
import com.sinyuk.fanfou.ui.home.RootPageAdapter
import com.sinyuk.fanfou.ui.message.MessageView
import com.sinyuk.fanfou.ui.search.PublicView
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.ViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.home_activity.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */
class HomeActivity : AbstractActivity() {
    companion object {
        @JvmStatic
        fun start(context: Context, flags: Int?) {
            val intent = Intent(context, HomeActivity::class.java)
            flags?.let { intent.flags = flags }
            context.startActivity(intent)
        }
    }

    override fun beforeInflate() {
    }

    override fun layoutId(): Int? = R.layout.home_activity

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    @Inject lateinit var toast: ToastUtils

    var adminLive: LiveData<Player>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply {
            accountRelay.observe(this@HomeActivity, Observer<String> {
                adminLive?.removeObserver(adminOB)
                adminLive = admin(it).apply { observe(this@HomeActivity, adminOB) }
            })
        }

        setupViewPager()

        setupActionBar()
    }

    private fun setupActionBar() {
        avatar.setOnClickListener {
            val sheet = AccountBottomSheet()
            sheet.show(supportFragmentManager, AccountBottomSheet::class.java.simpleName)
        }
    }

    private fun setupViewPager() {
        val homePage = HomeView()
        val publicPage = PublicView()
        val messagePage = MessageView()
        val profilePage = ProfileView()

        val adapter = RootPageAdapter(supportFragmentManager, mutableListOf(homePage, publicPage, messagePage, profilePage))

        viewPager.offscreenPageLimit = 4
        viewPager.adapter = adapter
    }

    private val adminOB: Observer<Player> = Observer { t ->
        t?.let {
            Log.d(HomeActivity::class.java.simpleName, t.uniqueId)
        }
    }
}