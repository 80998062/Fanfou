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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.BitmapPalette.Profile.MUTED_DARK
import com.github.florent37.glidepalette.GlidePalette
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.DragPhotoView
import com.sinyuk.fanfou.ui.status.StatusView
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.photo_details_view.*
import me.yokeyword.fragmentation.anim.FragmentAnimator


/**
 * Created by sinyuk on 2018/2/12.
 *
 */
class PhotoDetailsView : AbstractFragment(), Injectable {

    companion object {
        fun newInstance(status: Status, thumbnailInfo: ThumbnailInfo, bitmap: Bitmap) = PhotoDetailsView().apply {
            arguments = Bundle().apply {
                putParcelable("status", status)
                putSerializable("thumbnailInfo", thumbnailInfo)
                putParcelable("bitmap", bitmap)
            }
        }

        const val TAG = "PhotoView"

        const val IMAGE_TRANSLATION_DURATION: Long = 300
    }

    override fun layoutId() = R.layout.photo_details_view

    private lateinit var status: Status
    private lateinit var thumbnailInfo: ThumbnailInfo
    private var enterScreenAnimations: EnterScreenAnimations? = null
    private var exitScreenAnimations: ExitScreenAnimations? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.setBackgroundColor(Color.TRANSPARENT)

        status = arguments!!.getParcelable("status")
        status.text?.let { FanfouUtils.parseAndSetText(content, it) }
        content.setOnClickListener {
            val file = arguments!!.getParcelable<Bitmap>("bitmap")
            Bundle().apply {
                putInt("w", file.width)
                putInt("h", file.height)
                startWithPop(StatusView.newInstance(status = status, photoExtra = this))
            }

        }
        thumbnailInfo = arguments!!.getSerializable("thumbnailInfo") as ThumbnailInfo
        if (savedInstanceState != null) {
            // Activity is retrieved. Make the background visible
            onEnteringAnimationEnd()
        } else {
            // We entered activity for the first time.
            // Initialize Image view that will be transitioned
            initializeTransitionView()
        }

        enterScreenAnimations = EnterScreenAnimations(transitionImageView, photo, background)
        exitScreenAnimations = ExitScreenAnimations(transitionImageView, photo, background)
        renderBackground(savedInstanceState)
    }

    private fun onEnteringAnimationEnd() {
        background.alpha = 1f
        photo.visibility = View.VISIBLE
    }

    private fun renderBackground(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            loadPhotoAsync(savedInstanceState)
            return
        }

        val file = arguments!!.getParcelable<Bitmap>("bitmap")
        val thumbnail = status.photos?.size(ConvertUtils.dp2px(context, Photos.SMALL_SIZE))
        Glide.with(background).load(file)
                .listener(GlidePalette.with(thumbnail)
                        .use(MUTED_DARK)
                        .setGlideListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                loadPhotoAsync(null)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                loadPhotoAsync(null)
                                return false
                            }
                        })
                        .crossfade(false)
                        .intoBackground(background, BitmapPalette.Swatch.RGB))
                .preload()
    }

    private fun loadPhotoAsync(savedInstanceState: Bundle?) {
        val file = arguments!!.getParcelable<Bitmap>("bitmap")
        GlideApp.with(this@PhotoDetailsView)
                .load(file)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Log.v(TAG, "onLoadFailed")
                        onEnteringAnimationEnd()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Log.v(TAG, "onResourceReady")
                        resource?.apply { if (savedInstanceState == null) runEnteringAnimation() }
                        // Else activity was retrieved from recent apps. No animation needed, just load the image

                        setupPhotoView()
                        return false
                    }
                })
                .dontAnimate()
                .dontTransform()
                .into(photo)
    }

    private fun setupPhotoView() {
        photo.mOnTapListener = object : DragPhotoView.OnTapListener {
            override fun onTap(view: DragPhotoView) {
                exit()
            }
        }

        photo.mDismissListener = object : DragPhotoView.OnDismissListener {
            override fun onDismiss(view: DragPhotoView) {
                pop()
            }
        }

        photo.mUpdateListener = object : DragPhotoView.OnUpdateListener {
            override fun onUpdated(view: DragPhotoView, fraction: Float, translateY: Float) {
                background.alpha = 1 - fraction
            }
        }

        navBack.setOnClickListener { exit() }
    }

    private fun runEnteringAnimation() {
        photo.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            var mFrames = 0

            override fun onPreDraw(): Boolean {
                // When this method is called we already have everything laid out and measured so we can start our animation
                Log.v(TAG, "onPreDraw, mFrames " + mFrames)
                when (mFrames++) {
                    0 -> {
                        // Start animation on first frame
                        val finalLocationOnTheScreen = IntArray(2)
                        photo.getLocationOnScreen(finalLocationOnTheScreen)
                        Log.v(TAG, "onPreDraw,  " + finalLocationOnTheScreen[0] + ", " + finalLocationOnTheScreen[1] + ", " + photo.width + ", " + photo.height)
                        enterScreenAnimations?.animationListener = object : EnterScreenAnimations.AnimationListener {
                            override fun onAnimationEnd() {
                                showComponents()
                            }
                        }
                        enterScreenAnimations?.playEnteringAnimation(finalLocationOnTheScreen[0], finalLocationOnTheScreen[1], photo.width, photo.height)
                        return true
                    }
                    1 -> return true //  Do nothing. We just draw this frame
                }
                photo.viewTreeObserver.removeOnPreDrawListener(this)
                Log.v(TAG, "onPreDraw, << mFrames " + mFrames)
                return true
            }
        })
    }

    private val footerTranslationY: Float
        get() = if (content.height != 0) content.height.toFloat() else 200f


    private val headerTranslationY: Float
        get() = if (fakeActionBar.height != 0) fakeActionBar.height.toFloat() else 200f

    private val componentShowAnimator: AnimatorSet
        get () {
            val animatorSet = AnimatorSet()
            val animTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            animatorSet.duration = animTime
            animatorSet.interpolator = FastOutSlowInInterpolator()
            val fadeHeader = ObjectAnimator.ofFloat(fakeActionBar, View.ALPHA, 0f, 1f)
            val slideHeader = ObjectAnimator.ofFloat(fakeActionBar, View.TRANSLATION_Y, -headerTranslationY, 0f)
            val fadeFooter = ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f)
            val slideFooter = ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, footerTranslationY, 0f)

            animatorSet.playTogether(fadeFooter, fadeHeader, slideFooter, slideHeader)

            animatorSet.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator?) {
                    fakeActionBar.visibility = View.VISIBLE
                    content.visibility = View.VISIBLE
                }
            })
            return animatorSet
        }

    private val componentHideAnimator: AnimatorSet
        get () {
            val animatorSet = AnimatorSet()
            val animTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            animatorSet.duration = animTime
            animatorSet.interpolator = FastOutSlowInInterpolator()
            val fadeHeader = ObjectAnimator.ofFloat(fakeActionBar, View.ALPHA, 1f, 0f)
            val slideHeader = ObjectAnimator.ofFloat(fakeActionBar, View.TRANSLATION_Y, 0f, -headerTranslationY)
            val fadeFooter = ObjectAnimator.ofFloat(fakeActionBar, View.ALPHA, 1f, 0f)
            val slideFooter = ObjectAnimator.ofFloat(fakeActionBar, View.TRANSLATION_Y, 0f, footerTranslationY)

            animatorSet.playTogether(fadeFooter, fadeHeader, slideFooter, slideHeader)

            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (view == null) return
                    fakeActionBar.visibility = View.GONE
                    content.visibility = View.GONE
                }
            })
            return animatorSet
        }

    private fun showComponents() {
        if (!componentShowAnimator.isRunning) componentShowAnimator.start()
    }

    private fun hideComponents() {
        if (!componentHideAnimator.isRunning) componentHideAnimator.start()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator {
        return FragmentAnimator(0, 0, 0, 0)
    }

    private lateinit var transitionImageView: ImageView

    /**
     *
     */
    private fun initializeTransitionView() {
        Log.v(TAG, "initializeTransitionView")
        Log.v(TAG, thumbnailInfo.toString())

        transitionImageView = ImageView(context)

        // We set initial margins to the view so that it was situated at exact same spot that view from the previous screen were.
        val layoutParams = FrameLayout.LayoutParams(0, 0)
        layoutParams.height = thumbnailInfo.rect.height()
        layoutParams.width = thumbnailInfo.rect.width()
        layoutParams.setMargins(thumbnailInfo.rect.left, thumbnailInfo.rect.top, 0, 0)
        rootView.addView(transitionImageView, layoutParams)

        transitionImageView.scaleType = thumbnailInfo.scaleType
        val file = arguments!!.getParcelable<Bitmap>("bitmap")
        GlideApp.with(this).load(file).dontTransform().dontAnimate().into(transitionImageView)
    }

    private fun exit() {
        enterScreenAnimations?.cancelRunningAnimations()
        thumbnailInfo.rect.apply {
            exitScreenAnimations?.animationListener = object : ExitScreenAnimations.AnimationListener {
                override fun onAnimationEnd() {
                    pop()
                }
            }
            exitScreenAnimations?.playExitAnimations(top, left, width(), height(), enterScreenAnimations?.initialThumbnailMatrixValues!!)
        }
    }


}