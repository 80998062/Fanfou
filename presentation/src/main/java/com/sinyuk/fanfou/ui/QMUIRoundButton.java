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

package com.sinyuk.fanfou.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.sinyuk.fanfou.R;


/**
 * 使按钮能方便地指定圆角、边框颜色、边框粗细、背景色
 * <p>
 * 注意: 因为该控件的圆角采用 View 的 background 实现, 所以与原生的 <code>android:background</code> 有冲突。
 * <ul>
 * <li>如果在 xml 中用 <code>android:background</code> 指定 background, 该 background 不会生效。</li>
 * <li>如果在该 View 构造完后用 {@link #setBackgroundResource(int)} 等方法设置背景, 该背景将覆盖圆角效果。</li>
 * </ul>
 * </p>
 * <p>
 * 如需在 xml 中指定圆角、边框颜色、边框粗细、背景色等值,采用 xml 属性 {@link com.qmuiteam.qmui.R.styleable#QMUIRoundButton}
 * </p>
 * <p>
 * 如需在 Java 中指定以上属性, 需要通过 {@link #getBackground()} 获取 {@link QMUIRoundButtonDrawable} 对象,
 * 然后使用 {@link QMUIRoundButtonDrawable} 提供的方法进行设置。
 * </p>
 * <p>
 *
 * @see QMUIRoundButtonDrawable
 * </p>
 */
public class QMUIRoundButton extends android.support.v7.widget.AppCompatButton {

    public QMUIRoundButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public QMUIRoundButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.QMUIButtonStyle);
        init(context, attrs, R.attr.QMUIButtonStyle);
    }

    public QMUIRoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        QMUIRoundButtonDrawable bg = QMUIRoundButtonDrawable.fromAttributeSet(context, attrs, defStyleAttr);
        setBackgroundKeepingPadding(this, bg);
    }


    public static void setBackgroundKeepingPadding(View view, Drawable drawable) {
        int[] padding = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()};
        view.setBackground(drawable);
        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }

}