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
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DATABASE_IN_DISK
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.api.Endpoint
import com.sinyuk.fanfou.domain.api.Oauth1SigningInterceptor
import com.sinyuk.fanfou.domain.convertPlayerPathToFlag
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.isOnline
import com.sinyuk.fanfou.domain.repo.base.AbstractRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/12/8.
 *
 */
class PlayerRepository @Inject constructor(
        val application: Application,
        url: Endpoint,
        interceptor: Oauth1SigningInterceptor,
        private val appExecutors: AppExecutors,
        @Named(DATABASE_IN_DISK) private val disk: LocalDatabase) : AbstractRepository(application, url, interceptor) {

    fun profile(uniqueId: String, forcedUpdate: Boolean = false) = object : NetworkBoundResource<Player, Player>(appExecutors) {
        override fun onFetchFailed() {
        }

        override fun saveCallResult(item: Player?) {
            item?.let { savePlayer(item) }
        }

        override fun shouldFetch(data: Player?) = isOnline(application) && (forcedUpdate || data == null)


        override fun loadFromDb() = disk.playerDao().queryAsLive(uniqueId)


        override fun createCall() = restAPI.show_user(uniqueId)
    }.asLiveData()

    private fun savePlayer(item: Player) {
        disk.beginTransaction()
        try {
            Log.d("savePlayer", "insert: " + disk.playerDao().insert(item))
            disk.setTransactionSuccessful()
        } finally {
            disk.endTransaction()
        }
    }


    @MainThread
    fun fetchPlayers(path: String, uniqueId: String? = null, pageSize: Int): Listing<Player> {
        val sourceFactory = PlayerTiledDataSourceFactory(restAPI = restAPI, path = path, uniqueId = uniqueId, appExecutors = appExecutors, db = disk)

        val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(false).setPrefetchDistance(pageSize).setInitialLoadSizeHint(pageSize).setPageSize(pageSize).build()

        val pagedList = LivePagedListBuilder(sourceFactory, pagedListConfig).setFetchExecutor(appExecutors.networkIO()).build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return Listing(
                pagedList = pagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData, {
                    it.networkState
                }),
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }

    @MainThread
    fun savedPlayers(path: String, pageSize: Int): LiveData<PagedList<Player>> {
        val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(false).setPrefetchDistance(pageSize).setInitialLoadSizeHint(pageSize).setPageSize(pageSize).build()
        return LivePagedListBuilder(disk.playerDao().players(convertPlayerPathToFlag(path)), pagedListConfig).setFetchExecutor(appExecutors.diskIO()).build()

    }

    @MainThread
    fun filter(keyword: String) = disk.playerDao().filter(query = keyword)

    @WorkerThread
    fun updateMentionedAt(player: Player) {
        appExecutors.diskIO().execute {
            player.mentionedAt = Date(System.currentTimeMillis())
            disk.runInTransaction { disk.playerDao().update(player) }
        }
    }

}