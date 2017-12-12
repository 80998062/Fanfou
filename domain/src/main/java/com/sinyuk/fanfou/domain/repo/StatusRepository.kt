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

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.util.AbsentLiveData

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
@Singleton
class StatusRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val db: LocalDatabase,
        @Named(DATABASE_IN_MEMORY) private val memory: LocalDatabase) : AbstractRepository(url, interceptor) {


    fun timeline(path: String, max: String?, forcedUpdate: Boolean = false) =
            object : NetworkBoundResource<MutableList<Status>, MutableList<Status>>(appExecutors) {
                override fun onFetchFailed() {}

                override fun saveCallResult(item: MutableList<Status>?) {
                    val inMemory = when (path) {
                        TIMELINE_PUBLIC -> true
                        TIMELINE_HOME -> false
                        else -> true
                    }
                    item?.let { saveStatus(it, inMemory) }
                }

                override fun shouldFetch(data: MutableList<Status>?) =
                        /*networkConnected(application) && rateLimiter.shouldFetch(KEY) && */(forcedUpdate || data == null || data.isEmpty())

                override fun loadFromDb(): LiveData<MutableList<Status>?> = loadTimelineFromDb(path, max)

                override fun createCall() = when (path) {
                    TIMELINE_HOME -> restAPI.fetch_from_path(TIMELINE_HOME, null, max)
                    TIMELINE_PUBLIC -> restAPI.fetch_from_path(TIMELINE_PUBLIC, null, max)
                    else -> restAPI.fetch_from_path(TIMELINE_HOME, null, max)
                }

            }.asLiveData()

    private fun loadTimelineFromDb(path: String, max: String?) = when (path) {
        TIMELINE_PUBLIC -> if (max == null) {
            memory.statusDao().initial(PAGE_SIZE)
        } else {
            Transformations.switchMap(memory.statusDao().query(max), {
                if (it == null) {
                    AbsentLiveData.create()
                } else {
                    memory.statusDao().after(max, PAGE_SIZE)
                }
            })
        }
        TIMELINE_HOME -> if (max == null) {
            db.statusDao().initial(PAGE_SIZE)
        } else {
            Transformations.switchMap(db.statusDao().query(max), {
                if (it == null) {
                    AbsentLiveData.create()
                } else {
                    db.statusDao().after(max, PAGE_SIZE)
                }
            })
        }
        else -> TODO()
    }


    fun fetchTimeline(path: String, since: String?, uiqueId: String?): MutableLiveData<Resource<MutableList<Status>>> {
        val task = when (path) {
            TIMELINE_HOME, TIMELINE_PUBLIC -> FetchNewTimeLineTask(db, { restAPI.fetch_from_path(path, since, null) })
            else -> TODO()
        }
        appExecutors.diskIO().execute(task)
        return task.liveData
    }

    private fun saveStatus(t: MutableList<Status>, inMemory: Boolean) {
        db.beginTransaction()
        try {
            for (status in t) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                if (inMemory) {
                    Log.d("saveStatus", "in memory: " + memory.statusDao().insert(status))
                } else {
                    Log.d("saveStatus", "in disk: " + db.statusDao().insert(status))
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}