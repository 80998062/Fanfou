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

import android.os.Parcel;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

import com.sinyuk.myutils.ColorUtils;


/**
 * An extension to {@link ForegroundColorSpan} which allows updating the color or alpha component.
 * Note that Spans cannot invalidate themselves so consumers must ensure that the Spannable is
 * refreshed themselves.
 */
public class TextColorSpan extends ForegroundColorSpan {

    private
    @ColorInt
    int color;

    public TextColorSpan(int color) {
        super(color);
        this.color = color;
    }

    public
    @ColorInt
    int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public void setAlpha(@FloatRange(from = 0f, to = 1f) float alpha) {
        color = ColorUtils.modifyAlpha(color, alpha);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.color);
    }

    protected TextColorSpan(Parcel in) {
        super(in);
        this.color = in.readInt();
    }

    public static final Creator<TextColorSpan> CREATOR = new Creator<TextColorSpan>() {
        @Override
        public TextColorSpan createFromParcel(Parcel source) {
            return new TextColorSpan(source);
        }

        @Override
        public TextColorSpan[] newArray(int size) {
            return new TextColorSpan[size];
        }
    };
}
