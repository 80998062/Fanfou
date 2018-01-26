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
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.db.DateConverter
import java.util.*

/**
 * Created by sinyuk on 2018/1/26.
 *
 */
@Entity(tableName = "drafts", indices = [(Index("id"))])
@TypeConverters(DateConverter::class)
data class Draft @JvmOverloads constructor(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var createdAt: Date? = null,
        @SerializedName("status") @ColumnInfo(typeAffinity = ColumnInfo.BLOB) var content: ByteArray? = null,
        @SerializedName("in_reply_to_status_id") var inReplyToStatusId: String? = null,
        @SerializedName("in_reply_to_user_id") var inReplyToUserId: String? = null,
        @SerializedName("repost_status_id") var repostStatusId: String? = null,
        @SerializedName("source") var source: String? = null,
        @SerializedName("location") var location: String? = null) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.readSerializable() as Date?,
            source.createByteArray(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeSerializable(createdAt)
        writeByteArray(content)
        writeString(inReplyToStatusId)
        writeString(inReplyToUserId)
        writeString(repostStatusId)
        writeString(source)
        writeString(location)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Draft

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (!Arrays.equals(content, other.content)) return false
        if (inReplyToStatusId != other.inReplyToStatusId) return false
        if (inReplyToUserId != other.inReplyToUserId) return false
        if (repostStatusId != other.repostStatusId) return false
        if (source != other.source) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (content?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (inReplyToStatusId?.hashCode() ?: 0)
        result = 31 * result + (inReplyToUserId?.hashCode() ?: 0)
        result = 31 * result + (repostStatusId?.hashCode() ?: 0)
        result = 31 * result + (source?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Draft> = object : Parcelable.Creator<Draft> {
            override fun createFromParcel(source: Parcel): Draft = Draft(source)
            override fun newArray(size: Int): Array<Draft?> = arrayOfNulls(size)
        }
    }
}