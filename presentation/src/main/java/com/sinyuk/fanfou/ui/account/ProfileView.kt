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
import android.util.Log
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.abstracts.AbstractLazyFragment
import com.sinyuk.fanfou.domain.entities.Player
import com.sinyuk.fanfou.injections.Injectable
import com.sinyuk.fanfou.utils.obtainViewModel
import com.sinyuk.fanfou.viewmodels.ViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/28.
 */
class ProfileView : AbstractLazyFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.profile_view

    @Inject lateinit var factory: ViewModelFactory

    private lateinit var accountViewModel: AccountViewModel

    @Inject lateinit var toast: ToastUtils

    var adminLive: LiveData<Player>? = null

    override fun lazyDo() {
        accountViewModel = obtainViewModel(factory, AccountViewModel::class.java).apply {
            accountRelay.observe(this@ProfileView, Observer<String> {
                adminLive?.removeObserver(adminOB)
                adminLive = admin(it).apply { observe(this@ProfileView, adminOB) }
            })
        }

    }

    private val adminOB: Observer<Player> = Observer { t ->
        t?.let {
            Log.d(ProfileView::class.java.simpleName, t.uniqueId)
        }
    }
}