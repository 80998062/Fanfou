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

package com.sinyuk.fanfou.ui.colormatchtabs.colortabs

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.ActionBar
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.ui.colormatchtabs.model.ColorTab
import com.sinyuk.fanfou.ui.colormatchtabs.utils.getColor
import com.sinyuk.fanfou.ui.colormatchtabs.utils.getDimen

/**
 * Created by anna on 10.05.17.
 *
 */
class ColorTabView : LinearLayout, View.OnClickListener {

    companion object {
        const val ANIMATION_TEXT_APPEARANCE_DURATION = 100L
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initColorTabView()
    }

    internal var tab: ColorTab? = null
        set(value) {
            field = value
            updateView()
        }

    private lateinit var textView: TextView
    internal lateinit var iconView: ImageView
    internal var clickedTabView: ColorTabView? = null

    private fun initColorTabView() {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        isClickable = true
        setBackgroundColor(Color.TRANSPARENT)
        initViews()
        this.setOnClickListener(this@ColorTabView)
    }

    private fun initViews() {
        iconView = ImageView(context)
        iconView.setBackgroundColor(Color.TRANSPARENT)
        addView(iconView)
        textView = TextView(context)
        addView(textView)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        // This view masquerades as an action bar tab.
        event.className = ActionBar.Tab::class.java.name
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        // This view masquerades as an action bar tab.
        info.className = ActionBar.Tab::class.java.name
    }

    override fun onMeasure(origWidthMeasureSpec: Int, origHeightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec)
        val specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec)
        val maxWidth = (parent.parent as ColorMatchTabLayout).tabMaxWidth

        val widthMeasureSpec: Int
        val heightMeasureSpec = origHeightMeasureSpec - getDimen(R.dimen.tab_padding)
        if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
            // If we have a max width and a given spec which is either unspecified or
            // larger than the max width, update the width spec using the same mode
            val selectTabMaxWidth = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) (parent.parent as ColorMatchTabLayout).selectedTabWidth else (parent.parent as ColorMatchTabLayout).selectedTabHorizontalWidth
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(if (tab?.isSelected == true) selectTabMaxWidth else maxWidth, MeasureSpec.EXACTLY)
        } else {
            // Else, use the original width spec
            widthMeasureSpec = origWidthMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        iconView.setPadding(getDimen(R.dimen.normal_margin), 0, getDimen(R.dimen.normal_margin), getDimen(R.dimen.tab_padding))
        textView.setPadding(0, 0, getDimen(R.dimen.normal_margin), getDimen(R.dimen.tab_padding))
        if (clickedTabView != null) {
            (parent as SlidingTabStripLayout).animateDrawTab(clickedTabView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateView()
    }

    internal fun updateView() {
        val colorTab = tab
        if (tab?.isSelected ?: false) {
            textView.apply {
                visibility = View.VISIBLE
                alpha = 0f
                text = colorTab?.text
                setTextColor(getBackgroundColor())
                animatePlayButton()
            }
        } else {
            textView.visibility = View.GONE
        }
        if (colorTab?.icon != null) {
            iconView.setImageDrawable(colorTab.icon)
            reColorDrawable(colorTab.isSelected)
            iconView.requestLayout()
        }
        requestLayout()
    }

    private fun animatePlayButton() {
        textView.animate()
                .alpha(1f)
                .setDuration(ANIMATION_TEXT_APPEARANCE_DURATION)
                .start()
    }

    override fun onClick(v: View?) {
        if (!(parent as SlidingTabStripLayout).isAnimate) {
            val clickedTabView = v as ColorTabView?
            (parent.parent as ColorMatchTabLayout).select(clickedTabView?.tab)
            this.clickedTabView = clickedTabView
        }
    }

    internal fun reColorDrawable(isSelected: Boolean) {
        if (isSelected) {
            iconView.colorFilter = null
            iconView.setColorFilter(getBackgroundColor(), PorterDuff.Mode.SRC_ATOP)
        } else {
            iconView.setColorFilter(tab?.selectedColor ?: 0, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun getBackgroundColor(): Int {
        var color = getColor(R.color.windowBackground)
        if (parent != null) {
            val background = (parent.parent as ColorMatchTabLayout).background
            if (background is ColorDrawable) {
                color = background.color
            }
        }
        return color
    }

}