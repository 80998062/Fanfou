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

package com.sinyuk.fanfou.util.span

import android.annotation.TargetApi
import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.Build
import android.text.Layout
import android.text.style.*
import java.util.*

/**
 * Created by james on 13/10/15.
 */
class SpanOptions {
    val listSpan: MutableList<Any> = ArrayList()

    /**
     * 下划线
     *
     * @return
     */
    fun addUnderlineSpan(): SpanOptions {
        val span = UnderlineSpan()
        listSpan.add(span)
        return this
    }

    fun addBulletSpan(gapWidth: Int, color: Int): SpanOptions {
        val span = BulletSpan(gapWidth, color)
        listSpan.add(span)
        return this
    }

    /**
     * URL效果
     * 需要实现textView.setMovementMethod(LinkMovementMethod.getInstance());
     *
     * @param url 格式为：电话：tel:18721850636，邮箱：mailto:1119117546@qq.com，网站：http://www.baidu.com,短信：mms:4155551212，彩信：mmsto:18721850636,地图：geo:38.899533,-77.036476
     * @return
     */
    fun addURLSpan(url: String): SpanOptions {
        val Urlspan = URLSpan(url)
        listSpan.add(Urlspan)
        return this
    }

    fun addQuoteSpan(color: Int): SpanOptions {
        val span = QuoteSpan(color)
        listSpan.add(span)
        return this
    }

    fun addAlignmentSpan(align: Layout.Alignment): SpanOptions {
        val span = AlignmentSpan.Standard(align)
        listSpan.add(span)
        return this
    }

    fun addStrikethroughSpan(): SpanOptions {
        val span = StrikethroughSpan()
        listSpan.add(span)
        return this
    }

    fun addBackgroundColorSpan(color: Int): SpanOptions {
        val span = BackgroundColorSpan(color)
        listSpan.add(span)
        return this
    }

    /**
     * @param density
     * @param style   BlurMaskFilter.Blur.NORMAL
     * @return
     */
    fun addMaskFilterSpan(density: Float, style: BlurMaskFilter.Blur): SpanOptions {
        val span = MaskFilterSpan(BlurMaskFilter(density, style))
        listSpan.add(span)
        return this
    }

    fun addSubscriptSpan(): SpanOptions {
        val span = SubscriptSpan()
        listSpan.add(span)
        return this
    }

    fun addSuperscriptSpan(): SpanOptions {
        val span = SuperscriptSpan()
        listSpan.add(span)
        return this
    }

    /**
     * @param style Typeface.BOLD | Typeface.ITALIC
     * @return
     */
    fun addStyleSpan(style: Int): SpanOptions {
        val span = StyleSpan(style)
        listSpan.add(span)
        return this
    }

    fun addAbsoluteSizeSpan(size: Int, dip: Boolean): SpanOptions {
        val span = AbsoluteSizeSpan(size, dip)
        listSpan.add(span)
        return this
    }

    /**
     * 同比放大索小
     *
     * @return
     */
    fun addRelativeSizeSpan(proportion: Float): SpanOptions {
        val span = RelativeSizeSpan(proportion)
        listSpan.add(span)
        return this
    }

    fun addTextAppearanceSpan(context: Context, appearance: Int): SpanOptions {
        val span = TextAppearanceSpan(context, appearance)
        listSpan.add(span)
        return this
    }

    /**
     * @param locale Locale.CHINESE
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun addLocaleSpan(locale: Locale): SpanOptions {
        val span = LocaleSpan(locale)
        listSpan.add(span)
        return this
    }

    fun addScaleXSpan(proportion: Float): SpanOptions {
        val span = ScaleXSpan(proportion)
        listSpan.add(span)
        return this
    }

    /**
     * @param typeface serif
     * @return
     */
    fun addTypefaceSpan(typeface: String): SpanOptions {
        val span = TypefaceSpan(typeface)
        listSpan.add(span)
        return this
    }

    fun addImageSpan(context: Context, imgId: Int): SpanOptions {
        val span = ImageSpan(context, imgId)
        listSpan.add(span)
        return this
    }


    /**
     * 文本颜色
     *
     * @return
     */
    fun addForegroundColor(color: Int): SpanOptions {
        val span = ForegroundColorSpan(color)
        listSpan.add(span)
        return this
    }


    /**
     * 自定义Span
     *
     * @param object
     * @return
     */

    fun addSpan(span: Any): SpanOptions {
        listSpan.add(span)
        return this
    }
}
