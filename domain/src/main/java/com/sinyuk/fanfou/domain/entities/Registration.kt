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
import com.sinyuk.fanfou.domain.room.DateConverter
import java.util.*

/**
 * Created by sinyuk on 2017/11/29.
 */
@Entity(tableName = "registrations",
        indices = arrayOf(Index("uniqueId"), Index("account")),
        foreignKeys = arrayOf(ForeignKey(onDelete = ForeignKey.NO_ACTION, entity = Player::class, parentColumns = arrayOf("uniqueId"), childColumns = arrayOf("uniqueId"))))
@TypeConverters(DateConverter::class)
data class Registration constructor(
        @PrimaryKey @NonNull var uniqueId: String = "",
        var account: String? = "",
        var password: String? = "",
        var loggedAt: Date? = null,
        var token: String? = "",
        var secret: String? = ""
)