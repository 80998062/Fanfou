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

package com.sinyuk.fanfou.ui

import com.chad.library.adapter.base.loadmore.LoadMoreView
import com.sinyuk.fanfou.R


class CustomLoadMoreView : LoadMoreView() {

    override fun getLayoutId(): Int = R.layout.quick_view_load_more

    override fun getLoadingViewId(): Int = R.id.load_more_loading_view

    override fun getLoadFailViewId(): Int = R.id.load_more_load_fail_view

    override fun getLoadEndViewId(): Int = R.id.load_more_load_end_view

}