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

package com.sinyuk.fanfou.domain.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.sinyuk.fanfou.domain.entities.Player


/**
 * Created by sinyuk on 2017/11/27.
 */
@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(player: Player): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserts(players: MutableList<Player>): MutableList<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(player: Player): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updates(players: MutableList<Player>): Int

    @Delete
    fun delete(player: Player): Int

    @Query("SELECT * FROM players WHERE uniqueId IN (SELECT uniqueId FROM registrations)")
    fun admins(): LiveData<MutableList<Player>>

    @Query("SELECT * FROM players WHERE uniqueId = :uniqueId")
    fun query(uniqueId: String?): LiveData<Player>
}