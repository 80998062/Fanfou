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
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Created by sinyuk on 2017/11/28.
 *
 */
class MarginDecoration(@DimenRes resId: Int, private val includeEdge: Boolean, context: Context) : RecyclerView.ItemDecoration() {
    private val mSpace: Int = context.resources.getDimensionPixelOffset(resId)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position

        if (includeEdge) { // 如果边缘也要有边距
            outRect.left = mSpace
            outRect.right = mSpace
        }

        if (position == 0) { // top edge
            outRect.top = mSpace
        }
        outRect.bottom = mSpace // item bottom
    }
}