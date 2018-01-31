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

package com.sinyuk.fanfou.domain.api

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * Created by sinyuk on 2018/1/30.
 *
 */
class ConnectionLiveData constructor(private val application: Application) : LiveData<ConnectionModel>() {

    override fun onActive() {
        super.onActive()
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            application.registerReceiver(receiver, this)
        }
    }

    override fun onInactive() {
        application.unregisterReceiver(receiver)
        super.onInactive()
    }

    @Suppress("DEPRECATION")
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (manager.activeNetworkInfo != null && manager.activeNetworkInfo.isConnectedOrConnecting) {
                manager.activeNetworkInfo?.let {
                    when (it.type) {
                        ConnectivityManager.TYPE_WIFI -> postValue(ConnectionModel(type = ConnectivityManager.TYPE_WIFI, connected = true))
                        ConnectivityManager.TYPE_MOBILE -> postValue(ConnectionModel(type = ConnectivityManager.TYPE_MOBILE, connected = true))
                    }
                }
            } else {
                postValue(ConnectionModel(type = 0, connected = false))
            }
        }
    }
}