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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.content.Context
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.common.MockSucceedAPI
import com.sinyuk.fanfou.domain.common.mockAPISucceed
import com.sinyuk.fanfou.domain.db.LocalDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock

/**
 * Created by sinyuk on 2018/4/18.
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                                                                  │
 * │        _______. __  .__   __. ____    ____  __    __   __  ___   │
 * │       /       ||  | |  \ |  | \   \  /   / |  |  |  | |  |/  /   │
 * │      |   (----`|  | |   \|  |  \   \/   /  |  |  |  | |  '  /    │
 * │       \   \    |  | |  . `  |   \_    _/   |  |  |  | |    <     │
 * │   .----)   |   |  | |  |\   |     |  |     |  `--'  | |  .  \    │
 * │   |_______/    |__| |__| \__|     |__|      \______/  |__|\__\   │
 * │                                                                  │
 * └──────────────────────────────────────────────────────────────────┘
 */
@RunWith(JUnit4::class)
class AccountRepositoryTest {
    lateinit var succeedAPI: MockSucceedAPI

    @Mock
    lateinit var mockContext: Context

    lateinit var database: LocalDatabase

    @Mock
    lateinit var playerObserver: Observer<LiveData<Player>>

    lateinit var accountRepository: AccountRepository
    @Before
    fun setup() {
        database = Room.databaseBuilder(mockContext, LocalDatabase::class.java, "fanfou.db").build()
        succeedAPI = mockAPISucceed()
    }

    @Test
    fun verifyCredentialsSucceed() {

    }

    @Test
    fun verifyCredentialsFailed() {

    }

    @After
    fun tearDown() {

    }
}