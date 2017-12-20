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
import com.sinyuk.fanfou.domain.DO.Status


/**
 * Created by sinyuk on 2017/11/30.
 *
 */
@Dao
interface StatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserts(statuses: MutableList<Status>): MutableList<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(status: Status): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updates(statuses: MutableList<Status>): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(status: Status): Int


    @Query("SELECT * FROM statuses WHERE id = :id")
    fun query(id: String): Status?


    @Query("SELECT * FROM statuses WHERE createdAt < (SELECT createdAt FROM statuses WHERE id = :id) LIMIT 1")
    fun queryNext(id: String): Status?

    @Query("SELECT * FROM statuses WHERE createdAt < (SELECT createdAt FROM statuses WHERE id = :id) AND playeruniqueId = :uniqueId LIMIT 1")
    fun queryNextUsers(uniqueId: String?, id: String): Status?


    @Query("SELECT * FROM statuses ORDER BY createdAt DESC")
    fun home(): DataSource.Factory<Int, Status>

    @Query("SELECT * FROM statuses WHERE playeruniqueId = :uniqueId ORDER BY createdAt DESC")
    fun user(uniqueId: String?): DataSource.Factory<Int, Status>


    @Query("SELECT * FROM statuses WHERE favorited = 1 AND playeruniqueId = :uniqueId ORDER BY createdAt DESC")
    fun like(uniqueId: String?): DataSource.Factory<Int, Status>

    fun self(): DataSource.Factory<Int, Status>

    fun favorites(): DataSource.Factory<Int, Status>

}