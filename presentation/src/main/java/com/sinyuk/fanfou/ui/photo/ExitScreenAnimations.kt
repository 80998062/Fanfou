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

import android.animation.*
import android.graphics.Matrix
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.sinyuk.fanfou.ui.photo.PhotoDetailsView.Companion.IMAGE_TRANSLATION_DURATION

/**
 * Created by danylo.volokh on 3/16/16.
 *
 */
class ExitScreenAnimations(private val mAnimatedImage: ImageView, private val mImageTo: ImageView, private val mMainContainer: View)  {

    /**
     * These values represent the final position of a Image that is translated
     */
    private var mToTop: Int = 0
    private var mToLeft: Int = 0
    private var mToWidth: Int = 0
    private var mToHeight: Int = 0

    var mExitingAnimation: AnimatorSet? = null

    private var mToThumbnailMatrixValues: FloatArray? = null

    fun playExitAnimations(toTop: Int, toLeft: Int, toWidth: Int, toHeight: Int, toThumbnailMatrixValues: FloatArray) {
        mToTop = toTop
        mToLeft = toLeft
        mToWidth = toWidth
        mToHeight = toHeight

        mToThumbnailMatrixValues = toThumbnailMatrixValues

        Log.v(TAG, "playExitAnimations, mExitingAnimation " + mExitingAnimation)
        if (mExitingAnimation == null) playExitingAnimation()
    }

    private fun playExitingAnimation() {
        Log.v(TAG, "playExitingAnimation")

        mAnimatedImage.visibility = View.VISIBLE
        mImageTo.visibility = View.INVISIBLE

        val imageAnimatorSet = createExitingImageAnimation()

        val mainContainerFadeAnimator = createExitingFadeAnimator()

        mExitingAnimation = AnimatorSet()
        mExitingAnimation?.duration = IMAGE_TRANSLATION_DURATION
        mExitingAnimation?.interpolator = FastOutSlowInInterpolator()
        mExitingAnimation?.playTogether(imageAnimatorSet, mainContainerFadeAnimator)
        mExitingAnimation?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                animationListener?.onAnimationEnd()
                mExitingAnimation = null
            }
        })
        mExitingAnimation?.start()
    }

    /**
     * This method creates an animator set of 2 animations:
     * 1. ImageView position animation when screen is closed
     * 2. ImageView image matrix animation when screen is closed
     */
    private fun createExitingImageAnimation(): AnimatorSet {
        Log.v(TAG, ">> createExitingImageAnimation")

        val positionAnimator = createExitingImagePositionAnimator()
        val matrixAnimator = createExitingImageMatrixAnimator()

        val exitingImageAnimation = AnimatorSet()
        exitingImageAnimation.playTogether(positionAnimator, matrixAnimator)

        Log.v(TAG, "<< createExitingImageAnimation")
        return exitingImageAnimation
    }

    /**
     * This method creates an animator that changes ImageView position on the screen.
     * It will look like view is translated from its position on this screen to its position on previous screen
     */
    private fun createExitingImagePositionAnimator(): ObjectAnimator {

        // get initial location on the screen and start animation from there
        val locationOnScreen = IntArray(2)
        mAnimatedImage.getLocationOnScreen(locationOnScreen)
        val propertyLeft = PropertyValuesHolder.ofInt("left", locationOnScreen[0], mToLeft)
        val propertyTop = PropertyValuesHolder.ofInt("top", locationOnScreen[1], mToTop)
        val propertyRight = PropertyValuesHolder.ofInt("right", locationOnScreen[0] + mAnimatedImage.width, mToLeft + mToWidth)
        val propertyBottom = PropertyValuesHolder.ofInt("bottom", mAnimatedImage.bottom, mToTop + mToHeight)
        return ObjectAnimator.ofPropertyValuesHolder(mAnimatedImage, propertyLeft, propertyTop, propertyRight, propertyBottom)
    }


    /**
     * This method creates animator that animates Matrix of ImageView.
     * It is needed in order to show the effect when scaling of one view is smoothly changed to the scale of the second view.
     *
     *
     * For example: first view can have scaleType: centerCrop, and the other one fitCenter.
     * The image inside ImageView will smoothly change from one to another
     */
    private fun createExitingImageMatrixAnimator(): ObjectAnimator {

        val initialMatrix = MatrixUtils.getImageMatrix(mAnimatedImage)

        val endMatrix = Matrix()
        endMatrix.setValues(mToThumbnailMatrixValues)

        Log.v(TAG, "createExitingImageMatrixAnimator, initialMatrix " + initialMatrix!!)
        Log.v(TAG, "createExitingImageMatrixAnimator,     endMatrix " + endMatrix)

        mAnimatedImage.scaleType = ImageView.ScaleType.MATRIX

        return ObjectAnimator.ofObject(mAnimatedImage, MatrixEvaluator.ANIMATED_TRANSFORM_PROPERTY,
                MatrixEvaluator(), initialMatrix, endMatrix)
    }

    private fun createExitingFadeAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(mMainContainer, "alpha", 1.0f, 0.0f)
    }

    companion object {
        private val TAG = ExitScreenAnimations::class.java.simpleName
    }


    var animationListener: AnimationListener? = null

    interface AnimationListener {
        fun onAnimationEnd()
    }

}