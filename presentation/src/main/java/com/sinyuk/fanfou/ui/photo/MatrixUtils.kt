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

package com.sinyuk.fanfou.ui.photo

import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import android.widget.ImageView

/**
 * Created by danylo.volokh on 3/14/16.
 *
 */
object MatrixUtils {

    private val TAG = MatrixUtils::class.java.simpleName

    fun getImageMatrix(imageView: ImageView): Matrix? {
        Log.v(TAG, "getImageMatrix, imageView " + imageView)

        val left = imageView.left
        val top = imageView.top
        val right = imageView.right
        val bottom = imageView.bottom

        val bounds = Rect(left, top, right, bottom)

        val drawable = imageView.drawable

        var matrix: Matrix?
        val scaleType = imageView.scaleType
        Log.v(TAG, "getImageMatrix, scaleType " + scaleType)

        if (scaleType == ImageView.ScaleType.FIT_XY) {
            matrix = imageView.imageMatrix
            if (!matrix!!.isIdentity) {
                matrix = Matrix(matrix)
            } else {
                val drawableWidth = drawable.intrinsicWidth
                val drawableHeight = drawable.intrinsicHeight
                if (drawableWidth > 0 && drawableHeight > 0) {
                    val scaleX = bounds.width().toFloat() / drawableWidth
                    val scaleY = bounds.height().toFloat() / drawableHeight
                    matrix = Matrix()
                    matrix.setScale(scaleX, scaleY)
                } else {
                    matrix = null
                }
            }
        } else {
            matrix = Matrix(imageView.imageMatrix)
        }

        return matrix
    }
}