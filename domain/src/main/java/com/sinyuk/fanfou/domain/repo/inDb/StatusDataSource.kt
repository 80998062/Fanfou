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

import android.arch.paging.ItemKeyedDataSource
import android.util.Log
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.db.dao.StatusDao

/**
 * Created by sinyuk on 2018/1/29.
 *
 */
class StatusDataSource(private val dao: StatusDao, private val path: Int) : ItemKeyedDataSource<String, Status>() {

    private val TAG = "StatusDataSource"
    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Status>) {

    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Status>) {
        val data = dao.loadInitial(path, params.requestedLoadSize)
        if (BuildConfig.DEBUG) Log.i(TAG, "loadInitial: " + data.size)
        callback.onResult(data)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Status>) {
        val data = dao.loadAfter(path, params.key, params.requestedLoadSize)
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: " + params.key)
        callback.onResult(data)
    }

    override fun invalidate() {
        super.invalidate()
    }

    override fun getKey(item: Status): String {
        if (BuildConfig.DEBUG) Log.i(TAG, "key: " + item.id)
        return item.id
    }
}