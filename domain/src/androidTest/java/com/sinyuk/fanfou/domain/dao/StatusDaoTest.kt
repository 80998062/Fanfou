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
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.TIMELINE_PUBLIC
import com.sinyuk.fanfou.domain.convertPathToFlag
import com.sinyuk.fanfou.domain.db.LocalDatabase
import com.sinyuk.fanfou.domain.db.dao.StatusDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
@RunWith(AndroidJUnit4::class)
class StatusDaoTest {
    lateinit var statusDao: StatusDao
    lateinit var database: LocalDatabase
    private val mockId = "123"
    private val mockUid = "456"

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        database = Room.databaseBuilder(context, LocalDatabase::class.java, "mock.db").build()
        statusDao = database.statusDao()
    }

    @Test
    fun testInsertion() {
        val mock = Status(id = mockId, uid = mockUid)
        // verify single item insert
        assert(statusDao.insert(mock) > 0)
    }

    @Test
    fun testInsertions() {
        val statuses = mutableListOf<Status>()
        for (i in 0 until 10) {
            val mock = Status(
                    id = System.currentTimeMillis().toString(),
                    uid = System.currentTimeMillis().toString())
            statuses.add(mock)
        }
        // verify collection insert
        assert(statusDao.inserts(statuses).size == 10)
    }

    @Test
    fun testComposedPrimaryKey() {
        // Verify Composed PrimaryKey: id + uid
        val mockNewUid = System.currentTimeMillis().toString()
        var mock = Status(id = mockId, uid = mockNewUid)

        assert(statusDao.insert(mock) > 0)
        assert(statusDao.query(id = mockId, uniqueId = mockNewUid) != null)

        val mockNewId = System.currentTimeMillis().toString()
        mock = Status(id = mockNewId, uid = mockUid)

        assert(statusDao.insert(mock) > 0)
        assert(statusDao.query(id = mockNewId, uniqueId = mockUid) != null)

        // verify
        assert(statusDao.query(id = mockId, uniqueId = mockUid) != null)
    }

    @Test
    fun testLoadInitial() {
        val publicPath = convertPathToFlag(TIMELINE_PUBLIC)
        var mockUid = System.currentTimeMillis().toString()
        mockInsertions(mockUid, publicPath, 10)

        assert(statusDao.loadInitial(mockUid, publicPath, 100).size == 10)

        val homePath = convertPathToFlag(TIMELINE_HOME)
        mockUid = System.currentTimeMillis().toString()
        mockInsertions(mockUid, homePath, 5)
        assert(statusDao.loadInitial(mockUid, homePath, 100).size == 5)
    }

    @Test
    fun testOffsetAndLimit() {
        val homePath = convertPathToFlag(TIMELINE_PUBLIC)
        val publicPath = convertPathToFlag(TIMELINE_PUBLIC)
        val mockUid = System.currentTimeMillis().toString()
        val path = homePath and publicPath
        mockInsertions(mockUid, path, 10)

        assert(statusDao.loadAfter(mockUid, homePath, 10, 1).size == 9)
        assert(statusDao.loadAfter(mockUid, publicPath, 10, 9).size == 1)
    }


    private fun mockInsertions(uid: String, path: Int, size: Int) {
        val statuses = mutableListOf<Status>()
        for (i in 0 until size) {
            val mock = Status(
                    id = System.currentTimeMillis().toString(),
                    uid = uid,
                    pathFlag = path)
            statuses.add(mock)
        }
        assert(statusDao.inserts(statuses).size == 10)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }


}
