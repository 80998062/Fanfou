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

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import com.sinyuk.fanfou.domain.db.DateConverter

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
@Entity(tableName = "trends", indices = arrayOf(Index("name")))
@TypeConverters(DateConverter::class)
data class Trend @JvmOverloads constructor(
        @PrimaryKey @NonNull @SerializedName("name") var name: String = "",
        @SerializedName("query") var query: String = "",
        @SerializedName("url") var url: String = "")