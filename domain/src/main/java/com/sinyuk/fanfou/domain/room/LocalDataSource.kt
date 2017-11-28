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

package com.sinyuk.fanfou.domain.room

import android.app.Application
import android.arch.lifecycle.LiveData
import com.sinyuk.fanfou.domain.entities.User
import com.sinyuk.fanfou.domain.rest.Authorization
import java.util.*

/**
 * Created by sinyuk on 2017/11/28.
 */
class LocalDataSource constructor(private val application: Application, private val database: LocalDatabase) : LocalTasks {
    override fun allAccounts(): LiveData<List<User>> = database.accountDao().queryAll()

    override fun queryAccount(id: String) = database.accountDao().query(id)

    override fun saveAccount(user: User, account: String, authorization: Authorization, loggedAt: Date): Long {
        user.account = account
        user.secret = authorization.secret!!
        user.token = authorization.token!!
        user.loggedAt = loggedAt
        return database.accountDao().insert(user)
    }


}