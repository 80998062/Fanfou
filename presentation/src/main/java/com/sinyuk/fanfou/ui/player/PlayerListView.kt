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

/**
 * Created by sinyuk on 2018/1/10.
 *
 */
class PlayerListView : AbstractFragment(), Injectable {

    companion object {
        fun Path(path: String, uniqueId: String? = null) = PlayerListView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("uniqueId", uniqueId)
            }
        }
    }

    override fun layoutId() = R.layout.player_list_view

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)

    }

    private fun setupRecyclerView() {

    }
}