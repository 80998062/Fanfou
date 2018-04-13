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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractActivity
import com.sinyuk.fanfou.base.AbstractSwipeFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Photos
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.STATUS_LIMIT
import com.sinyuk.fanfou.domain.StatusCreation
import com.sinyuk.fanfou.domain.TIMELINE_CONTEXT
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import com.sinyuk.fanfou.ui.QMUIRoundButtonDrawable
import com.sinyuk.fanfou.ui.editor.EditorView
import com.sinyuk.fanfou.ui.editor.MentionListView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.FanfouFormatter
import com.sinyuk.fanfou.util.linkfy.FanfouUtils
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.myutils.ConvertUtils
import kotlinx.android.synthetic.main.status_view.*
import kotlinx.android.synthetic.main.status_view_footer.*
import kotlinx.android.synthetic.main.status_view_header.*
import kotlinx.android.synthetic.main.status_view_reply_actionbar.*
import javax.inject.Inject


/**
 * Created by sinyuk on 2018/1/12.
 *
 */
class StatusView : AbstractSwipeFragment(), Injectable, QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {


    companion object {
        fun newInstance(status: Status, photoExtra: Bundle? = null) = StatusView().apply {
            arguments = Bundle().apply {
                putParcelable("status", status)
                putBundle("photoExtra", photoExtra)
            }
        }
    }

    override fun layoutId() = R.layout.status_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val playerViewModel by lazy { obtainViewModelFromActivity(factory, PlayerViewModel::class.java) }


    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST)

        val status = arguments!!.getParcelable<Status>("status")
        navBack.setOnClickListener { pop() }
        setupEditor(status)
        setupKeyboard()
        onFormValidation(0)
        renderUI(status)

        if (findChildFragment(TimelineView::class.java) == null) {
            loadRootFragment(R.id.contextTimelineContainer, TimelineView.contextTimeline(TIMELINE_CONTEXT, status.id))
        } else {
            showHideFragment(findChildFragment(TimelineView::class.java))
        }
    }

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private fun setupKeyboard() {
        keyboardListener = KeyboardUtil.attach(activity, panelRoot, {
            panelRootContainer.visibility =
                    if (it) {
                        if (replyEt.requestFocus()) replyEt.setSelection(replyEt.text.length)
                        View.VISIBLE
                    } else {
                        replyEt.clearFocus()
                        View.GONE
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
        val formattedId = String.format(uidFormat, status.playerExtracts?.id)
        userId.text = formattedId
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

            GlideApp.with(this@StatusView).asDrawable().load(status.photos?.size(ConvertUtils.dp2px(context, Photos.LARGE_SIZE)))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(image)
        }


        moreButton.setOnClickListener {}

    }


    private val config = WordTokenizerConfig.Builder().setExplicitChars("@").setThreshold(3).setMaxNumKeywords(5).setWordBreakChars(" ").build()

    private fun setupEditor(status: Status) {
        replyEt.tokenizer = WordTokenizer(config)
        replyEt.setAvoidPrefixOnTap(true)
        replyEt.setQueryTokenReceiver(this)
        replyEt.setSuggestionsVisibilityManager(this)
        replyEt.setAvoidPrefixOnTap(true)

        replyCommitButton.setOnClickListener { }

        fullscreenButton.setOnClickListener {
            (activity as AbstractActivity).start(EditorView.newInstance(status.id, replyEt.mentionsText, StatusCreation.REPOST_STATUS))
            replyEt.text = null
        }

        textCountProgress.max = STATUS_LIMIT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) textCountProgress.min = 0

        replyEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextCountUpdated(s?.length ?: 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }


    /**
     * @param count 字数
     */
    private fun onTextCountUpdated(count: Int) {
        textCountProgress.progress = count
        onFormValidation(count)
    }


    private fun onFormValidation(count: Int) {
        if (count in 1..STATUS_LIMIT) {
            if (replyCommitButton.isEnabled) return
            (replyCommitButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlActivated))
            replyCommitButton.isEnabled = true
        } else {
            if (!replyCommitButton.isEnabled) return
            (replyCommitButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlDisable))
            replyCommitButton.isEnabled = false
        }
    }


    @Suppress("PrivatePropertyName")
    private val BUCKET = "player-mentioned"

    private var playerLiveData: LiveData<MutableList<Player>>? = null
    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
        playerLiveData = playerViewModel.filter(queryToken.keywords).apply {
            observe(this@StatusView, Observer<MutableList<Player>> { t ->
                playerLiveData?.removeObservers(this@StatusView)
                onReceiveSuggestionsResult(SuggestionsResult(queryToken, t), BUCKET)
            })
        }
        return arrayOf(BUCKET).toMutableList()
    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
        val data = result.suggestions
        displaySuggestions(data?.isNotEmpty() == true)
        if (data?.isEmpty() != false) return
        if (findChildFragment(MentionListView::class.java) == null) {
            val fragment = MentionListView.newInstance(data.toTypedArray())
            fragment.onItemClickListener = object : MentionListView.OnItemClickListener {
                override fun onItemClick(position: Int, item: Suggestible) {
                    (item as Player).let {
                        replyEt.insertMention(it)
                        displaySuggestions(false)
                        playerViewModel.updateMentionedAt(it) //
                        onTextCountUpdated(replyEt.text.length)
                        replyEt.requestFocus()
                        replyEt.setSelection(replyEt.text.length)
                    }
                }
            }
            loadRootFragment(R.id.mentionLayout, fragment)
        } else {
            findChildFragment(MentionListView::class.java)?.apply {
                showHideFragment(this)
                updateListView(data = data)
            }
        }
    }

    override fun displaySuggestions(display: Boolean) {
        viewAnimator.displayedChildId = if (display) {
            R.id.mentionLayout
        } else {
            R.id.coordinator
        }
    }

    override fun isDisplayingSuggestions() = viewAnimator.displayedChildId == R.id.mentionLayout

    override fun onDestroy() {
        keyboardListener?.let { KeyboardUtil.detach(activity, it) }
        activity?.currentFocus?.let { KeyboardUtil.hideKeyboard(it) }
        super.onDestroy()
    }

}