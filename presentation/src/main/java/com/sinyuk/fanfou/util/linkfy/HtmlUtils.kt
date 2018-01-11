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

package com.sinyuk.fanfou.util.linkfy

import android.content.res.ColorStateList
import android.os.Build
import android.support.annotation.ColorInt
import android.text.*
import android.text.style.URLSpan
import android.widget.TextView


/**
 * Utility methods for working with HTML.
 */
object HtmlUtils {

    /**
     * Work around some 'features' of TextView and URLSpans. i.e. vanilla URLSpans do not react to
     * touch so we replace them with our own [TouchableUrlSpan]
     * & [LinkTouchMovementMethod] to fix this.
     *
     *
     * Setting a custom MovementMethod on a TextView also alters touch handling (see
     * TextView#fixFocusableAndClickableSettings) so we need to correct this.
     *
     * @param textView the text view
     * @param input    the input
     */
    fun setTextWithNiceLinks(textView: TextView, input: CharSequence) {
        textView.text = input
        textView.movementMethod = LinkTouchMovementMethod.getInstance()
        textView.isFocusable = false
        textView.isClickable = false
        textView.isLongClickable = false
    }

    /**
     * Parse the given input using [TouchableUrlSpan]s rather than vanilla [URLSpan]s
     * so that they respond to touch.
     *
     * @param input              the input
     * @param linkTextColor      the link text color
     * @param linkHighlightColor the link highlight color
     * @return the spannable string builder
     */
     fun parseHtml(input: String, linkTextColor: ColorStateList, @ColorInt linkHighlightColor: Int): SpannableStringBuilder {

        var spanned = fromHtml(input)

        // strip any trailing newlines
        while (spanned[spanned.length - 1] == '\n') {
            spanned = spanned.delete(spanned.length - 1, spanned.length)
        }

        return linkifyPlainLinks(spanned, linkTextColor, linkHighlightColor)
    }

    /**
     * Parse and set text.
     *
     * @param textView the text view
     * @param input    the input
     */
    fun parseAndSetText(textView: TextView, input: String) {
        if (TextUtils.isEmpty(input)) return
        setTextWithNiceLinks(textView, parseHtml(input, textView.linkTextColors, textView.highlightColor))
    }


    /**
     * 把[URLSpan]转换成[TouchableUrlSpan]
     *
     * @param input              the input
     * @param linkTextColor      the link text color
     * @param linkHighlightColor the link highlight color
     * @return the spannable string builder
     */
    fun linkifyPlainLinks(input: CharSequence, linkTextColor: ColorStateList, @ColorInt linkHighlightColor: Int): SpannableStringBuilder {
        val plainLinks = SpannableString(input) // copy of input

        // Linkify doesn't seem to work as expected on M+
        // TODO: figure out why
        //        Linkify.addLinks(plainLinks, Linkify.WEB_URLS);

        val urlSpans = plainLinks.getSpans(0, plainLinks.length, URLSpan::class.java)

        // add any plain links to the output
        val ssb = SpannableStringBuilder(input)
        for (urlSpan in urlSpans) {
            ssb.removeSpan(urlSpan)
            ssb.setSpan(TouchableUrlSpan(
                    urlSpan.url,
                    linkTextColor, linkHighlightColor),
                    plainLinks.getSpanStart(urlSpan),
                    plainLinks.getSpanEnd(urlSpan),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return ssb
    }

    /**
     * From html spannable string builder.
     *
     * @param input the input
     * @return the spannable string builder
     */
    fun fromHtml(input: String): SpannableStringBuilder {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY) as SpannableStringBuilder
        } else {
            Html.fromHtml(input) as SpannableStringBuilder
        }

    }

}
