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

@file:Suppress("unused")

package com.sinyuk.fanfou.domain.DO

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.linkedin.android.spyglass.mentions.Mentionable
import com.sinyuk.fanfou.domain.convertPlayerPathToFlag
import com.sinyuk.fanfou.domain.db.DateConverter
import java.util.*


/**
 * Created by sinyuk on 2017/11/27.
 *
 */

@Entity(tableName = "players", indices = [(Index("uniqueId")), (Index("name"))])
@TypeConverters(DateConverter::class)
data class Player @JvmOverloads constructor(
        @PrimaryKey @NonNull @SerializedName("unique_id") var uniqueId: String = "",
        @SerializedName("id") var id: String = "",
        @SerializedName("name") var name: String? = "",
        @SerializedName("screen_name") var screenName: String? = "",
        @SerializedName("location") var location: String? = "",
        @SerializedName("gender") var gender: String? = "",
        @SerializedName("birthday") var birthday: String? = "",
        @SerializedName("description") var description: String? = "",
        @SerializedName("profile_image_url") var profileImageUrl: String? = "",
        @SerializedName("profile_image_url_large") var profileImageUrlLarge: String? = "",
        @SerializedName("url") var url: String? = "",
        @SerializedName("protected") var protectedX: Boolean? = true,
        @SerializedName("followers_count") var followersCount: Int? = 0,
        @SerializedName("friends_count") var friendsCount: Int? = 0,
        @SerializedName("favourites_count") var favouritesCount: Int? = 0,
        @SerializedName("statuses_count") var statusesCount: Int? = 0,
        @SerializedName("photo_count") var photoCount: Int? = 0,
        @SerializedName("following") var following: Boolean? = false,
        @SerializedName("notifications") var notifications: Boolean? = false,
        @SerializedName("created_at") var createdAt: Date? = null,
        @SerializedName("profile_background_image_url") var profileBackgroundImageUrl: String? = "",
        @Embedded(prefix = "access") var authorization: Authorization? = null,
        var pathFlag: Int = 0,
        var mentionedAt: Date? = null,
        var updatedAt: Date? = null
) : Parcelable, Mentionable {
    override fun getSuggestibleId() = uniqueId.hashCode()

    override fun getSuggestiblePrimaryText() = screenName

    override fun getDeleteStyle(): Mentionable.MentionDeleteStyle = Mentionable.MentionDeleteStyle.FULL_DELETE

    override fun getTextForDisplayMode(mode: Mentionable.MentionDisplayMode?) = when (mode) {
        Mentionable.MentionDisplayMode.FULL -> "@" + screenName!! + " "
        else -> ""
    }

    fun addPath(flags: Int) {
        pathFlag = pathFlag or flags
    }

    fun removePath(flags: Int) {
        pathFlag = pathFlag and flags.inv()
    }

    fun addPathFlag(path: String) {
        addPath(convertPlayerPathToFlag(path))
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readSerializable() as Date?,
            source.readString(),
            source.readParcelable<Authorization>(Authorization::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(uniqueId)
        writeString(id)
        writeString(name)
        writeString(screenName)
        writeString(location)
        writeString(gender)
        writeString(birthday)
        writeString(description)
        writeString(profileImageUrl)
        writeString(profileImageUrlLarge)
        writeString(url)
        writeValue(protectedX)
        writeValue(followersCount)
        writeValue(friendsCount)
        writeValue(favouritesCount)
        writeValue(statusesCount)
        writeValue(photoCount)
        writeValue(following)
        writeValue(notifications)
        writeSerializable(createdAt)
        writeString(profileBackgroundImageUrl)
        writeParcelable(authorization, PARCELABLE_WRITE_RETURN_VALUE)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Player> = object : Parcelable.Creator<Player> {
            override fun createFromParcel(source: Parcel): Player = Player(source)
            override fun newArray(size: Int): Array<Player?> = arrayOfNulls(size)
        }
    }
}
