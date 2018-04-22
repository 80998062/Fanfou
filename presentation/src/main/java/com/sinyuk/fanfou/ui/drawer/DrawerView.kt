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

package com.sinyuk.fanfou.ui.drawer

import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.sinyuk.fanfou.NIGHT_MODE
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.currentNightMode
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.util.booleanLiveData
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.account.AccountManageView
import com.sinyuk.fanfou.ui.player.FollowingView
import com.sinyuk.fanfou.ui.player.FriendsView
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.util.setUserId
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.drawer_view.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2018/1/31.
 *
 */
class DrawerView : AbstractFragment(), Injectable {
    override fun layoutId() = R.layout.drawer_view

    @field:[Named(TYPE_GLOBAL) Inject]
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var toast: ToastUtils

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        accountViewModel.profile.observe(this@DrawerView, Observer { renderPlayer(it) })

        setupNightButton()

        accountButton.setOnClickListener {
            closeDrawerAndPost(accountButton, { startFragmentInActivity(AccountManageView()) })
        }
    }

    private fun startFragmentInActivity(fragment: AbstractFragment) {
        (activity as AbstractActivity).extraTransaction()
                .loadRootFragment(R.id.fragment_container, fragment, true, true)
    }


    private fun renderPlayer(data: Player?) {
        data?.let {
            GlideApp.with(avatar).asDrawable().avatar().load(it.profileImageUrlLarge).into(avatar)
            screenName.text = it.screenName
            setUserId(userId, it.id)
            if (it.friendsCount == 0) {
                friendCount.text = 0.toString()
            } else {
                friendCount.text = it.friendsCount.toString()
                friendButton.setOnClickListener {
                    closeDrawerAndPost(friendButton, {
                        startFragmentInActivity(FriendsView.newInstance(data.uniqueId))
                    })
                }
            }

            if (it.followersCount == 0) {
                followerCount.text = 0.toString()
            } else {
                followerCount.text = it.followersCount.toString()
                followerButton.setOnClickListener {
                    closeDrawerAndPost(followerButton, {
                        startFragmentInActivity(FollowingView.newInstance(data.uniqueId))
                    })
                }
            }

            mineButton.setOnClickListener {
                closeDrawerAndPost(mineButton, { startFragmentInActivity(PlayerView.newInstance(uniqueId = data.uniqueId)) })
            }

        }
    }

    private fun setupNightButton() {
        sharedPreferences.booleanLiveData(NIGHT_MODE, false).observe(this@DrawerView, Observer {
            if (it == true) {
                nightModeButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorAccent))
            } else {
                nightModeButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlNormal))
            }
        })


        nightModeButton.setOnClickListener {
            when (currentNightMode(context!!)) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPreferences.edit().putBoolean(NIGHT_MODE, true).apply()
                    toast.toastShort("夜间模式开启")
                    activity?.recreate()
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPreferences.edit().putBoolean(NIGHT_MODE, false).apply()
                    toast.toastShort("夜间模式关闭")
                    activity?.recreate()
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    toast.toastShort("夜间模式不知道")
                }
            }
        }
    }

    private fun closeDrawerAndPost(view: View, action: () -> Unit) {
        view.post(action)
        view.postDelayed({
            toggleDrawer(false)
        }, 500)
    }

    private fun toggleDrawer(open: Boolean? = null) {
        EventBus.getDefault().post(DrawerToggleEvent(open))
    }
}