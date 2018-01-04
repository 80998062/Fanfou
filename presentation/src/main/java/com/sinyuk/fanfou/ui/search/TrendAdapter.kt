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

package com.sinyuk.fanfou.ui.search

import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Trend
import com.sinyuk.fanfou.util.QuickAdapter

/**
 * Created by sinyuk on 2018/1/4.
 *
 */
class TrendAdapter constructor(data: MutableList<Trend>? = null) : QuickAdapter<Trend, BaseViewHolder>(R.layout.trend_list_item, data) {
    override fun convert(helper: BaseViewHolder, item: Trend) {
        helper.setText(R.id.index, helper.adapterPosition.toString())
        helper.setText(R.id.name, item.name)
        helper.setText(R.id.query, item.query)
    }
}