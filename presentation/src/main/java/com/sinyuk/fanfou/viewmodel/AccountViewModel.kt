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
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.support.annotation.VisibleForTesting
import com.sinyuk.fanfou.domain.repo.AccountRepository
import com.sinyuk.fanfou.domain.vo.Authorization
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.domain.vo.Resource
import com.sinyuk.fanfou.util.AbsentLiveData
import javax.inject.Inject


/**
 * Created by sinyuk on 2017/12/6.
 */
class AccountViewModel @Inject constructor(val repo: AccountRepository) : ViewModel() {

    @VisibleForTesting
    val login = MutableLiveData<Authorization>()

    private val user: LiveData<Resource<Player>>? by lazy {
        Transformations.switchMap(login, {
            if (it?.secret == null) {
                AbsentLiveData.create()
            } else {
                repo.loadAccount()
            }
        })
    }


    fun checkLogin() {
        Authorization(repo.accessToken(), repo.accessSecret())
    }

}