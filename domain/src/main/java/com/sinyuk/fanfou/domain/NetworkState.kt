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


enum class Status {
    RUNNING,
    SUCCESS,
    FAILED,
    REACH_BOTTOM,
    REACH_TOP
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val msg: String? = null) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        val REACH_BOTTOM = NetworkState(Status.REACH_BOTTOM)
        val REACH_TOP = NetworkState(Status.REACH_TOP)
        fun error(msg: String?) = NetworkState(Status.FAILED, msg)
    }
}


@SuppressLint("MissingPermission")
fun isOnline(application: Application): Boolean {
    val connMgr = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}