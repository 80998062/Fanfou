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

import android.arch.paging.PageKeyedDataSource
import android.arch.persistence.room.InvalidationTracker
import android.util.Log
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.db.dao.StatusDao

/**
 * Created by sinyuk on 2018/1/29.
 *
 */
class StatusDataSource(private val db: LocalDatabase, private val dao: StatusDao, private val path: Int) : PageKeyedDataSource<String, Status>() {

    init {
        db.invalidationTracker.addObserver(object : InvalidationTracker.Observer("statuses") {
            override fun onInvalidated(tables: MutableSet<String>) {
                invalidate()
            }
        })
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Status>) {
        val data = dao.loadInitial(path, params.requestedLoadSize)
        if (BuildConfig.DEBUG) Log.i(TAG, "loadInitial: " + data.size)
        if (data.isEmpty()) {
            callback.onResult(data, null, null)
        } else {
            callback.onResult(data, null, data.last().id)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Status>) {
        val data = dao.loadAfter(path, params.key, params.requestedLoadSize)
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: " + params.key)
        if (BuildConfig.DEBUG) Log.i(TAG, "loadAfter: " + data.size)
        if (data.isEmpty()) {
            callback.onResult(data, null)
        } else {
            callback.onResult(data, data.last().id)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Status>) {
    }

    override fun isInvalid(): Boolean {
        db.invalidationTracker.refreshVersionsAsync()
        return super.isInvalid()
    }

    companion object {
        const val TAG = "StatusDataSource"
    }

}