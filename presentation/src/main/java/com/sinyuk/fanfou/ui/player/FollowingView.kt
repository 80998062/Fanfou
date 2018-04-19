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

import android.os.Bundle
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.USERS_FOLLOWERS
import kotlinx.android.synthetic.main.friends_view.*

/**
 * Created by sinyuk on 2018/1/10.
 *
 */
class FollowingView : AbstractFragment(), Injectable {
    companion object {
        fun newInstance(uniqueId: String? = null) = FollowingView().apply {
            arguments = Bundle().apply { putString("uniqueId", uniqueId) }
        }
    }

    override fun layoutId() = R.layout.following_view

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        navBack.setOnClickListener { pop() }
        postFanfouButton.setOnClickListener { }
        if (findChildFragment(PlayerListView::class.java) == null) {
            loadRootFragment(R.id.followingListContainer, PlayerListView.newInstance(USERS_FOLLOWERS, arguments!!.getString("uniqueId")))
        } else {
            showHideFragment(findChildFragment(PlayerListView::class.java))
        }
    }

}