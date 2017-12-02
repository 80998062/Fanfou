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

package com.sinyuk.fanfou.domain.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.entities.*
import com.sinyuk.fanfou.domain.room.dao.*


/**
 * Created by sinyuk on 2017/11/27.
 */
@Database(entities = arrayOf(
        Player::class,
        Registration::class,
        Status::class,
        PlayerAndStatus::class,
        PlayerAndLike::class),
        version = BuildConfig.VERSION_CODE)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun registrationDao(): RegistrationDao
    abstract fun statusDao(): StatusDao
    abstract fun playerAndStatusDao(): PlayerAndStatusDao
    abstract fun playerAndLikeDao(): PlayerAndLikeDao

    companion object {

        private var INSTANCE: LocalDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): LocalDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            LocalDatabase::class.java, "fanfou.db")
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}