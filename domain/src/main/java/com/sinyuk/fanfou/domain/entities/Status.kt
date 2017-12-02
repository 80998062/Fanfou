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

package com.sinyuk.fanfou.domain.entities

import android.arch.persistence.room.*
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.room.DateConverter
import java.util.*

/**
 * Created by sinyuk on 2017/3/28.
 */

@Entity(tableName = "statuses",
        indices = arrayOf(Index("id", "createdAt")))
//foreignKeys = arrayOf(ForeignKey(onDelete = NO_ACTION, entity = Player::class, parentColumns = arrayOf("uniqueId"), childColumns = arrayOf("uniqueId")))
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
        @SerializedName("is_self") var isSelf: Boolean = false,
        @SerializedName("favorited") var favorited: Boolean = false,
        var collectorIds: String? = null,
        @SerializedName("repost_user_id") var repostUserId: String? = null,
        @SerializedName("in_reply_to_user_id") var inReplyToUserId: String? = null
) {
    /**
     * 添加一个收藏者
     */
    fun addCollector(uniqueId: String) {
        if (collectorIds?.contains(uniqueId.toRegex()) == true) {
            // no-op
        } else {
            if (collectorIds != null) {
                collectorIds += ","
            }
            collectorIds += uniqueId
        }
    }

    /**
     * 移除一个收藏者
     */
    fun removeCollector(uniqueId: String) {
        if (collectorIds?.contains(uniqueId.toRegex()) == true) {
            if (collectorIds == uniqueId) {
                collectorIds = null
            } else {
                collectorIds!!.replace((uniqueId + ","), "", false)
            }
        }
    }
}
