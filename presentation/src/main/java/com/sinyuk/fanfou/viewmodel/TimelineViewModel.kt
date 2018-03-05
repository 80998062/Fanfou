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

package com.sinyuk.fanfou.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.inDb.TimelineRepository
import com.sinyuk.fanfou.domain.repo.inMemory.tiled.TiledTimelineRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
class TimelineViewModel @Inject constructor(private val disk: TimelineRepository,
                                            private val tiled: TiledTimelineRepository) : ViewModel() {

    data class TimelinePath(val path: String, val id: String? = null, val query: String? = null)

    private var params: MutableLiveData<TimelinePath> = MutableLiveData()

    fun setParams(path: String, id: String? = null, query: String? = null) {
        params.postValue(TimelinePath(path, id, query))
    }

    private val repoResult: LiveData<Listing<Status>> = map(params, {
        when (it.path) {
            SEARCH_TIMELINE_PUBLIC, SEARCH_USER_TIMELINE -> tiled.statuses(path = it.path, query = it.query, pageSize = PAGE_SIZE, uniqueId = it.id)
            TIMELINE_PHOTO -> tiled.statuses(path = it.path, pageSize = PHOTO_SIZE, uniqueId = it.id)
            TIMELINE_PUBLIC, TIMELINE_CONTEXT, TIMELINE_FAVORITES -> tiled.statuses(path = it.path, uniqueId = it.id, pageSize = PAGE_SIZE)
            TIMELINE_USER -> tiled.statuses(path = it.path, uniqueId = it.id, pageSize = PAGE_SIZE)
            TIMELINE_HOME -> disk.statuses(path = it.path, pageSize = PAGE_SIZE)
            else -> TODO()
        }
    })

    val statuses = Transformations.switchMap(repoResult, { it.pagedList })!!
    val networkState = Transformations.switchMap(repoResult, { it.networkState })!!
    val refreshState = switchMap(repoResult, { it.refreshState })!!


    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun createFavorite(id: String) = disk.createFavorite(id)

    fun destroyFavorite(id: String) = disk.destoryFavorite(id)

    fun delete(id: String) = disk.delete(id)
}