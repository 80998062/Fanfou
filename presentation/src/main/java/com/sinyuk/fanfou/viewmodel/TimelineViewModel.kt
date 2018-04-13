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
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.TIMELINE_CONTEXT
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.repo.Listing
import com.sinyuk.fanfou.domain.repo.timeline.TimelineRepository
import com.sinyuk.fanfou.domain.repo.timeline.tiled.TiledTimelineRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
class TimelineViewModel @Inject constructor(private val repo: TimelineRepository, private val remote: TiledTimelineRepository) : ViewModel() {

    companion object {
        const val TAG = "TimelineViewModel"
    }

    data class RelativeUrl(val path: String, val id: String, val query: String? = null)

    private var relativeUrl: MutableLiveData<RelativeUrl> = MutableLiveData()

    fun setRelativeUrl(path: String, id: String, query: String? = null) {
        setRelativeUrl(RelativeUrl(path, id, query))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setRelativeUrl(url: RelativeUrl) = if (url == relativeUrl.value) {
        false
    } else {
        relativeUrl.value = url
        true
    }


    private val repoResult: LiveData<Listing<Status>> = map(relativeUrl, {
        if (it.path == TIMELINE_FAVORITES || it.path == TIMELINE_CONTEXT) {
            remote.statuses(path = it.path, pageSize = PAGE_SIZE, uniqueId = it.id)
        } else {
            repo.statuses(path = it.path, pageSize = PAGE_SIZE, uniqueId = it.id)
        }
    })

    val statuses = switchMap(repoResult, { it.pagedList })!!
    val networkState = switchMap(repoResult, { it.networkState })!!
    val refreshState = switchMap(repoResult, { it.refreshState })!!


    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun createFavorite(id: String) = repo.createFavorite(id)

    fun destroyFavorite(id: String) = repo.destroyFavorite(id)

    fun delete(id: String) = repo.delete(id)
}