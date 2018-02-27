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
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*

/**
 * Created by danylo.volokh on 3/16/16.
 */
class EnterScreenAnimations(private val mAnimatedImage: ImageView, private val mImageTo: ImageView, private val mMainContainer: View) {

    private var mEnteringAnimation: AnimatorSet? = null

    /**
     * This array will contain the data about image matrix of original image.
     * We will use that matrix to animate image back to original state
     */
    val initialThumbnailMatrixValues = FloatArray(9)

    /**
     * These values represent the final position of a Image that is translated
     */
    private var mToTop: Int = 0
    private var mToLeft: Int = 0
    private var mToWidth: Int = 0
    private var mToHeight: Int = 0

    /**
     * This method combines several animations when screen is opened.
     * 1. Animation of ImageView position and image matrix.
     * 2. Animation of main container elements - they are fading in after image is animated to position
     */
    fun playEnteringAnimation(left: Int, top: Int, width: Int, height: Int) {
        Log.v(TAG, ">> playEnteringAnimation")

        mToLeft = left
        mToTop = top
        mToWidth = width
        mToHeight = height

        val imageAnimatorSet = createEnteringImageAnimation()

        val mainContainerFadeAnimator = createEnteringFadeAnimator()

        mEnteringAnimation = AnimatorSet()
        mEnteringAnimation!!.duration = PhotoDetailsView.IMAGE_TRANSLATION_DURATION
        mEnteringAnimation!!.interpolator = FastOutSlowInInterpolator()
        mEnteringAnimation!!.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator) {
                Log.v(TAG, "onAnimationCancel, mEnteringAnimation " + mEnteringAnimation)
                mEnteringAnimation = null
            }

            override fun onAnimationEnd(animation: Animator) {
                Log.v(TAG, "onAnimationEnd, mEnteringAnimation " + mEnteringAnimation)
                if (mEnteringAnimation != null) {
                    mEnteringAnimation = null
                    mImageTo.visibility = View.VISIBLE
                    mAnimatedImage.visibility = View.INVISIBLE
                }
                animationListener?.onAnimationEnd()
            }
        })

        mEnteringAnimation!!.playTogether(
                imageAnimatorSet,
                mainContainerFadeAnimator
        )

        mEnteringAnimation!!.start()
        Log.v(TAG, "<< playEnteringAnimation")
    }

    /**
     * Animator returned form this method animates fade in of all other elements on the screen besides ImageView
     */
    private fun createEnteringFadeAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(mMainContainer, "alpha", 0.0f, 1.0f)
    }

    /**
     * This method creates an animator set of 2 animations:
     * 1. ImageView position animation when screen is opened
     * 2. ImageView image matrix animation when screen is opened
     */
    private fun createEnteringImageAnimation(): AnimatorSet {
        Log.v(TAG, ">> createEnteringImageAnimation")

        val positionAnimator = createEnteringImagePositionAnimator()
        val matrixAnimator = createEnteringImageMatrixAnimator()

        val enteringImageAnimation = AnimatorSet()
        enteringImageAnimation.playTogether(positionAnimator, matrixAnimator)

        Log.v(TAG, "<< createEnteringImageAnimation")
        return enteringImageAnimation
    }

    /**
     * This method creates an animator that changes ImageView position on the screen.
     * It will look like view is translated from its position on previous screen to its new position on this screen
     */
    private fun createEnteringImagePositionAnimator(): ObjectAnimator {

        Log.v(TAG, "createEnteringImagePositionAnimator")

        val propertyLeft = PropertyValuesHolder.ofInt("left", mAnimatedImage.left, mToLeft)
        val propertyTop = PropertyValuesHolder.ofInt("top", mAnimatedImage.top, mToTop)
        val propertyRight = PropertyValuesHolder.ofInt("right", mAnimatedImage.right, mToLeft + mToWidth)
        val propertyBottom = PropertyValuesHolder.ofInt("bottom", mAnimatedImage.bottom, mToTop + mToHeight)

        val animator = ObjectAnimator.ofPropertyValuesHolder(mAnimatedImage, propertyLeft, propertyTop, propertyRight, propertyBottom)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // set new parameters of animated ImageView. This will prevent blinking of view when set visibility to visible in Exit animation
                val layoutParams = mAnimatedImage.layoutParams as FrameLayout.LayoutParams
                layoutParams.height = mImageTo.height
                layoutParams.width = mImageTo.width
                layoutParams.setMargins(mToLeft, mToTop, 0, 0)
            }
        })
        return animator
    }

    /**
     * This method creates Animator that will animate matrix of ImageView.
     * It is needed in order to show the effect when scaling of one view is smoothly changed to the scale of the second view.
     *
     *
     * For example: first view can have scaleType: centerCrop, and the other one fitCenter.
     * The image inside ImageView will smoothly change from one to another
     */
    private fun createEnteringImageMatrixAnimator(): ObjectAnimator {

        val initMatrix = MatrixUtils.getImageMatrix(mAnimatedImage)
        // store the data about original matrix into array.
        // this array will be used later for exit animation
        initMatrix?.getValues(initialThumbnailMatrixValues)

        val endMatrix = MatrixUtils.getImageMatrix(mImageTo)
        Log.v(TAG, "createEnteringImageMatrixAnimator, mInitThumbnailMatrixValues " + Arrays.toString(initialThumbnailMatrixValues))
        Log.v(TAG, "createEnteringImageMatrixAnimator, initMatrix " + initMatrix)
        Log.v(TAG, "createEnteringImageMatrixAnimator,  endMatrix " + endMatrix)

        mAnimatedImage.scaleType = ImageView.ScaleType.MATRIX

        return ObjectAnimator.ofObject(mAnimatedImage, MatrixEvaluator.ANIMATED_TRANSFORM_PROPERTY,
                MatrixEvaluator(), initMatrix, endMatrix)
    }

    @Suppress("unused")
    fun cancelRunningAnimations() {
        Log.v(TAG, "cancelRunningAnimations, mEnteringAnimation " + mEnteringAnimation)
        mEnteringAnimation?.cancel()
        mEnteringAnimation = null
    }

    companion object {
        private val TAG = EnterScreenAnimations::class.java.simpleName
    }

    var animationListener: AnimationListener? = null

    interface AnimationListener {
        fun onAnimationEnd()
    }
}