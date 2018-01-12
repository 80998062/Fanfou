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

package com.sinyuk.fanfou.ui.home

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.home_view.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/11.
 *
 */
class HomeView : AbstractFragment(), Injectable, View.OnClickListener {
    override fun layoutId() = R.layout.home_view

    @Inject lateinit var factory: ViewModelProvider.Factory
    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }
    @Inject lateinit var toast: ToastUtils


    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)

        if (findChildFragment(TabView::class.java) == null) {
            loadRootFragment(R.id.tabViewContainer, TabView())
        } else {
            showHideFragment(findChildFragment(TabView::class.java))
        }

        renderUI()
    }


    private fun renderUI() {

        drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerOpened(drawerView: View) {

            }
        })

        setupTabLayout()
    }

    private fun setupTabLayout() {
        homeTab.setOnClickListener(this)
        publicTab.setOnClickListener(this)
        notificationTab.setOnClickListener(this)
        messageTab.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.homeTab -> 0
            R.id.publicTab -> 1
            R.id.notificationTab -> 2
            R.id.messageTab -> 3
            else -> TODO()
        }.apply {
            EventBus.getDefault().post(TabEvent(index = this))
        }
    }
}