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

package com.sinyuk.fanfou.domain

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager


enum class RequestStatus {
    RUNNING,
    SUCCESS,
    FAILED,
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: RequestStatus,
        val msg: String? = null) {
    companion object {
        val LOADED = NetworkState(RequestStatus.SUCCESS)
        val LOADING = NetworkState(RequestStatus.RUNNING)
        fun error(msg: String?) = NetworkState(RequestStatus.FAILED, msg)
    }

    override fun equals(other: Any?): Boolean {
        if (other is NetworkState) {
            return other.status == status && other.msg == msg
        }
        return false
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (msg?.hashCode() ?: 0)
        return result
    }
}


@SuppressLint("MissingPermission")
fun isOnline(application: Application): Boolean {
    val connMgr = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}