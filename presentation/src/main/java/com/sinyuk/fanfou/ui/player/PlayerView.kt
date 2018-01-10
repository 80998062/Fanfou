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

package com.sinyuk.fanfou.ui.player

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractSwipeFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.player_view.*
import kotlinx.android.synthetic.main.player_view_header.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/7.
 *
 */
class PlayerView : AbstractSwipeFragment(), Injectable {
    override fun layoutId() = R.layout.player_view

    companion object {
        fun newInstance(uniqueId: String? = null) = PlayerView().apply {
            arguments = Bundle().apply { putString("uniqueId", uniqueId) }
        }
    }

    @Inject lateinit var factory: FanfouViewModelFactory

    private val accountViewModel: AccountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    private val playerViewModel: PlayerViewModel by lazy { obtainViewModelFromActivity(factory, PlayerViewModel::class.java) }


    @Inject lateinit var toast: ToastUtils

    private val uniqueId by lazy { arguments?.getString("uniqueId") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton.setOnClickListener { activity?.onBackPressed() }
    }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        if (uniqueId == null) {
            accountViewModel.user.observe(this@PlayerView, playerObserver)
        } else {
            playerViewModel.profile(uniqueId!!).observe(this@PlayerView, playerObserver)
        }
        loadRootFragment(R.id.navigationContainer, NavigationView.newInstance(uniqueId))
    }

    private val playerObserver by lazy { Observer<Resource<Player>> { subscribe(it) } }

    private fun subscribe(resource: Resource<Player>?) {
        resource?.let {
            when (it.states) {
                States.SUCCESS -> {
                    render(it.data)
                }
                States.ERROR -> {
                    it.message?.let { toast.toastShort(it) }
                }
                else -> {
                }
            }
        }
    }

    private fun render(player: Player?) {
        player?.let {
            screenName.text = it.screenName
            userId.text = String.format(getString(R.string.format_unique_id), it.uniqueId)
            bio.text = it.description
            link.text = it.url
            followerCount.text = it.followersCount.toString()
            followingCount.text = it.friendsCount.toString()
            postCount.text = (it.statusesCount.toString() + "条饭否")
            actionBarTitle.text = it.screenName

            followerButton.setOnClickListener { }
            if (it.protectedX == true) {

            } else {

            }

            if (uniqueId == null) {
                // isSelf
                followOrEdit.text = getString(R.string.action_edit_profile)
            } else {
                if (player.following == true) {
                    followOrEdit.text = getString(R.string.action_unfollow)
                } else {
                    followOrEdit.text = getString(R.string.action_follow)
                }
            }
        }
    }


    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }

}