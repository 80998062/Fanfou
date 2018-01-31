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

package com.sinyuk.fanfou.util

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.ui.QMUIRoundButton
import com.sinyuk.fanfou.ui.QMUIRoundButtonDrawable

/**
 * Created by sinyuk on 2018/1/4.
 *
 */

/**
 * convert px to dp
 */
fun px2dp(context: Context, pxValue: Float) = (pxValue / context.resources.displayMetrics.density + 0.5f).toInt()


fun QMUIRoundButton.toggleOutline(enable: Boolean) {
    if (enable) {
        (background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
    } else {
        (background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
        setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    }
}

/**
 * 背景色为<colorControlDisable/>和<colorAccent/>
 */
fun QMUIRoundButton.toggleSolid(enable: Boolean) {
    if (enable) {
        (background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
    } else {
        (background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorControlDisable))
    }
}