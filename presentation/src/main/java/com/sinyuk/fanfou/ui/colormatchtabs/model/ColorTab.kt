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

package com.sinyuk.fanfou.ui.colormatchtabs.model

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.sinyuk.fanfou.ui.colormatchtabs.colortabs.ColorTabView


/**
 * Created by anna on 10.05.17.
 *
 */
class ColorTab {

    companion object {
        private const val INVALID_POSITION = -1
    }

    var tabView: ColorTabView? = null

    /**
     * Sets and rReturn the icon associated with this tab.
     *
     * @return The tab's icon
     */
    var icon: Drawable? = null
        set(value) {
            field = value
            tabView?.updateView()
        }

    /**
     * Sets/return the text displayed on this tab.
     *
     * @return The tab's text
     */
    var text: CharSequence = ""

    /**
     * Sets/return the selected color of this tab. If color is not set return Color.GREEN
     *
     * @return The tab's selected color
     */
    var selectedColor: Int = Color.GREEN

    /**
     * Sets/return the current position of this tab in the action bar.
     *
     * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in
     * the action bar.
     */
    var position: Int = INVALID_POSITION

    var selectTimeStamp: Long = 0

    /**
     * Returns true if this tab is currently selected.
     */
    var isSelected: Boolean = false
        set(value) {
            field = value
            selectTimeStamp = System.currentTimeMillis()
            tabView?.updateView()
        }

}