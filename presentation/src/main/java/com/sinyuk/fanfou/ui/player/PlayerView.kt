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
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractSwipeFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.util.toggleOutline
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.myutils.math.MathUtils
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.player_view.*
import kotlinx.android.synthetic.main.player_view_header.*
import javax.inject.Inject
import javax.inject.Named

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

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val accountViewModel: AccountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    private val playerViewModel: PlayerViewModel by lazy { obtainViewModel(factory, PlayerViewModel::class.java) }


    @Inject
    lateinit var toast: ToastUtils

    private val uniqueId by lazy { arguments?.getString("uniqueId") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton.setOnClickListener { activity?.onBackPressed() }
    }

    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var sharedPreferences: SharedPreferences

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)

        if (isSelf()) {
            accountViewModel.user.observe(this@PlayerView, Observer { subscribe(it) })
        } else {
            playerViewModel.profile(uniqueId!!).observe(this@PlayerView, Observer { subscribe(it) })
        }

        appBarLayout.addOnOffsetChangedListener { v, verticalOffset ->
            val max = v.totalScrollRange
            val minHeight = collapsingToolbarLayout.minimumHeight
            when {
                verticalOffset == 0 -> {
                    avatar.alpha = 1f
                    titleLayout.alpha = 0f
                }
                -verticalOffset < max -> {
                    MathUtils.constrain(0f, 1f, -verticalOffset * 1.0f / max).also {
                        avatar.alpha = 1 - it
                        titleLayout.alpha = it
                    }
                }
                -verticalOffset == max -> {
                    avatar.alpha = 0f
                    titleLayout.alpha = 1f
                }
            }
            Log.i("OnOffsetChanged", "max: $max, min: $minHeight, offset: $verticalOffset")
        }
    }

    private fun isSelf() = uniqueId == null || uniqueId == sharedPreferences.getString(UNIQUE_ID, null)

    private fun subscribe(resource: Resource<Player>?) {
        resource?.let {
            when (it.states) {
                States.SUCCESS -> render(it.data)
                States.ERROR -> it.message?.let { toast.toastShort(it) }
                else -> {
                }
            }
        }
    }

    private fun render(player: Player?) {
        player?.let {
            screenName.text = it.screenName
            userId.text = String.format(getString(R.string.format_unique_id), it.id)
            FanfouUtils.parseAndSetText(bio, it.description)
            FanfouUtils.parseAndSetText(link, it.url)
            followerCount.text = it.followersCount.toString()
            friendCount.text = it.friendsCount.toString()
            postCount.text = (it.statusesCount.toString() + "条饭否")
            actionBarTitle.text = it.screenName
            GlideApp.with(avatar).asDrawable().load(player.profileImageUrlLarge).avatar().transition(withCrossFade()).into(avatar)
            GlideApp.with(profileBackground).asDrawable().load(player.profileBackgroundImageUrl).transition(withCrossFade()).into(profileBackground)
            // pass NULL to identify yourself
            followerButton.setOnClickListener { (activity as AbstractActivity).start(FollowingView.newInstance(uniqueId)) }
            friendButton.setOnClickListener { (activity as AbstractActivity).start(FriendsView.newInstance(uniqueId)) }

            if (it.protectedX == true) {

            } else {
                loadRootFragment(R.id.navigationContainer, NavigationView.newInstance(uniqueId))
            }

            if (isSelf()) {
                // isSelf
                directMsgButton.visibility = View.GONE
                followOrEdit.text = getString(R.string.action_edit_profile)
                followOrEdit.toggleOutline(true)
                followOrEdit.setOnClickListener { }
            } else {
                directMsgButton.visibility = View.VISIBLE
                directMsgButton.setOnClickListener { }
                if (player.following == true) {
                    followOrEdit.text = getString(R.string.name_following)
                    followOrEdit.toggleOutline(true)
                    followOrEdit.setOnClickListener { }
                } else {
                    followOrEdit.text = getString(R.string.action_begin_follow)
                    followOrEdit.toggleOutline(false)
                    followOrEdit.setOnClickListener { }
                }
            }

            followOrEdit.visibility = View.VISIBLE
        }
    }


}