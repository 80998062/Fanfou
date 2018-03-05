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

package com.sinyuk.fanfou.domain.repo.inDb

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.db.LocalDatabase

/**
 * Created by sinyuk on 2018/1/29.
 *
 */
class StatusDataSourceFactory(private val db: LocalDatabase, private val path: Int,private val uniqueId:String) : DataSource.Factory<String, Status> {
    val sourceLiveData = MutableLiveData<StatusDataSource>()

    override fun create(): DataSource<String, Status> {
        val source = StatusDataSource(db, path,uniqueId)
        sourceLiveData.postValue(source)
        return source
    }
}