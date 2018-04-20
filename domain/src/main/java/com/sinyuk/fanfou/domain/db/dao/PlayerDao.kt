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

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.sinyuk.fanfou.domain.DO.Player


/**
 * Created by sinyuk on 2017/11/27.
 *
 */
@Dao
interface PlayerDao {

    @Delete
    fun delete(player: Player): Int

    @Query("SELECT * FROM players WHERE uniqueId = :uniqueId")
    fun queryAsLive(uniqueId: String?): LiveData<Player?>

    @Query("SELECT * FROM players WHERE uniqueId = :uniqueId")
    fun query(uniqueId: String?): Player?

    @Insert(onConflict = REPLACE)
    fun insert(player: Player?): Long?

    @Insert(onConflict = REPLACE)
    fun inserts(items: MutableList<Player>): List<Long>

    @Update(onConflict = REPLACE)
    fun update(player: Player)

    @Query("SELECT * FROM players WHERE pathFlag & :path = :path ORDER BY screenName ASC")
    fun players(path: Int): DataSource.Factory<Int, Player>

    @Query("SELECT * FROM players WHERE screenName LIKE '%' || :query || '%' ORDER BY mentionedAt DESC")
    fun filterAsLive(query: String): LiveData<MutableList<Player>>

    @Query("SELECT * FROM players WHERE screenName LIKE '%' || :query || '%' ORDER BY mentionedAt DESC")
    fun filter(query: String): MutableList<Player>

    @Query("SELECT * FROM players WHERE pathFlag & :path = :path ORDER BY updatedAt DESC")
    fun byPath(path: Int): LiveData<MutableList<Player>>
}