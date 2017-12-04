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

import android.annotation.SuppressLint
import android.arch.paging.KeyedDataSource
import com.sinyuk.fanfou.domain.Repository
import com.sinyuk.fanfou.domain.entities.Status


/**
 * Created by sinyuk on 2017/12/1.
 */
@SuppressLint("RestrictedApi")
class KeyedStatusDataSource constructor(private val repository: Repository, private val path: String) : KeyedDataSource<String, Status>() {


    override fun isInvalid(): Boolean {
        return super.isInvalid()
    }


    override fun loadBefore(currentBeginKey: String, pageSize: Int) = repository.loadTimelineBefore(path, currentBeginKey, pageSize)


    override fun loadAfter(currentEndKey: String, pageSize: Int) = repository.loadTimelineAfter(path, currentEndKey, pageSize)

    override fun loadInitial(pageSize: Int): MutableList<Status> = repository.loadTimelineInitial(path, pageSize)


    override fun getKey(item: Status): String = item.id
}