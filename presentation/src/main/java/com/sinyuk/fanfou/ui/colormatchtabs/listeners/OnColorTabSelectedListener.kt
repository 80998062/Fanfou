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

package com.sinyuk.fanfou.ui.colormatchtabs.listeners

import com.sinyuk.fanfou.ui.colormatchtabs.model.ColorTab

/**
 * Created by anna on 11.05.17.
 *
 */
/**
 * Callback interface invoked when a tab's selection state changes.
 */
interface OnColorTabSelectedListener {

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    fun onSelectedTab(tab: ColorTab?)


    /**
     * Called when a tab was reselected
     *
     * @param tab The tab that was clicked
     */
    fun onReSelectedTab(tab: ColorTab?)

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    fun onUnselectedTab(tab: ColorTab?)


    fun onDoubleClick(tab: ColorTab?)
}