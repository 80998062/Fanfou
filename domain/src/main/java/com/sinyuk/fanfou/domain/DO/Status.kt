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

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.convertPathToFlag
import com.sinyuk.fanfou.domain.db.DateConverter
import java.util.*

/**
 * Created by sinyuk on 2017/3/28.
 *
 */

@Entity(tableName = "statuses",
        indices = arrayOf(Index("id", "createdAt")))
@TypeConverters(DateConverter::class)
data class Status constructor(
        @PrimaryKey @NonNull @SerializedName("id") var id: String = "",
        @SerializedName("text") var text: String? = null,
        @SerializedName("source") var source: String? = null,
        @SerializedName("location") var location: String? = null,
        @Ignore @SerializedName("user") var user: Player? = null,
        @SerializedName("created_at") var createdAt: Date? = null,
        @Embedded(prefix = "player_") var playerExtracts: PlayerExtracts? = null,
        @Embedded(prefix = "photo_") @SerializedName("photo") var photos: Photos? = null,
        @SerializedName("isSelf") var isSelf: Boolean = false,
        @SerializedName("favorited") var favorited: Boolean = false,
        var pathFlag: Int = 0
) : Parcelable {
    fun addPath(flags: Int) {
        pathFlag = pathFlag or flags
    }

    fun removePath(flags: Int) {
        pathFlag = pathFlag and flags.inv()
    }

    fun addPathFlag(path: String) {
        addPath(convertPathToFlag(path))
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<Player>(Player::class.java.classLoader),
            source.readSerializable() as Date?,
            source.readParcelable<PlayerExtracts>(PlayerExtracts::class.java.classLoader),
            source.readParcelable<Photos>(Photos::class.java.classLoader),
            1 == source.readInt(),
            1 == source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(text)
        writeString(source)
        writeString(location)
        writeParcelable(user, 0)
        writeSerializable(createdAt)
        writeParcelable(playerExtracts, 0)
        writeParcelable(photos, 0)
        writeInt((if (isSelf) 1 else 0))
        writeInt((if (favorited) 1 else 0))
        writeInt(pathFlag)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Status> = object : Parcelable.Creator<Status> {
            override fun createFromParcel(source: Parcel): Status = Status(source)
            override fun newArray(size: Int): Array<Status?> = arrayOfNulls(size)
        }
    }
}
