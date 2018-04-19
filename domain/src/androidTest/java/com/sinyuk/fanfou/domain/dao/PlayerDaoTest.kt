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

package com.sinyuk.fanfou.domain.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.USERS_ADMIN
import com.sinyuk.fanfou.domain.USERS_FOLLOWERS
import com.sinyuk.fanfou.domain.convertPlayerPathToFlag
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.db.dao.PlayerDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by sinyuk on 2018/4/19.
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│        _______. __  .__   __. ____    ____  __    __   __  ___   │
│       /       ||  | |  \ |  | \   \  /   / |  |  |  | |  |/  /   │
│      |   (----`|  | |   \|  |  \   \/   /  |  |  |  | |  '  /    │
│       \   \    |  | |  . `  |   \_    _/   |  |  |  | |    <     │
│   .----)   |   |  | |  |\   |     |  |     |  `--'  | |  .  \    │
│   |_______/    |__| |__| \__|     |__|      \______/  |__|\__\   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
 */
@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {
    private lateinit var playerDao: PlayerDao
    private lateinit var database: LocalDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        database = Room.databaseBuilder(context, LocalDatabase::class.java, "mock.db").build()
        playerDao = database.playerDao()
    }


    private val mockScreenName = "Sinyuk"
    private val mockId = "123456"

    @Test
    fun testInsertion() {
        val mock = Player(
                uniqueId = mockId,
                screenName = System.currentTimeMillis().toString(),
                pathFlag = convertPlayerPathToFlag(USERS_ADMIN))
        // verify insertion
        assert(playerDao.insert(mock) ?: 0 > 0)
    }

    @Test
    fun testInsertions() {
        val list = mutableListOf<Player>()
        for (i in 0 until 10) {
            val mock = Player(uniqueId = System.currentTimeMillis().toString())
            list.add(mock)
        }
        // verify insertions
        assert(playerDao.inserts(list).size == 10)
    }


    @Test
    fun testUpdate() {
        val mock = Player(
                uniqueId = mockId,
                screenName = mockScreenName)
        playerDao.update(mock)

        // verify update
        val query = playerDao.query(mockId)
        assert(mockScreenName == query?.screenName)
    }


    @Test
    fun testQueryAsLive() {
        // verify query return livedata
        playerDao.queryAsLive(mockId).observeForever({
            assert(mockScreenName == it?.screenName)
        })
    }


    @Test
    fun testFilter() {
        // verify search
        playerDao.filter("S").observeForever({
            assert(it?.isNotEmpty() ?: false)
        })
    }

    @Test
    fun testQueryByPath() {
        val mock = Player(uniqueId = System.currentTimeMillis().toString())
        mock.addPathFlag(USERS_ADMIN)
        mock.addPathFlag(USERS_FOLLOWERS)
        assert(playerDao.insert(mock) ?: 0 > 0)

        playerDao.byPath(path = convertPlayerPathToFlag(USERS_FOLLOWERS))
                .observeForever({
                    assert(it?.size == 1)
                })

        playerDao.byPath(path = convertPlayerPathToFlag(USERS_ADMIN))
                .observeForever({
                    assert(it?.size == 2)
                })
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }
}

