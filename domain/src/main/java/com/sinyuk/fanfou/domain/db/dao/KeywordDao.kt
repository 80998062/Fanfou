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
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.sinyuk.fanfou.domain.DO.Keyword

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
@Dao
interface KeywordDao {
    @Query("SELECT * FROM keys ORDER BY createdAt DESC")
    fun list(): LiveData<MutableList<Keyword>?>

    @Query("SELECT * FROM keys ORDER BY createdAt DESC LIMIT :limit")
    fun take(limit: Int): LiveData<MutableList<Keyword>?>

    @Insert(onConflict = REPLACE)
    fun save(item: Keyword)

    @Insert(onConflict = REPLACE)
    fun create(item: Keyword)

    @Delete
    fun delete(item: Keyword)

    @Query("DELETE FROM keys")
    fun clear()

    @Query("SELECT * FROM keys WHERE query LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun filter(query: String): LiveData<MutableList<Keyword>?>

    @Query("SELECT * FROM keys WHERE query LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit")
    fun filterAndTake(query: String, limit: Int): LiveData<MutableList<Keyword>?>
}