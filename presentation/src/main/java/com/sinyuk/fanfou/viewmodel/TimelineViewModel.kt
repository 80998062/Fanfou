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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.repo.inDb.DbStatusRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
class TimelineViewModel @Inject constructor(private val repo: DbStatusRepository) : ViewModel() {

    private val pathLive = MutableLiveData<String>()

    var uniqueId: String? = null

    fun setPath(path: String): Boolean {
        if (pathLive.value == path) {
            return false
        }
        pathLive.value = path
        return true
    }

    private val repoResult = map(pathLive, { repo.statusesInPath(path = it, pageSize = PAGE_SIZE, uniqueId = uniqueId) })
    val statuses = Transformations.switchMap(repoResult, { it.pagedList })!!
    val networkState = Transformations.switchMap(repoResult, { it.networkState })!!
    val refreshState = Transformations.switchMap(repoResult, { it.refreshState })!!
    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun load(max: String) = repo.load(path = pathLive.value!!, uniqueId = uniqueId, max = max, pageSize = PAGE_SIZE)
}