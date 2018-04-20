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
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import cn.dreamtobe.kpswitch.util.KeyboardUtil
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
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.STATUS_LIMIT
import com.sinyuk.fanfou.domain.StatusCreation
import com.sinyuk.fanfou.domain.TIMELINE_CONTEXT
import com.sinyuk.fanfou.ui.editor.EditorView
import com.sinyuk.fanfou.ui.editor.MentionListView
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import kotlinx.android.synthetic.main.status_view.*
import kotlinx.android.synthetic.main.status_view_footer.*
import kotlinx.android.synthetic.main.status_view_reply_actionbar.*
import javax.inject.Inject


/**
 * Created by sinyuk on 2018/1/12.
 *
 */
class StatusView : AbstractFragment(), Injectable, QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

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
        navBack.setOnClickListener { onBackPressedSupport() }
        setupEditor()
        setupKeyboard()
        onTextChanged(0)
        setupViewPager()

        val status = arguments!!.getParcelable<Status>("status")
        fullscreenButton.setOnClickListener {
            (activity as AbstractActivity).start(EditorView.newInstance(status.id,
                    replyEt.mentionsText,
                    StatusCreation.REPOST_STATUS))
            replyEt.text = null
        }
    }

    private fun setupViewPager() {
        val status = arguments!!.getParcelable<Status>("status")
        val bundle = arguments!!.getBundle("photoExtra")
        val fragments: List<Fragment> = if (findChildFragment(TimelineView::class.java) == null) {
            val mentionView = MentionListView()
            mentionView.onItemClickListener = onSuggestionSelectListener
            mutableListOf(TimelineView.contextTimeline(TIMELINE_CONTEXT, status, bundle), mentionView)
        } else {
            mutableListOf(findChildFragment(TimelineView::class.java), MentionListView())
        }

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size
        }
    }

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private fun setupKeyboard() {
        keyboardListener = KeyboardUtil.attach(activity, panelRoot, {
            // TODO: how comes the Exception: panelRootContainer must not be null
            panelRootContainer?.visibility =
                    if (it) {
                        if (replyEt.requestFocus()) replyEt.setSelection(replyEt.text.length)
                        View.VISIBLE
                    } else {
                        replyEt.clearFocus()
                        View.GONE
                    }
        })
    }

    private val config = WordTokenizerConfig.Builder()
            .setExplicitChars("@")
            .setThreshold(3)
            .setMaxNumKeywords(5)
            .setWordBreakChars(" ").build()

    private fun setupEditor() {
        replyEt.tokenizer = WordTokenizer(config)
        replyEt.setAvoidPrefixOnTap(true)
        replyEt.setQueryTokenReceiver(this)
        replyEt.setSuggestionsVisibilityManager(this)
        replyEt.setAvoidPrefixOnTap(true)

        replyCommitButton.setOnClickListener { }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            textCountProgress.min = 0
        textCountProgress.max = STATUS_LIMIT
        replyEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s?.length ?: 0)
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
    private fun onTextChanged(count: Int) {
        textCountProgress.progress = count
        replyCommitButton.isEnabled = count in 1..STATUS_LIMIT
    }


    private val onSuggestionSelectListener = object : MentionListView.OnItemClickListener {
        override fun onItemClick(position: Int, item: Suggestible) {
            (item as Player).let {
                replyEt.insertMention(it)
                displaySuggestions(false)
                playerViewModel.updateMentionedAt(it) //
                onTextChanged(replyEt.text.length)
                replyEt.requestFocus()
                replyEt.setSelection(replyEt.text.length)
            }
        }
    }

    @Suppress("PrivatePropertyName")
    private val BUCKET = "player-mentioned"

    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
        val data = playerViewModel.filter(queryToken.keywords)
        onReceiveSuggestionsResult(SuggestionsResult(queryToken, data), BUCKET)
        return arrayOf(BUCKET).toMutableList()
    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
        val data = result.suggestions
        if (data?.isEmpty() != false) return
        displaySuggestions(true)
        findChildFragment(MentionListView::class.java).setData(data)
    }

    override fun displaySuggestions(display: Boolean) {
        viewPager.setCurrentItem(if (display) 1 else 0, true)
    }

    override fun isDisplayingSuggestions() = viewPager.currentItem == 1

    override fun onBackPressedSupport(): Boolean {
        when {
            panelRootContainer.visibility == View.VISIBLE -> KeyboardUtil.hideKeyboard(panelRootContainer)
            isDisplayingSuggestions -> displaySuggestions(false)
            else -> pop()
        }
        return true

    }

    override fun onDestroy() {
        keyboardListener?.let { KeyboardUtil.detach(activity, it) }
        activity?.currentFocus?.let { KeyboardUtil.hideKeyboard(it) }
        super.onDestroy()
    }

}