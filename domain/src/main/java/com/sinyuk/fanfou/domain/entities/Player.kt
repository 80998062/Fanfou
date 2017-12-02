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

package com.sinyuk.fanfou.domain.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.room.DateConverter
import java.util.*


/**
 * Created by sinyuk on 2017/11/27.
 *
 */

@Entity(tableName = "players", indices = arrayOf(Index("uniqueId"), Index("name")))
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
        @SerializedName("protected") var protectedX: Boolean? = false,
        @SerializedName("followers_count") var followersCount: Int? = 0,
        @SerializedName("friends_count") var friendsCount: Int? = 0,
        @SerializedName("favourites_count") var favouritesCount: Int? = 0,
        @SerializedName("statuses_count") var statusesCount: Int? = 0,
        @SerializedName("photo_count") var photoCount: Int? = 0,
        @SerializedName("following") var following: Boolean? = false,
        @SerializedName("notifications") var notifications: Boolean? = false,
        @SerializedName("created_at") var createdAt: Date? = null,
        @SerializedName("profile_background_image_url") var profileBackgroundImageUrl:String?= ""
)
