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
import android.support.annotation.ColorInt
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import okhttp3.HttpUrl


/**
 * Created by sinyuk on 2017/3/20.
 *
 */
object FanfouUtils {

    /**
     * The constant SOURCE_FORMAT.
     */
    private val SOURCE_FORMAT = "<a href=\"%s\" target=\"_blank\">%s</a>"

    /**
     * 1. 把@某个人的[TouchableUrlSpan]转换成[PlayerSpan]
     * 2. 把#话题#的[TouchableUrlSpan]转换成[TopicSpan]
     *
     * @param input              the input
     * @param linkTextColor      the link text color
     * @param linkHighlightColor the link highlight color
     * @return the spanned
     */
    private fun parseFanfouHtml(input: String, linkTextColor: ColorStateList, @ColorInt linkHighlightColor: Int): SpannableStringBuilder {
        val ssb = HtmlUtils.parseHtml(input, linkTextColor, linkHighlightColor)


        val urlSpans = ssb.getSpans(0, ssb.length, TouchableUrlSpan::class.java)

        for (urlSpan in urlSpans) {
            val start = ssb.getSpanStart(urlSpan)
            if (start < 1) continue
            // Sample: @<a href=\"http://fanfou.com/~Iz0JWzKTb-Y\" class=\"former\">努尔哈欠</a>

            if (ssb.subSequence(start - 1, start).toString() == "@") {
                val end = ssb.getSpanEnd(urlSpan)
                ssb.removeSpan(urlSpan)
                val url = HttpUrl.parse(urlSpan.url)

                val uniqueId = url!!.pathSegments()[0]
//                val nickname = ssb.subSequence(start + 1, end).toString()
                ssb.setSpan(PlayerSpan(urlSpan.url, uniqueId, linkTextColor, linkHighlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            } else if (ssb.subSequence(start - 1, start).toString() == "#") {
                // #饭否每日精选#
                val end = ssb.getSpanEnd(urlSpan)
                ssb.removeSpan(urlSpan)
                val title = ssb.subSequence(start + 1, end).toString()
                ssb.setSpan(TopicSpan(urlSpan.url, title, linkTextColor, linkHighlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return ssb
    }

    /**
     * Parse and set text.
     *
     * @param textView the text view
     * @param input    the input
     */
    fun parseAndSetText(textView: TextView, input: String?) {
        if (TextUtils.isEmpty(input)) textView.text = null
        else HtmlUtils.setTextWithNiceLinks(textView, parseFanfouHtml(input!!, textView.linkTextColors, textView.highlightColor))
    }

    /**
     * Build source html string.
     *
     * @param link   the link
     * @param source the source
     * @return the string
     */
    fun buildSourceHtml(link: String, source: String) = if (TextUtils.isEmpty(link) || TextUtils.isEmpty(source)) {
        "<a href=\"http://sinyuk.me\" target=\"_blank\">饭壳</a>"
    } else String.format(SOURCE_FORMAT, link, source)


}
