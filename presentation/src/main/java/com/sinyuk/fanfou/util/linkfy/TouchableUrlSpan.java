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

package com.sinyuk.fanfou.util.linkfy;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * An extension to URLSpan which changes it's background & foreground color when clicked.
 * <p>
 * Derived from http://stackoverflow.com/a/20905824
 */
public class TouchableUrlSpan extends URLSpan {

    private static int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private boolean isPressed;
    private int normalTextColor;
    private int pressedTextColor;
    private int pressedBackgroundColor;

    /**
     * Instantiates a new Touchable url span.
     *
     * @param url                    the url
     * @param textColor              the text color
     * @param pressedBackgroundColor the pressed background color
     */
    public TouchableUrlSpan(String url, ColorStateList textColor, int pressedBackgroundColor) {
        super(url);
        this.normalTextColor = textColor.getDefaultColor();
        this.pressedTextColor = textColor.getColorForState(STATE_PRESSED, normalTextColor);
        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    /**
     * Sets pressed.
     *
     * @param isPressed the is pressed
     */
    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    @Override
    public void updateDrawState(TextPaint drawState) {
        drawState.setColor(isPressed ? pressedTextColor : normalTextColor);
        drawState.bgColor = isPressed ? pressedBackgroundColor : 0;
        drawState.setUnderlineText(!isPressed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isPressed ? (byte) 1 : (byte) 0);
        dest.writeInt(this.normalTextColor);
        dest.writeInt(this.pressedTextColor);
        dest.writeInt(this.pressedBackgroundColor);
    }

    private TouchableUrlSpan(Parcel in) {
        super(in);
        this.isPressed = in.readByte() != 0;
        this.normalTextColor = in.readInt();
        this.pressedTextColor = in.readInt();
        this.pressedBackgroundColor = in.readInt();
    }

    /**
     * The constant CREATOR.
     */
    public static final Creator<TouchableUrlSpan> CREATOR = new Creator<TouchableUrlSpan>() {
        @Override
        public TouchableUrlSpan createFromParcel(Parcel source) {
            return new TouchableUrlSpan(source);
        }

        @Override
        public TouchableUrlSpan[] newArray(int size) {
            return new TouchableUrlSpan[size];
        }
    };
}