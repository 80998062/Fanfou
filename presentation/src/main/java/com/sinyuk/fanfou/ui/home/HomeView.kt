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

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.View
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.myutils.system.ToastUtils
import com.yalantis.colormatchtabs.colormatchtabs.adapter.ColorTabAdapter
import com.yalantis.colormatchtabs.colormatchtabs.listeners.OnColorTabSelectedListener
import com.yalantis.colormatchtabs.colormatchtabs.model.ColorTab
import kotlinx.android.synthetic.main.home_view.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/11.
 *
 */
class HomeView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.home_view

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    @Inject
    lateinit var toast: ToastUtils

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
                KeyboardUtil.hideKeyboard(drawerView)
            }
        })

        setupTabLayout()
    }


    @SuppressLint("Recycle")
    private fun setupTabLayout() {
        val colors = resources.getStringArray(R.array.tab_colors)
        val icons = resources.obtainTypedArray(R.array.tab_icons)
        resources.getStringArray(R.array.tab_titles).apply {
            forEachIndexed { index, title ->
                val color = Color.parseColor(colors[index])
                val icon = icons.getDrawable(index)
                tabLayout.addTab(ColorTabAdapter.createColorTab(tabLayout, title, color, icon))
            }
        }

        tabLayout.addOnColorTabSelectedListener(object : OnColorTabSelectedListener {
            override fun onSelectedTab(tab: ColorTab?) {
                if (BuildConfig.DEBUG) Log.i("onSelectedTab", "position: " + tab?.position)
                EventBus.getDefault().post(TabEvent(index = tab?.position ?: 0))
            }

            override fun onUnselectedTab(tab: ColorTab?) {
            }
        })

    }
}