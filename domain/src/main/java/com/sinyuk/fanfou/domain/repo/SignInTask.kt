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

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import com.sinyuk.fanfou.domain.ACCESS_SECRET
import com.sinyuk.fanfou.domain.ACCESS_TOKEN
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.util.XauthUtils
import com.sinyuk.fanfou.domain.DO.Authorization
import com.sinyuk.fanfou.domain.DO.Resource
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Named


/**
 * Created by sinyuk on 2017/12/6.
 */
class SignInTask
constructor(private val account: String,
            private val password: String,
            private val client: OkHttpClient,
            @Named(TYPE_GLOBAL) private val preferences: SharedPreferences) : Runnable {

    val liveData: MutableLiveData<Resource<Authorization>> = MutableLiveData()

    @SuppressLint("CommitPrefEdits")
    override fun run() {
        try {
            val url: HttpUrl = XauthUtils.newInstance(account, password).url()
            val request = Request.Builder().url(url).build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful && response.body() != null) {
                val text = response.body()!!.string()
                val querySpilt = text.split("&")
                val tokenAttr = querySpilt[0].split("=".toRegex())
                val secretAttr = querySpilt[1].split("=".toRegex())
                val token = tokenAttr[1]
                val secret = secretAttr[1]
                preferences.edit().apply {
                    putString(ACCESS_SECRET, secret)
                    putString(ACCESS_TOKEN, token)
                }.run {
                    apply()
                }
                liveData.postValue(Resource.success(Authorization(token, secret)))
            } else {
                liveData.postValue(Resource.error(response.message(), null))
            }
        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, null))
        }
    }
}