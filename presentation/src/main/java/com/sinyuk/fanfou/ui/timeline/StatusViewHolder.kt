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

package com.sinyuk.fanfou.ui.timeline

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.glide.GlideRequests
import com.sinyuk.fanfou.ui.photo.PhotoDetailsView
import com.sinyuk.fanfou.ui.photo.ThumbnailInfo
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.ui.status.StatusView
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import com.sinyuk.myutils.ConvertUtils
import com.sinyuk.myutils.DateUtils
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*


/**
 * Created by sinyuk on 2017/12/18.
 *
 * A RecyclerView ViewHolder that displays a status.
 */
class StatusViewHolder(private val view: View, private val glide: GlideRequests, private val uniqueId: String?, private val fragment: Fragment) : BaseViewHolder(view) {

    private val roundedCornersTransformation = RoundedCornersTransformation(ConvertUtils.dp2px(view.context, 4f), 0)
    fun bind(status: Status) {
        view.swipeLayout.isRightSwipeEnabled = true
        view.swipeLayout.isClickToClose = true
        glide.asDrawable().load(status.playerExtracts?.profileImageUrl).avatar().transition(withCrossFade()).into(view.avatar)

        when (status.playerExtracts?.uniqueId) {
            null -> view.avatar.setOnClickListener(null)
            uniqueId -> view.avatar.setOnClickListener { }
            else -> view.avatar.setOnClickListener { (view.context as AbstractActivity).start(PlayerView.newInstance(uniqueId = status.playerExtracts!!.uniqueId)) }
        }

        // Clear background
        view.screenName.background = null
        view.createdAt.background = null
        view.content.background = null
        view.screenName.text = status.playerExtracts?.screenName
        view.createdAt.text = DateUtils.getTimeAgo(view.context, status.createdAt)

        /**
         * code about imageView
         */
        // play animated GIFs whilst touched
        view.image.setOnTouchListener(View.OnTouchListener { v, event ->
            // check if it's an event we care about, else bail fast
            val action = event?.action
            if (!(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL)) {
                return@OnTouchListener false
            }
            // get the image and check if it's an animated GIF
            val drawable = (v as ImageView).drawable
            var gif: GifDrawable? = null
            if (drawable == null) return@OnTouchListener false
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

            if (gif == null) return@OnTouchListener false

            // GIF found, start/stop it on press/lift
            when (action) {
                MotionEvent.ACTION_DOWN -> gif.start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> gif.stop()
            }
            false
        })

        val url = status.photos?.size(ConvertUtils.dp2px(view.context, Photos.SMALL_SIZE))

        if (url == null) {
            view.image.setOnClickListener(null)
            view.image.visibility = View.GONE
            glide.clear(view.image)
        } else {
            view.image.visibility = View.VISIBLE
            glide.asBitmap()
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(roundedCornersTransformation).centerCrop())
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                            if (!status.photos!!.hasFadedIn) {
//                                view.image.setHasTransientState(true)
//                                val cm = ObservableColorMatrix()
//                                val saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f)
//                                saturation.addUpdateListener {
//                                    // just animating the color matrix does not invalidate the
//                                    // drawable so need this update listener.  Also have to create a
//                                    // new CMCF as the matrix is immutable :(
//                                    view.image.colorFilter = ColorMatrixColorFilter(cm)
//                                }
//                                saturation.duration = 2000L
//                                saturation.interpolator = FastOutSlowInInterpolator()
//                                saturation.addListener(object : AnimatorListenerAdapter() {
//                                    override fun onAnimationEnd(animation: Animator) {
//                                        view.image.clearColorFilter()
//                                        view.image.setHasTransientState(false)
//                                    }
//                                })
//                                saturation.start()
//                                status.photos!!.hasFadedIn = true
//                            }


                            return false
                        }
                    })
                    .into(view.image)
            view.image.setOnClickListener {
                val rect = Rect()
                view.image.getGlobalVisibleRect(rect)
                val thumbnailInfo = ThumbnailInfo(rect, view.image.scaleType)
                gotoPhotoView(status, thumbnailInfo)
            }
        }

        FanfouUtils.parseAndSetText(view.content, status.text)

        view.surfaceView.setOnClickListener {
            if (url == null) {
                (view.context as AbstractActivity).start(StatusView.newInstance(status))
            } else {
                Glide.with(view)
                        .asBitmap()
                        .load(url)
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                (view.context as AbstractActivity).start(StatusView.newInstance(status))
                                return false
                            }

                            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                resource?.apply {
                                    Bundle().apply {
                                        putInt("w", width)
                                        putInt("h", height)
                                    }.also { (view.context as AbstractActivity).start(StatusView.newInstance(status, photoExtra = it)) }
                                }
                                return true
                            }
                        }).preload()
            }
        }

    }


    /**
     * 这里要再加载一次原图，因为列表中的图是裁剪过的
     *
     */
    private fun gotoPhotoView(status: Status, thumbnailInfo: ThumbnailInfo) {
        GlideApp.with(view).asBitmap().load(status.photos?.size(thumbnailInfo.rect.width()))
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


    fun clear() {
        view.swipeLayout.isRightSwipeEnabled = false
        view.avatar.setOnClickListener(null)
        view.screenName.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.createdAt.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.content.setBackgroundColor(ContextCompat.getColor(view.context, R.color.textColorHint))
        view.screenName.text = null
        view.createdAt.text = null
        view.content.text = null
        glide.clear(view.image)
        glide.clear(view.avatar)
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, uniqueId: String?, fragment: Fragment): StatusViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.timeline_view_list_item, parent, false)
            return StatusViewHolder(view, glide, uniqueId, fragment)
        }
    }

}