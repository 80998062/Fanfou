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
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.fanfou.util.ObservableColorMatrix
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.photo_grid_list_item.view.*


/**
 * Created by sinyuk on 2018/2/24.
 *
 */
class PhotoGridItemHolder(private val view: View, private val glide: GlideRequests, private val fragment: Fragment) : BaseViewHolder(view) {

    private val initialGifBadgeColor = ContextCompat.getColor(fragment.context!!, R.color.scrim)
    private val itemBackground = ContextCompat.getColor(fragment.context!!, R.color.itemBackground)
    private val windowBackground = ContextCompat.getColor(fragment.context!!, R.color.windowBackground)

    private val placeholders = mutableListOf(ColorDrawable(itemBackground), ColorDrawable(windowBackground))

    fun bind(status: Status) {
        view.image.setBadgeColor(initialGifBadgeColor)
        view.image.setOnClickListener {
            val rect = Rect()
            view.image.getGlobalVisibleRect(rect)
            val thumbnailInfo = ThumbnailInfo(rect, view.image.scaleType)
            gotoPhotoView(status, thumbnailInfo)
        }
        // play animated GIFs whilst touched
        view.image.setOnTouchListener { _, event ->
            // check if it's an event we care about, else bail fast
            val action = event.action
            if (!(action == MotionEvent.ACTION_DOWN
                            || action == MotionEvent.ACTION_UP
                            || action == MotionEvent.ACTION_CANCEL)) return@setOnTouchListener false
            // get the image and check if it's an animated GIF
            val drawable = view.image.drawable ?: return@setOnTouchListener false
            var gif: GifDrawable? = null
            if (drawable is GifDrawable) {
                gif = drawable
            } else if (drawable is TransitionDrawable) {
                // we fade in images on load which uses a TransitionDrawable; check its layers
                for (i in 0 until drawable.numberOfLayers) {
                    if (drawable.getDrawable(i) is GifDrawable) {
                        gif = drawable.getDrawable(i) as GifDrawable
                        break
                    }
                }
            }
            if (gif == null) return@setOnTouchListener false
            // GIF found, start/stop it on press/lift
            when (action) {
                MotionEvent.ACTION_DOWN -> gif.start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> gif.stop()
            }
            return@setOnTouchListener false
        }

        val url = status.photos?.size(ConvertUtils.dp2px(fragment.context, Photos.SMALL_SIZE))
        glide.asBitmap().load(url)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        if (!status.photos!!.hasFadedIn) {
                            view.image.setHasTransientState(true)
                            val cm = ObservableColorMatrix()
                            val saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f)
                            saturation.addUpdateListener {
                                // just animating the color matrix does not invalidate the
                                // drawable so need this update listener.  Also have to create a
                                // new CMCF as the matrix is immutable :(
                                view.image.colorFilter = ColorMatrixColorFilter(cm)
                            }
                            saturation.duration = 1000L
                            saturation.interpolator = FastOutSlowInInterpolator()
                            saturation.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    view.image.clearColorFilter()
                                    view.image.setHasTransientState(false)
                                }
                            })
                            saturation.start()
                            status.photos!!.hasFadedIn = true
                        }
                        return false
                    }
                })
                .placeholder(placeholders[adapterPosition % 2])
                .transition(withCrossFade())
                .into(view.image)

        // need both placeholder & background to prevent seeing through shot as it fades in
        view.image.background = placeholders[adapterPosition % 2]
        view.image.drawBadge = Photos.isAnimated(status.photos?.size()) ?: false

    }

    fun clear() {
        view.image.setOnClickListener(null)
        glide.clear(view.image)
    }


    /**
     * 这里要再加载一次原图，因为列表中的图是裁剪过的
     *
     */
    private fun gotoPhotoView(status: Status, thumbnailInfo: ThumbnailInfo) {
        glide.asBitmap().load(status.photos?.size(thumbnailInfo.rect.width()))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        (view.context as AbstractActivity).extraTransaction()
                                .setCustomAnimations(0, 0, 0, 0)
                                .startDontHideSelf(PhotoDetailsView.newInstance(status, thumbnailInfo, resource!!))
                        return false
                    }
                })
                .preload()
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, fragment: Fragment): PhotoGridItemHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.photo_grid_list_item, parent, false)
            return PhotoGridItemHolder(view, glide, fragment)
        }
    }
}