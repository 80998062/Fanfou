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

package com.sinyuk.fanfou.ui.colormatchtabs.adapter

import android.graphics.drawable.Drawable
import com.sinyuk.fanfou.ui.colormatchtabs.colortabs.ColorMatchTabLayout
import com.sinyuk.fanfou.ui.colormatchtabs.model.ColorTab

/**
 * Created by anna on 24.05.17.
 * 
 */
class ColorTabAdapter {

    companion object {
        @JvmStatic
        fun createColorTab(tabLayout: ColorMatchTabLayout, text: String, color: Int, icon: Drawable): ColorTab {
            val colorTab = tabLayout.newTab()
            colorTab.text = text
            colorTab.selectedColor = color
            colorTab.icon = icon
            return colorTab
        }
    }

}