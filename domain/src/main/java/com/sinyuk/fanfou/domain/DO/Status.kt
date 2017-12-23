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
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.TIMELINE_USER
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
        var pathPublic: Boolean? = null,
        var pathUser: Boolean? = null,
        var pathFavorited: Boolean? = null,
        var breakFavorited: Boolean? = null,
        var breakUser: Boolean? = null,
        var breakPublic: Boolean? = null,
        var xxx: Int = 0
) {

    fun isBreak(path: String) = when (path) {
        TIMELINE_HOME -> pathPublic
        TIMELINE_FAVORITES -> pathFavorited
        TIMELINE_USER -> pathUser
        else -> false
    }

    fun addPathFlag(path: String) {
        when (path) {
            TIMELINE_HOME -> pathPublic = true
            TIMELINE_FAVORITES -> pathFavorited = true
            TIMELINE_USER -> pathUser = true
        }
    }

    fun removeBreakFlag(path: String) {
        when (path) {
            TIMELINE_HOME -> breakPublic = false
            TIMELINE_FAVORITES -> breakFavorited = false
            TIMELINE_USER -> breakUser = false
        }
    }

    fun addBreakFlag(path: String) {
        when (path) {
            TIMELINE_HOME -> breakPublic = true
            TIMELINE_FAVORITES -> breakFavorited = true
            TIMELINE_USER -> breakUser = true
        }
    }

}