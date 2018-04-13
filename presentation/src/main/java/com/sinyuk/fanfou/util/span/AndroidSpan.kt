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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import java.util.*

/**
 * Created by james on 13/10/15.
 */
class AndroidSpan {

    val spanText: SpannableStringBuilder = SpannableStringBuilder("")

    private fun getWordPosition(text: String): WordPosition {
        val start = spanText.toString().length
        val end = start + text.length
        return WordPosition(start, end)
    }

    /**
     * 下划线
     *
     * @param text
     * @return
     */
    fun drawUnderlineSpan(text: String): AndroidSpan {
        val span = UnderlineSpan()
        drawSpan(text, span)
        return this
    }

    /**
     * 没有效果
     *
     * @param text
     * @return
     */
    fun drawCommonSpan(text: String): AndroidSpan {
        drawSpan(text, null)
        return this
    }

    /**
     * 段落的开始处加上项目符号
     *
     * @param gapWidth 项目符号和文本之间的间隙
     * @param color    项目符号的颜色，默认为透明
     * @return
     */
    fun drawBulletSpan(text: String, gapWidth: Int, color: Int): AndroidSpan {
        val span = BulletSpan(gapWidth, color)
        drawSpan(text, span)
        return this
    }

    /**
     * 左侧添加一条表示引用的竖线
     *
     * @param text
     * @param color
     * @return
     */
    fun drawQuoteSpan(text: String, color: Int): AndroidSpan {
        val span = QuoteSpan(color)
        drawSpan(text, span)
        return this
    }

    /**
     * 同比放大索小
     *
     * @param text
     * @param size
     * @return
     */
    fun drawRelativeSize(text: String, size: Float): AndroidSpan {
        val span = RelativeSizeSpan(size)
        drawSpan(text, span)
        return this
    }


    /**
     * URL效果
     * 需要实现textView.setMovementMethod(LinkMovementMethod.getInstance());
     *
     * @param url 格式为：电话：tel:18721850636，邮箱：mailto:1119117546@qq.com，网站：http://www.baidu.com,短信：mms:4155551212，彩信：mmsto:18721850636,地图：geo:38.899533,-77.036476
     * @return
     */

    fun drawURLSpan(url: String): AndroidSpan {
        val Urlspan = URLSpan(url)
        drawSpan(url, Urlspan)
        return this
    }

    /**
     * 标准文本对齐样式
     *
     * @param text
     * @param align "ALIGN_CENTER"、"ALIGN_NORMAL" 或"ALIGN_OPPOSITE"中的之一
     * @return
     */
    fun drawAlignmentSpan(text: String, align: Layout.Alignment): AndroidSpan {
        val span = AlignmentSpan.Standard(align)
        drawSpan(text, span)
        return this
    }

    /**
     * 删除线样式
     *
     * @param text
     * @return
     */
    fun drawStrikethroughSpan(text: String): AndroidSpan {
        val span = StrikethroughSpan()
        drawSpan(text, span)
        return this
    }

    /**
     * 背景样式
     *
     * @param text
     * @param color
     * @return
     */
    fun drawBackgroundColorSpan(text: String, color: Int): AndroidSpan {
        val span = BackgroundColorSpan(color)
        drawSpan(text, span)
        return this
    }

    /**
     * 模糊效果
     *
     * @param text
     * @param density
     * @param style   BlurMaskFilter.Blur.NORMAL
     * @return
     */
    fun drawMaskFilterSpan(text: String, density: Float, style: BlurMaskFilter.Blur): AndroidSpan {
        val span = MaskFilterSpan(BlurMaskFilter(density, style))
        drawSpan(text, span)
        return this
    }

    /**
     * 脚注样式，比如化学式的常见写法，当然，还可以对脚注的文字做一定的缩放
     *
     * @param text
     * @return
     */
    fun drawSubscriptSpan(text: String): AndroidSpan {
        val span = SubscriptSpan()
        drawSpan(text, span)
        return this
    }

    /**
     * 上标样式，比如数学上的次方运算
     * @param text
     * @return
     */
    fun drawSuperscriptSpan(text: String): AndroidSpan {
        val span = SuperscriptSpan()
        drawSpan(text, span)
        return this
    }

    /**
     * 由正常、粗体、斜体和同时加粗倾斜四种样式
     * @param text
     * @param style Typeface.BOLD | Typeface.ITALIC |Typeface.BOLD_ITALIC
     * @return
     */
    fun drawStyleSpan(text: String, style: Int): AndroidSpan {
        val span = StyleSpan(style)
        drawSpan(text, span)
        return this
    }

    /**
     * 指定绝对尺寸来改变文本的字体大小
     * @param text
     * @param size
     * @param dip
     * @return
     */
    fun drawAbsoluteSizeSpan(text: String, size: Int, dip: Boolean): AndroidSpan {
        val span = AbsoluteSizeSpan(size, dip)
        drawSpan(text, span)
        return this
    }

    /**
     * 同比放大索小
     *
     * @param text
     * @return
     */
    fun drawRelativeSizeSpan(text: String, proportion: Float): AndroidSpan {
        val span = RelativeSizeSpan(proportion)
        drawSpan(text, span)
        return this
    }

    /**
     * style文件来定义文本样式
     * @param text
     * @param context
     * @param appearance
     * @return
     */
    fun drawTextAppearanceSpan(text: String, context: Context, appearance: Int): AndroidSpan {
        val span = TextAppearanceSpan(context, appearance)
        drawSpan(text, span)
        return this
    }

    /**
     * @param text
     * @param locale Locale.CHINESE
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun drawLocaleSpan(text: String, locale: Locale): AndroidSpan {
        val span = LocaleSpan(locale)
        drawSpan(text, span)
        return this
    }

    /**
     * 缩放X大小
     * @param text
     * @param proportion
     * @return
     */
    fun drawScaleXSpan(text: String, proportion: Float): AndroidSpan {
        val span = ScaleXSpan(proportion)
        drawSpan(text, span)
        return this
    }

    /**
     * 字体样式
     * @param text
     * @param typeface serif
     * @return
     */
    fun drawTypefaceSpan(text: String, typeface: String): AndroidSpan {
        val span = TypefaceSpan(typeface)
        drawSpan(text, span)
        return this
    }

    /**
     * 图片样式
     * @param text
     * @param context
     * @param imgId
     * @return
     */
    fun drawImageSpan(text: String, context: Context, imgId: Int): AndroidSpan {
        val span = ImageSpan(context, imgId)
        drawSpan(text, span)
        return this
    }

    /**
     * 文本颜色
     *
     * @param text
     * @return
     */
    fun drawForegroundColor(text: String, color: Int): AndroidSpan {
        val span = ForegroundColorSpan(color)
        drawSpan(text, span)
        return this
    }

    fun drawSpan(text: String, span: Any?) {
        val wordPosition = getWordPosition(text)
        spanText.append(text)
        if (span == null) {
            return
        }
        spanText.setSpan(span, wordPosition.start, wordPosition.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun drawWithOptions(text: String, spanOptions: SpanOptions): AndroidSpan {
        val wordPosition = getWordPosition(text)
        spanText.append(text)
        for (span in spanOptions.listSpan) {
            spanText.setSpan(span, wordPosition.start, wordPosition.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return this
    }
}
