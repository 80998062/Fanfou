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

package com.sinyuk.fanfou.domain.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.paging.PagingRequestHelper
import com.sinyuk.fanfou.domain.NetworkState

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    return PagingRequestHelper.RequestType.values().mapNotNull { report.getErrorFor(it)?.message }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
    val liveData = MutableLiveData<NetworkState>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
            report.hasError() -> liveData.postValue(NetworkState.error(getErrorMessage(report)))
            report.hasReachedBottom() -> liveData.postValue(NetworkState.REACH_BOTTOM)
            report.hasReachedTop() -> liveData.postValue(NetworkState.REACH_TOP)
            else -> liveData.postValue(NetworkState.LOADED)
        }
    }
    return liveData
}
