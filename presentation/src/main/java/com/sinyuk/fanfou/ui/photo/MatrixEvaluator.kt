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

import android.animation.TypeEvaluator
import android.graphics.Matrix
import android.util.Property
import android.widget.ImageView

/**
 * This class is passed to ObjectAnimator in order to animate changes in ImageView image matrix
 */
class MatrixEvaluator : TypeEvaluator<Matrix> {

    private var mTempStartValues = FloatArray(9)

    private var mTempEndValues = FloatArray(9)

    private var mTempMatrix = Matrix()

    override fun evaluate(fraction: Float, startValue: Matrix, endValue: Matrix): Matrix {
        startValue.getValues(mTempStartValues)
        endValue.getValues(mTempEndValues)
        for (i in 0..8) {
            val diff = mTempEndValues[i] - mTempStartValues[i]
            mTempEndValues[i] = mTempStartValues[i] + fraction * diff
        }
        mTempMatrix.setValues(mTempEndValues)

        return mTempMatrix
    }

    companion object {

        private val TAG = MatrixEvaluator::class.java.simpleName

        var NULL_MATRIX_EVALUATOR: TypeEvaluator<Matrix> = TypeEvaluator { _, _, _ -> null }

        /**
         * This property is passed to ObjectAnimator when we are animating image matrix of ImageView
         */
        val ANIMATED_TRANSFORM_PROPERTY: Property<ImageView, Matrix> = object : Property<ImageView, Matrix>(Matrix::class.java,
                "animatedTransform") {

            /**
             * This is copy-paste form ImageView#animateTransform - method is invisible in sdk
             */
            override fun set(imageView: ImageView, matrix: Matrix?) {
                val drawable = imageView.drawable ?: return
                if (matrix == null) {
                    drawable.setBounds(0, 0, imageView.width, imageView.height)
                } else {

                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    var drawMatrix: Matrix? = imageView.imageMatrix
                    if (drawMatrix == null) {
                        drawMatrix = Matrix()
                        imageView.imageMatrix = drawMatrix
                    }
                    imageView.imageMatrix = matrix
                }
                imageView.invalidate()
            }

            override fun get(`object`: ImageView): Matrix? {
                return null
            }
        }
    }
}