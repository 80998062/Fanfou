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

package com.sinyuk.fanfou.domain.db.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import com.sinyuk.fanfou.domain.DO.Favorite

/**
 * Created by sinyuk on 2017/12/21.
 *
 */
@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserts(statuses: MutableList<Favorite>): MutableList<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(status: Favorite): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updates(statuses: MutableList<Favorite>): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(status: Favorite): Int

    @Query("SELECT * FROM favorites WHERE id = :id")
    fun query(id: String): Favorite?

    @Query("SELECT * FROM favorites WHERE createdAt < (SELECT createdAt FROM favorites WHERE id = :id) AND player_uniqueId = uniqueId LIMIT 1")
    fun queryNext(id: String, uniqueId: String): Favorite?

    @Query("SELECT * FROM favorites WHERE player_uniqueId = :uniqueId ORDER BY createdAt DESC")
    fun favorites(uniqueId: String): DataSource.Factory<Int, Favorite>
}