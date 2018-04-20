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
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.linkedin.android.spyglass.mentions.MentionsEditable
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible
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
import com.sinyuk.fanfou.domain.STATUS_LIMIT
import com.sinyuk.fanfou.domain.StatusCreation
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.QMUIRoundButtonDrawable
import com.sinyuk.fanfou.util.PictureHelper
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.PlayerViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.editor_picture_list_item.view.*
import kotlinx.android.synthetic.main.editor_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2018/1/16.
 *
 */
class EditorView : AbstractFragment(), Injectable, QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

    companion object {
        fun newInstance(id: String? = null, content: MentionsEditable? = null, action: Int, screenName: String? = null) = EditorView().apply {
            arguments = Bundle().apply {
                putString("id", id)
                putParcelable("content", content)
                putInt("action", action)
                putString("screenName", screenName)
            }
        }


        const val OPEN_PICTURE_REQUEST_CODE = 0X123
    }

    override fun layoutId() = R.layout.editor_view

    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val queryMap = mutableMapOf<String, String?>()

    private val playerViewModel by lazy { obtainViewModelFromActivity(factory, PlayerViewModel::class.java) }

    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        closeButton.setOnClickListener { pop() }
        setupKeyboard()
        setupEditor()
        renderUI()

        val action = arguments!!.getInt("action")


        when (action) {
            StatusCreation.CREATE_NEW -> {
                actionButton.text = "发送"
            }
            StatusCreation.REPOST_STATUS -> {
                arguments!!.getParcelable<MentionsEditable>("content")?.let { contentEt.text = it }
                arguments!!.getString("id")?.let { queryMap["repost_status_id"] = it }
                actionButton.text = "转发"
            }
            else -> TODO()
        }
        onFormValidation(0)
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
        onFormValidation(count)
        textCountProgress.progress = count
        textCount.text = count.toString()
    }

    private fun renderUI() {
        actionButton.setOnClickListener {

        }

        addPictureButton.setOnClickListener {
            startActivityForResult(PictureHelper.fileSearchIntent(), OPEN_PICTURE_REQUEST_CODE)
        }

        pictureItem.image.setOnClickListener {
            startActivityForResult(PictureHelper.fileSearchIntent(), OPEN_PICTURE_REQUEST_CODE)
        }

        pictureItem.deleteButton.setOnClickListener {
            viewAnimator.displayedChildId = R.id.emptyLayout
            uri = null
            GlideApp.with(this).clear(pictureItem.image)
            onFormValidation(contentEt.text.length)
        }

        pictureItem.editButton.setOnClickListener {
            val editIntent = Intent(Intent.ACTION_EDIT)
            editIntent.setDataAndType(uri, "image/*")
            editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(editIntent, null))
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
        val data = playerViewModel.filter(queryToken.keywords)
        onReceiveSuggestionsResult(SuggestionsResult(queryToken, data), BUCKET)
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
                        contentEt.insertMention(it)
                        displaySuggestions(false)
                        playerViewModel.updateMentionedAt(it) //
                        onTextCountUpdated(contentEt.text.length)
                        contentEt.requestFocus()
                        contentEt.setSelection(contentEt.text.length)
                    }
                }
            }
            loadRootFragment(R.id.mentionLayout, fragment)
        } else {
            findChildFragment(MentionListView::class.java)?.apply {
                showHideFragment(this)
                setData(data = data)
            }
        }
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


    private fun onFormValidation(count: Int) {
        if (count in 1..STATUS_LIMIT || isPictureValid()) {
            if (actionButton.isEnabled) return
            (actionButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlActivated))
            actionButton.isEnabled = true
        } else {
            if (!actionButton.isEnabled) return
            (actionButton.background as QMUIRoundButtonDrawable).color = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorControlDisable))
            actionButton.isEnabled = false
        }
    }

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

    @Inject
    lateinit var toast: ToastUtils

    private var uri: Uri? = null
    private fun showImage(data: Uri?) {
        if (data == null) {
            uri = null
            onFormValidation(contentEt.text.length)
            GlideApp.with(this).clear(pictureItem.image)
            viewAnimator.displayedChildId = R.id.emptyLayout
        } else {
            GlideApp.with(this).asBitmap().load(data).listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    toast.toastShort(e?.message ?: "( ˉ ⌓ ˉ ๑)图片加载失败")
                    viewAnimator.displayedChildId = if (uri == null) {
                        R.id.emptyLayout
                    } else {
                        R.id.pictureLayout
                    }
                    onFormValidation(contentEt.text.length)
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    uri = data
                    onFormValidation(contentEt.text.length)
                    viewAnimator.displayedChildId = R.id.pictureLayout
                    return false
                }
            }).into(pictureItem.image)
        }
    }


    override fun onDestroy() {
        keyboardListener?.let { KeyboardUtil.detach(activity, it) }
        activity?.currentFocus?.let { KeyboardUtil.hideKeyboard(it) }
        super.onDestroy()
    }
}