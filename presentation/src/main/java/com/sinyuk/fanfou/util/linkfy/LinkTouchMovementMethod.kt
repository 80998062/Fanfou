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

package com.sinyuk.fanfou.util.linkfy

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.MotionEvent
import android.widget.TextView

/**
 * A movement method that only highlights any touched
 * [TouchableUrlSpan]s
 *
 *
 * Adapted from  http://stackoverflow.com/a/20905824
 */
class LinkTouchMovementMethod : LinkMovementMethod() {
    private var pressedSpan: TouchableUrlSpan? = null

    override fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {

        // Reference to
        // @link:{http://angeldevil.me/2015/09/08/Two-problems-about-LinkMovementMethod-and-URLSPan/}

        // LinkMovementMethod导致TextView可滚动，可能使文本错位
        val action = event.action
        if (action == MotionEvent.ACTION_MOVE) {
            // Prevent scroll event
            return true
        }

        var handled = false
        if (event.action == MotionEvent.ACTION_DOWN) {
            pressedSpan = getPressedSpan(textView, spannable, event)
            if (pressedSpan != null) {
                pressedSpan!!.setPressed(true)
                Selection.setSelection(spannable, spannable.getSpanStart(pressedSpan),
                        spannable.getSpanEnd(pressedSpan))
                handled = true
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val touchedSpan = getPressedSpan(textView, spannable, event)
            if (pressedSpan != null && touchedSpan != pressedSpan) {
                pressedSpan!!.setPressed(false)
                pressedSpan = null
                Selection.removeSelection(spannable)
            }
        } else {
            if (pressedSpan != null) {
                pressedSpan!!.setPressed(false)
                super.onTouchEvent(textView, spannable, event)
                handled = true
            }
            pressedSpan = null
            Selection.removeSelection(spannable)
        }
        return handled
    }

    private fun getPressedSpan(textView: TextView, buffer: Spannable, event: MotionEvent): TouchableUrlSpan? {

        var x = event.x.toInt()
        var y = event.y.toInt()

        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop

        x += textView.scrollX
        y += textView.scrollY

        val layout = textView.layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())


        val link = buffer.getSpans(off, off, TouchableUrlSpan::class.java)

        var touchedSpan: TouchableUrlSpan? = null


        if (link.isNotEmpty()) touchedSpan = link[0]

        return touchedSpan
    }

    companion object {
        private var instance: LinkTouchMovementMethod? = null
        fun getInstance(): MovementMethod {
            if (instance == null) {
                synchronized(LinkTouchMovementMethod::class.java) {
                    if (instance == null)
                        instance = LinkTouchMovementMethod()
                }
            }
            return instance!!
        }
    }

}