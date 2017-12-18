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
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.db.DateConverter
import java.util.*

/**
 * Created by sinyuk on 2017/3/28.
 */

@Entity(tableName = "statuses",
        indices = arrayOf(Index("id", "createdAt")))
@TypeConverters(DateConverter::class)
data class Status constructor(
        @PrimaryKey @NonNull @SerializedName("id") var id: String = "",
        @SerializedName("rawid") var rawid: Int? = 0,
        @SerializedName("text") var text: String? = null,
        @SerializedName("source") var source: String? = null,
        @SerializedName("in_reply_to_status_id") var inReplyToStatusId: String? = null,
        @SerializedName("in_reply_to_screen_name") var inReplyToScreenName: String? = null,
        @SerializedName("repost_screen_name") var repostScreenName: String? = null,
        @SerializedName("repost_status_id") var repostStatusId: String? = null,
        @SerializedName("location") var location: String? = null,
        @Ignore @SerializedName("user") var user: Player? = null,
        @SerializedName("created_at") var createdAt: Date? = null,
        @Embedded(prefix = "player") var playerExtracts: PlayerExtracts? = null,
        @Embedded(prefix = "photo") @SerializedName("photo") var photos: Photos? = null,
        @SerializedName("isSelf") var isSelf: Boolean = false,
        @SerializedName("favorited") var favorited: Boolean = false,
        @SerializedName("repost_user_id") var repostUserId: String? = null,
        @SerializedName("in_reply_to_user_id") var inReplyToUserId: String? = null,
        var breakPoint: Boolean = false,
        var sibling: String? = null

)
