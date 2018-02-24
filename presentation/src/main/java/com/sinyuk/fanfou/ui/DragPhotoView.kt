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

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.sinyuk.myutils.system.ScreenUtils


class DragPhotoView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0) : AppCompatImageView(context, attr, defStyle), View.OnTouchListener {

    private val mPaint: Paint = Paint().apply { color = Color.TRANSPARENT }
    private var mDownX = 0f
    private var mDownY = 0f

    private var mTranslateY = 0f
    private var mWidth = 0f
    private var mHeight = 0f
    private var isAnimate = false
    private var isTouchEvent = false
    private var mScreenHeight = 0

    var mDismissListener: OnDismissListener? = null
    var mOnTapListener: OnTapListener? = null


    private val translateYAnimation: ValueAnimator
        get() {
            val animator = ValueAnimator.ofFloat(mTranslateY, 0.toFloat())
            animator.duration = DURATION
            animator.interpolator = FastOutSlowInInterpolator()
            animator.addUpdateListener { v ->
                mTranslateY = v.animatedValue as Float
                mUpdateListener?.onUpdated(this@DragPhotoView, computeFraction(), Math.abs(mTranslateY))

                invalidate()
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    isAnimate = true
                }

                override fun onAnimationEnd(animator: Animator) {
                    isAnimate = false
                    animator.removeAllListeners()
                }

                override fun onAnimationCancel(animator: Animator) {
                }

                override fun onAnimationRepeat(animator: Animator) {

                }
            })
            return animator
        }

    private var mMaxTranslateY = 0
    private var mMaximumVelocity: Float = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()

    init {
        mScreenHeight = ScreenUtils.getScreenHeight(context)
        mMaxTranslateY = mScreenHeight / 2
        setOnTouchListener(this@DragPhotoView)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mWidth, mHeight, mPaint)
        canvas.translate(0f, mTranslateY)
        canvas.scale(1f, 1f, mWidth / 2, mHeight / 2)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
    }


    private var velocityTracker: VelocityTracker? = null
    private val mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop

    private var velocityY = 0f

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (!isAnimate) {
            val pointerId = event.getPointerId(0)
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain()
            } else {
                velocityTracker!!.clear()
            }
            velocityTracker!!.addMovement(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(event)
                MotionEvent.ACTION_MOVE -> {
                    //在ViewPager里面，如果不消费事件，则不作操作
                    val movedHorizontal = Math.abs(event.x - mDownX) > mTouchSlop
                    if (mTranslateY == 0f && movedHorizontal) {
                        if (!isTouchEvent) return false
                    }
                    if (event.pointerCount == 1) { //如果有上下位移 则不交给ViewPager处理
                        onActionMove(event)
                        isTouchEvent = Math.abs(mTranslateY) > mTouchSlop
                        velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity)
                        velocityY = Math.abs(velocityTracker!!.getYVelocity(pointerId))
                        return true
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    isTouchEvent = false
                    if (Math.abs(mTranslateY) < mTouchSlop && event.x - mDownX == 0f) {
                        mOnTapListener?.onTap(this@DragPhotoView)
                    } else if (event.pointerCount == 1) {
                        Log.v(TAG, "velocityY: " + velocityY)
                        onActionUp(event)
                    }
                    releaseVelocityTracker()
                }
            }
        }

        return true
    }


    private fun releaseVelocityTracker() {
        velocityY = 0f
        velocityTracker?.clear()
        velocityTracker?.recycle()
        velocityTracker = null
    }


    private fun onActionUp(@Suppress("UNUSED_PARAMETER") event: MotionEvent) {
        when {
            velocityY > VELOCITY_MIN || Math.abs(mTranslateY) > mMaxTranslateY -> mDismissListener?.onDismiss(this@DragPhotoView)
            else -> performAnimation()
        }
    }

    private fun computeFraction() = when {
        Math.abs(mTranslateY) <= mTouchSlop -> 0f
        Math.abs(mTranslateY) >= mMaxTranslateY -> 1f
        else -> Math.abs(mTranslateY) / mMaxTranslateY
    }

    private fun onActionMove(event: MotionEvent) {
        val moveY = event.y
        mTranslateY = moveY - mDownY
        mUpdateListener?.onUpdated(this@DragPhotoView, computeFraction(), Math.abs(mTranslateY))
        //
        invalidate()
    }

    private fun performAnimation() {
        if (!translateYAnimation.isStarted) translateYAnimation.start()
    }

    private fun onActionDown(event: MotionEvent) {
        Log.v(TAG, "onActionDown")
        mDownX = event.x
        mDownY = event.y
    }


    interface OnDismissListener {
        fun onDismiss(view: DragPhotoView)
    }


    interface OnTapListener {
        fun onTap(view: DragPhotoView)
    }

    var mUpdateListener: OnUpdateListener? = null

    interface OnUpdateListener {
        fun onUpdated(view: DragPhotoView, fraction: Float, translateY: Float)
    }

    companion object {
        const val DURATION: Long = 300
        const val VELOCITY_MIN = 1000
        const val TAG = "PhotoView"
    }
}