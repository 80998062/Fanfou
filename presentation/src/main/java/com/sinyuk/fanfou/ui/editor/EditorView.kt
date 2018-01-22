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

package com.sinyuk.fanfou.ui.editor

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.STATUS_LIMIT
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.QMUIRoundButtonDrawable
import com.sinyuk.fanfou.util.PictureHelper
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import kotlinx.android.synthetic.main.editor_picture_list_item.view.*
import kotlinx.android.synthetic.main.editor_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/16.
 *
 */
class EditorView : AbstractFragment(), Injectable, QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

    companion object {
        fun reply2Player(uniqueId: String, content: String?) = EditorView().apply {
            arguments = Bundle().apply {
                putString("uniqueId", uniqueId)
                putString("content", content)

            }
        }


        fun repostOrReply(status: String, content: String?) = EditorView().apply {
            arguments = Bundle().apply {
                putString("status", status)
                putString("content", content)
            }
        }

        const val OPEN_PICTURE_REQUEST_CODE = 0X123
    }

    override fun layoutId() = R.layout.editor_view

    private val status by lazy { arguments!!.getParcelable<Status>("status") }

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val playerViewModel by lazy { obtainViewModel(factory, PlayerViewModel::class.java) }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        closeButton.setOnClickListener { pop() }
        setupKeyboard()
        setupEditor()
        setupMentionList()

        //
        renderUI()
    }

    private val config = WordTokenizerConfig.Builder().setExplicitChars("@").setThreshold(1).setWordBreakChars(" ").build()

    private fun setupEditor() {
        contentEt.tokenizer = WordTokenizer(config)
        contentEt.setAvoidPrefixOnTap(true)
        contentEt.setQueryTokenReceiver(this)
        contentEt.setSuggestionsVisibilityManager(this)
        contentEt.setAvoidPrefixOnTap(true)

        textCountProgress.max = STATUS_LIMIT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) textCountProgress.min = 0
//        contentEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(STATUS_LIMIT))
        contentEt.addTextChangedListener(object : TextWatcher {
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
        onFormValidation()
        textCountProgress.progress = count
        textCount.text = count.toString()
    }

    private fun renderUI() {
        onFormValidation()
        actionButton.setOnClickListener {

        }

        addPictureButton.setOnClickListener {
            startActivityForResult(PictureHelper.fileSearchIntent(), OPEN_PICTURE_REQUEST_CODE)
        }

        pictureItem.deleteButton.setOnClickListener {
            viewAnimator.displayedChildId = R.id.emptyLayout
            uri = null
            GlideApp.with(this).clear(pictureItem.image)
            onFormValidation()
        }

        pictureItem.editButton.setOnClickListener {
            val editIntent = Intent(Intent.ACTION_EDIT)
            editIntent.setDataAndType(uri, "image/*")
            editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(editIntent, null))
        }


    }

    private lateinit var adapter: MentionAdapter

    private fun setupMentionList() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 10
            isAutoMeasureEnabled = true
            mentionList.layoutManager = this
        }

        mentionList.isNestedScrollingEnabled = false
        mentionList.setHasFixedSize(true)
        mentionList.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        adapter = MentionAdapter(this@EditorView)
        mentionList.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            val p = adapter.getItem(position) as Player
            contentEt.insertMention(p)
            onTextCountUpdated(contentEt.text.length)
            adapter.setNewData(null)
            playerViewModel.updateMentionedAt(p)
        }
    }

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private fun setupKeyboard() {
        nestedScrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
            return@setOnTouchListener false
        }

        keyboardListener = KeyboardUtil.attach(activity, panelRoot, {
            panelRoot.visibility =
                    if (it) {
                        if (contentEt.requestFocus()) contentEt.setSelection(contentEt.text.length)
                        View.VISIBLE
                    } else {
                        contentEt.clearFocus()
                        View.GONE
                    }
        })
    }

    @Suppress("PrivatePropertyName")
    private val BUCKET = "player-mentioned"

    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {
        playerViewModel.filter(queryToken.keywords).observe(this@EditorView, Observer<MutableList<Player>> { t ->
            playerViewModel.filter(queryToken.keywords).removeObservers(this@EditorView)
            onReceiveSuggestionsResult(SuggestionsResult(queryToken, t), BUCKET)
        })
        return arrayOf(BUCKET).toMutableList()
    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {
        val suggestions = result.suggestions
        adapter.setNewData(suggestions)
        displaySuggestions(suggestions?.isNotEmpty() == true)
    }

    override fun displaySuggestions(display: Boolean) {
        viewAnimator.displayedChildId = if (display) {
            R.id.mentionLayout
        } else {
            if (uri == null) {
                R.id.emptyLayout
            } else {
                R.id.pictureLayout
            }
        }
    }


    private fun onFormValidation() {
        if (isContentValid() || isPictureValid()) {
            if (actionButton.isEnabled) return
            (actionButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlActivated))
            actionButton.isEnabled = true
        } else {
            if (!actionButton.isEnabled) return
            (actionButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlDisable))
            actionButton.isEnabled = false
        }
    }

    private var originContent: String? = null

    private fun isContentValid() = contentEt.text.isNotBlank() && contentEt.text.toString() !== originContent

    private fun isPictureValid() = uri != null

    override fun isDisplayingSuggestions() = viewAnimator.displayedChildId == R.id.mentionLayout

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == OPEN_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            data?.let { showImage(it.data) }
        }
    }

    private var uri: Uri? = null
    private fun showImage(data: Uri?) {
        uri = data
        if (data == null) {
            GlideApp.with(this).clear(pictureItem.image)
        } else {
            viewAnimator.displayedChildId = R.id.pictureLayout
            GlideApp.with(this).asBitmap().load(data).into(pictureItem.image)
            onFormValidation()
        }
    }


    override fun onDestroy() {
        keyboardListener?.let { KeyboardUtil.detach(activity, it) }
        activity?.currentFocus?.let { KeyboardUtil.hideKeyboard(it) }
        super.onDestroy()
    }
}