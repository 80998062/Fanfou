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

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.MotionEvent

class ClickableTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor(context: Context) : super(context)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (hasOnClickListeners()) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> isSelected = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isSelected = false
            }
        }
        // allow target view to handle click
        return super.onTouchEvent(event)
    }
}