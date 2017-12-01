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

import android.arch.paging.LivePagedListProvider
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.sinyuk.fanfou.domain.entities.Status


/**
 * Created by sinyuk on 2017/11/30.
 */
@Dao
interface StatusDao {
    @Query("SELECT * from statuses ORDER BY id DESC LIMIT :limit")
    fun initial(limit: Int): MutableList<Status>

    @Query("SELECT * from statuses WHERE id < :key ORDER BY id DESC LIMIT :limit")
    fun idLoadAfter(key: String, limit: Int): MutableList<Status>

    @Query("SELECT * from statuses WHERE id > :key ORDER BY id ASC LIMIT :limit")
    fun idLoadBefore(key: String, limit: Int): MutableList<Status>


    @Query("SELECT * from statuses  WHERE uniqueId = :uniqueId ORDER BY createdAt DESC, id ASC")
    fun queryById(uniqueId: String): LivePagedListProvider<Int, Status>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(statuses: List<Status>): List<Long>
}