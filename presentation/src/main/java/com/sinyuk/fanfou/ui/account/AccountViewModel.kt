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

package com.sinyuk.fanfou.ui.account

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Log
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.Repository
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/28.
 */
class AccountViewModel @Inject constructor(
        private val context: Application,
        private val repository: Repository,
        @Named(TYPE_GLOBAL) private val preferences: RxSharedPreferences) : AndroidViewModel(context) {

    internal val loggedAccount = repository.currentAccount()

    internal val allLogged = repository.allLogged()

    init {
        preferences.getString(UNIQUE_ID).asObservable().subscribe { it ->
            Log.d("AccountViewModel", "切换用户: " + it)
        }
    }


    fun login(account: String, password: String) = repository.signIn(account, password)

    fun switchAccount(uniqueId: String) = repository.switchAccount(uniqueId)

}