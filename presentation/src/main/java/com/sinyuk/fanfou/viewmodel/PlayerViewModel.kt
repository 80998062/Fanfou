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
import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.repo.PlayerRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/8.
 *
 */
class PlayerViewModel @Inject constructor(private val repo: PlayerRepository) : ViewModel() {


    fun profile(uniqueId: String, forced: Boolean = false) = repo.profile(uniqueId, forced)


    data class PlayerPath(val path: String, val uniqueId: String? = null, val query: String? = null)

    private val paramLive = MutableLiveData<PlayerPath>()

    fun setParams(params: PlayerPath): Boolean {
        if (paramLive.value == params) {
            return false
        }
        paramLive.value = params
        return true
    }

    /**
     * 当前登录用户的玩家缓存
     *
     * @param path 路径
     */
    fun cached(path: String) = repo.savedPlayers(path = path, pageSize = PAGE_SIZE)

    private val repoResult = Transformations.map(paramLive, {
        if (it.query == null) {
            repo.fetchPlayers(path = it.path, uniqueId = it.uniqueId, pageSize = PAGE_SIZE)
        } else {
            TODO()
        }
    })

    val players = Transformations.switchMap(repoResult, { it.pagedList })!!
    val networkState = Transformations.switchMap(repoResult, { it.networkState })!!
    val refreshState = Transformations.switchMap(repoResult, { it.refreshState })!!

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun refresh() {
        repoResult?.value?.refresh?.invoke()
    }

}