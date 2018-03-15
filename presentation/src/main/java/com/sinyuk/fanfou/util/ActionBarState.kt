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

import android.os.Bundle
import android.support.annotation.DrawableRes

/**
 * Created by sinyuk on 2018/3/6.
 *
 */
data class ActionBarUi
@JvmOverloads constructor(var title: String? = null,
                          var subTitle: String? = null,
                          @DrawableRes var startButtonDrawable: Int?,
                          var startButtonType: Int = ActionButton.Avatar,
                          var endButtonType: Int = ActionButton.Rice,
                          @DrawableRes var endButtonDrawable: Int?,
                          var displayedChildIndex: Int = 0) {


    companion object {
        const val TITLE = "title"
        const val DISPLAYED_CHILD_INDEX = "displayedChildIndex"
        const val SUBTITLE = "subTitle"
        const val START_BUTTON_DRAWABLE = "startButtonDrawable"
        const val START_BUTTON_TYPE = "startButtonType"
        const val END_BUTTON_DRAWABLE = "endButtonDrawable"
        const val END_BUTTON_TYPE = "endButtonType"

    }


    fun update(payLoads: PayLoads): Boolean {
        if (payLoads.get().isEmpty) return false
        if (payLoads.get().containsKey(TITLE)) title = payLoads.get().getString(TITLE)
        if (payLoads.get().containsKey(DISPLAYED_CHILD_INDEX)) displayedChildIndex = payLoads.get().getInt(DISPLAYED_CHILD_INDEX)
        if (payLoads.get().containsKey(SUBTITLE)) subTitle = payLoads.get().getString(SUBTITLE)
        if (payLoads.get().containsKey(START_BUTTON_DRAWABLE)) startButtonDrawable = payLoads.get().getInt(START_BUTTON_DRAWABLE)
        if (payLoads.get().containsKey(START_BUTTON_TYPE)) startButtonType = payLoads.get().getInt(START_BUTTON_TYPE)
        if (payLoads.get().containsKey(END_BUTTON_DRAWABLE)) endButtonDrawable = payLoads.get().getInt(END_BUTTON_DRAWABLE)
        if (payLoads.get().containsKey(END_BUTTON_TYPE)) endButtonType = payLoads.get().getInt(END_BUTTON_TYPE)
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is ActionBarUi) {
            if (other.displayedChildIndex == displayedChildIndex &&
                    other.title == title &&
                    other.subTitle == subTitle &&
                    other.startButtonDrawable == startButtonDrawable &&
                    other.startButtonType == startButtonType &&
                    other.endButtonDrawable == endButtonDrawable &&
                    other.endButtonType == endButtonType) {
                return true
            }
        }
        return false
    }

    class PayLoads constructor(private val bundle: Bundle = Bundle()) {

        fun title(title: String?): PayLoads {
            bundle.putString(TITLE, title)
            return this@PayLoads
        }

        fun subTitle(subTitle: String?): PayLoads {
            bundle.putString(SUBTITLE, subTitle)
            return this@PayLoads
        }

        fun displayedChildIndex(displayedChildIndex: Int): PayLoads {
            bundle.putInt(DISPLAYED_CHILD_INDEX, displayedChildIndex)
            return this@PayLoads
        }

        fun startButtonDrawable(@DrawableRes drawable: Int): PayLoads {
            bundle.putInt(START_BUTTON_DRAWABLE, drawable)
            return this@PayLoads
        }

        fun endButtonDrawable(@DrawableRes drawable: Int): PayLoads {
            bundle.putInt(END_BUTTON_DRAWABLE, drawable)
            return this@PayLoads
        }

        fun startButtonType(type: Int): PayLoads {
            bundle.putInt(START_BUTTON_TYPE, type)
            return this@PayLoads
        }

        fun endButtonType(type: Int): PayLoads {
            bundle.putInt(END_BUTTON_TYPE, type)
            return this@PayLoads
        }


        fun get(): Bundle = bundle
    }
}


object ActionButton {
    const val Avatar = 1
    const val Back = 2
    const val Rice = 3
    const val SearchClose = 4
    const val AddFriend = 5
    const val Settings = 6
    const val Send = 7
}






