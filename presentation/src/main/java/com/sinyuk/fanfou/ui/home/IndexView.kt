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

package com.sinyuk.fanfou.ui.home

import android.arch.lifecycle.Observer
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.ViewTarget
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.util.span.AndroidSpan
import com.sinyuk.fanfou.util.span.SpanOptions
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.index_view.*
import kotlinx.android.synthetic.main.state_layout_forbidden.view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/30.
 *
 */
@Deprecated("废物")
class IndexView : AbstractFragment(), Injectable {


    override fun layoutId() = R.layout.index_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    @Inject
    lateinit var toastUtils: ToastUtils

    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        accountViewModel.profile.observe(this@IndexView, Observer { render(it) })
    }

    private fun render(data: Player?) {
        viewAnimator.displayedChildId = when {
            data == null -> {
                setup401View()
                R.id.layout401
            }
            data.friendsCount == 0 -> {
                setupNoFriendsView(data)
                R.id.layoutEmpty
            }
            else -> {
                setupTimelineView(data)
                R.id.pullRefreshLayout
            }
        }
    }

    private fun setup401View() {
        if (layout401.title.text.isNullOrEmpty()) {
            layout401.title.text = getString(R.string.state_title_401)
            val span = AndroidSpan().drawRelativeSizeSpan(getString(R.string.state_description_401), 1f)
                    .drawWithOptions("Sign in", SpanOptions().addTextAppearanceSpan(context!!, R.style.text_bold_primary).addSpan(object : ClickableSpan() {
                        override fun onClick(v: View?) {
                            toastUtils.toastLong("haha")
                        }
                    })).spanText
            layout401.description.movementMethod = LinkMovementMethod.getInstance()
            layout401.description.text = span
            val target = GlideApp.with(this).load(R.drawable.state_layout_forbidden).transition(withCrossFade()).into(layout401.image)
            setStateDrawableTarget(target)
        }
    }

    private var stateDrawableTarget: ViewTarget<ImageView, Drawable>? = null

    private fun setupNoFriendsView(data: Player) {
        if (layoutEmpty.title.text.isNullOrEmpty()) {
            layoutEmpty.title.text = getString(R.string.state_title_nofriends)
            val span = AndroidSpan().drawRelativeSizeSpan(getString(R.string.state_description_nofriends), 1f)
                    .drawWithOptions("Look around", SpanOptions().addTextAppearanceSpan(context!!, R.style.text_bold_primary).addSpan(object : ClickableSpan() {
                        override fun onClick(v: View?) {
                            toastUtils.toastLong("haha")
                        }
                    })).spanText
            layoutEmpty.description.movementMethod = LinkMovementMethod.getInstance()
            layoutEmpty.description.text = span
            val target = GlideApp.with(this).load(R.drawable.state_layout_empty).transition(withCrossFade()).into(layoutEmpty.image)
            setStateDrawableTarget(target)
        }
    }

    private fun setupTimelineView(data: Player) {
        setStateDrawableTarget(null)
        if (findChildFragment(TimelineView::class.java) == null) {
            val fragment = TimelineView.playerTimeline(TIMELINE_HOME, data)
            loadRootFragment(R.id.homeTimelineViewContainer, fragment)
        } else {
            showHideFragment(findChildFragment(TimelineView::class.java))
        }

        pullRefreshLayout.setOnRefreshListener { (findChildFragment(TimelineView::class.java) as TimelineView).refresh() }
    }

    /**
     * 清除正在加载的图片目标
     *
     * @param target 加载图片目标
     */
    private fun setStateDrawableTarget(target: ViewTarget<ImageView, Drawable>?) {
        if (stateDrawableTarget != null) stateDrawableTarget!!.request?.clear()
        stateDrawableTarget = target
    }
}