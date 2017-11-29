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
import com.sinyuk.fanfou.domain.entities.User


/**
 * Created by sinyuk on 2017/11/27.
 */
@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(users: List<User>): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(user: User): Int

    @Delete
    fun delete(user: User): Int

    @Query("SELECT * FROM accounts ORDER BY loggedAt ASC")
    fun all(): LiveData<List<User>>

    @Query("SELECT * FROM accounts WHERE loggedAt IS NOT NULL ORDER BY loggedAt ASC")
    fun allLogged(): LiveData<List<User>>

    @Query("SELECT * FROM accounts WHERE uniqueId = :uniqueId")
    fun query(uniqueId: String): LiveData<User>
}