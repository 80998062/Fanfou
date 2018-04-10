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

package com.sinyuk.fanfou.domain.repo

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.db.LocalDatabase

/**
 * Created by sinyuk on 2018/1/15.
 *
 */
class PlayerTiledDataSourceFactory(private val restAPI: RestAPI,
                                   private val path: String,
                                   private val uniqueId: String? = null,
                                   private val db: LocalDatabase,
                                   private val appExecutors: AppExecutors) : DataSource.Factory<Int, Player>() {
    val sourceLiveData = MutableLiveData<PlayerTiledDataSource>()

    override fun create(): DataSource<Int, Player> {
        val source = PlayerTiledDataSource(restAPI = restAPI, path = path, uniqueId = uniqueId, appExecutors = appExecutors, db = db)
        sourceLiveData.postValue(source)
        return source
    }
}