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

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ViewAnimator

/**
 * Custom View Animator which is more easier to be uesd
 */
class BetterViewAnimator(context: Context, attrs: AttributeSet) : ViewAnimator(context, attrs) {

    companion object {
        const val TAG = "BetterViewAnimator"
    }

    var displayedChildId: Int
        get() = getChildAt(displayedChild).id
        set(id) {
            Log.d(TAG, "displayedChild: $id")
            if (displayedChildId == id) {
                return
            }
            var i = 0
            val count = childCount
            Log.d(TAG, "childCount: $childCount")

            while (i < count) {
                if (getChildAt(i).id == id) {
                    displayedChild = i
                    Log.d(TAG, "index: $i")
                    return
                }
                i++
            }
            val name = resources.getResourceEntryName(id)
            throw IllegalArgumentException("No view with ID $name")
        }
}
