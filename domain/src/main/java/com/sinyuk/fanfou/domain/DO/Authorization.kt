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

package com.sinyuk.fanfou.domain.DO

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by sinyuk on 2017/12/6.
 */
data class Authorization constructor(val token: String?, val secret: String?) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(token)
        writeString(secret)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Authorization> = object : Parcelable.Creator<Authorization> {
            override fun createFromParcel(source: Parcel): Authorization = Authorization(source)
            override fun newArray(size: Int): Array<Authorization?> = arrayOfNulls(size)
        }
    }
}