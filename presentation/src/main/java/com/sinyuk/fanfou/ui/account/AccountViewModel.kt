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
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.repo.Repository
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.util.PreferenceAwareLiveData
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/28.
 */
class AccountViewModel @Inject constructor(
        private val context: Application,
        private val repository: Repository,
        @Named(TYPE_GLOBAL) private val preferences: RxSharedPreferences) : AndroidViewModel(context) {

    internal val accountRelay: PreferenceAwareLiveData<String> = PreferenceAwareLiveData(preferences.getString(UNIQUE_ID))
    internal fun admin(uniqueId: String?) = repository.admin(uniqueId!!)
    internal fun registration(uniqueId: String = preferences.getString(UNIQUE_ID).get()) = repository.registration(uniqueId)

    internal val admins = repository.admins()

    fun login(account: String, password: String) = repository.signIn(account, password)

    fun updateProfile() = repository.updateProfile(null)

    fun deleteRegistration(uniqueId: String) = repository.deleteRegistration(uniqueId)

}