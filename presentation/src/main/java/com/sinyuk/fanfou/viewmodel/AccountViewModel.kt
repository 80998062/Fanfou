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
import com.sinyuk.fanfou.domain.util.AbsentLiveData
import com.sinyuk.fanfou.domain.vo.Authorization
import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.domain.vo.Resource
import com.sinyuk.fanfou.util.Objects
import javax.inject.Inject


/**
 * Created by sinyuk on 2017/12/6.
 *
 */
class AccountViewModel @Inject constructor(val repo: AccountRepository) : ViewModel() {

    @VisibleForTesting
    private val login = MutableLiveData<Authorization>()

    init {
        login.value = Authorization(repo.accessToken(), repo.accessSecret())
    }

    val user: LiveData<Resource<Player>> = Transformations.switchMap(login, {
        if (it?.secret == null) {
            AbsentLiveData.create()
        } else {
            repo.verifyCredentials()
        }
    })

    fun loadmore(max: String?) = repo.timeline(max)


    /**
     * 检查是否登录
     */
    fun checkLogin() {
        val current = Authorization(repo.accessToken(), repo.accessSecret())
        if (!Objects.equals(current, login.value)) {
            login.value = current
        }
    }

    fun sign(account: String, password: String) = repo.sign(account, password)

}