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

package com.sinyuk.fanfou.ui.editor

import android.support.v4.app.Fragment
import android.view.ViewGroup
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.util.QuickAdapter

/**
 * Created by sinyuk on 2018/1/17.
 *
 */
class MentionAdapter constructor(fragment: Fragment) : QuickAdapter<Suggestible, MentionItemViewHolder>(null) {

    private val glide = GlideApp.with(fragment)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int) = MentionItemViewHolder.create(parent, glide)

    override fun convert(helper: MentionItemViewHolder, item: Suggestible?) {
        item?.let { helper.bind(it as Player) }
    }
}