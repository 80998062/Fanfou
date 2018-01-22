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

package com.sinyuk.fanfou.ui.status

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractSwipeFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.STATUS_LIMIT
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import com.sinyuk.fanfou.util.FanfouFormatter
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import kotlinx.android.synthetic.main.status_view.*
import kotlinx.android.synthetic.main.status_view_footer.*
import kotlinx.android.synthetic.main.status_view_header.*
import kotlinx.android.synthetic.main.status_view_reply_actionbar.*


/**
 * Created by sinyuk on 2018/1/12.
 *
 */
class StatusView : AbstractSwipeFragment(), Injectable {
    companion object {
        fun newInstance(status: Status, photoExtra: Bundle? = null) = StatusView().apply {
            arguments = Bundle().apply {
                putParcelable("status", status)
                putBundle("photoExtra", photoExtra)
            }
        }
    }

    override fun layoutId() = R.layout.status_view

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST)
        navBack.setOnClickListener { pop() }
        val status = arguments!!.getParcelable<Status>("status")
        renderUI(status)
        setupKeyboard()
    }

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private fun setupKeyboard() {

        keyboardListener = KeyboardUtil.attach(activity, panelRoot, {
            if (BuildConfig.DEBUG) Log.i("StatusView", "keyboardShowing: " + it)
            panelRootContainer.visibility =
                    if (it) {
                        if (replyEt.requestFocus()) replyEt.setSelection(replyEt.text.length)
                        View.VISIBLE
                    } else {
                        replyEt.clearFocus()
                        View.GONE
                    }
        })


        textCountProgress.max = STATUS_LIMIT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) textCountProgress.min = 0

        replyEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(STATUS_LIMIT))

        replyCommitButton.isEnabled = false

        replyCommitButton.setOnClickListener { }

        replyEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textCountProgress.progress = count
                replyCommitButton.isEnabled = count > 0 || count < STATUS_LIMIT
            }
        })

        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) KPSwitchConflictUtil.hidePanelAndKeyboard(panelRootContainer)
            return@setOnTouchListener false
        }
    }

    private val uidFormat = "@%s"
    private val sourceFormat = "来自%s"

    private fun renderUI(status: Status) {
        GlideApp.with(this@StatusView).asDrawable().load(status.playerExtracts?.profileImageUrlLarge).avatar().transition(withCrossFade()).into(avatar)
        screenName.text = status.playerExtracts?.screenName
        FanfouUtils.parseAndSetText(content, status.text)
        FanfouUtils.parseAndSetText(source, String.format(sourceFormat, status.source))
        val formatedId = String.format(uidFormat, status.playerExtracts?.id)
        userId.text = formatedId
        createdAt.text = FanfouFormatter.convertDateToStr(status.createdAt!!)

        if (status.photos == null) {
            image.visibility = View.GONE
        } else {
            image.visibility = View.VISIBLE
            arguments?.getBundle("photoExtra")?.let {
                image.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        image.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val ratio = it.getInt("h", 0) * 1.0f / it.getInt("w", 1)
                        val lps = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        lps.height = (image.width * ratio).toInt()
                        image.layoutParams = lps
                    }
                })
            }

            GlideApp.with(this@StatusView).asBitmap().load(status.photos?.bestUrl()).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(image)
        }


        //

        fullscreenButton.setOnClickListener {}

        moreButton.setOnClickListener {}

    }

    override fun onDestroy() {
        keyboardListener?.let { KeyboardUtil.detach(activity, it) }
        activity?.currentFocus?.let { KeyboardUtil.hideKeyboard(it) }
        super.onDestroy()
    }

}