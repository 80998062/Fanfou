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

package com.sinyuk.fanfou.domain.util

import android.util.Log
import com.sinyuk.fanfou.domain.vo.PlayerAndLike
import com.sinyuk.fanfou.domain.vo.PlayerAndStatus
import com.sinyuk.fanfou.domain.vo.PlayerExtracts
import com.sinyuk.fanfou.domain.vo.Status
import com.sinyuk.fanfou.domain.db.LocalDatabase
import io.reactivex.functions.Function

/**
 * Created by sinyuk on 2017/12/5.
 */
class SaveInMemory constructor(private val database: LocalDatabase) : Function<MutableList<Status>, MutableList<Status>> {

    override fun apply(t: MutableList<Status>): MutableList<Status> {
        return t
    }

    companion object {
        fun saveInDatabase(t: MutableList<Status>, database: LocalDatabase, currentUser: String) {
            Log.d(SaveInDisk::class.java.simpleName, "获取到" + t.size + "条消息")
            var count = 0
            for (status in t) {
                // add user extras data to database
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }

                if (status.favorited) {
                    val localStatus = database.statusDao().query(status.id)
                    if (localStatus == null) {
                        status.addCollector(currentUser)
                        database.statusDao().insert(status)
                        count++
                    } else {
                        status.collectorIds = localStatus.collectorIds
                        status.addCollector(currentUser)
                        count += database.statusDao().update(status)
                    }
                } else {
                    database.statusDao().insert(status)
                    count++
                }

                // make sure insert states first to get foreign key worked
                // mapper current player to this states
                database.playerAndStatusDao().insert(PlayerAndStatus(currentUser, status.id, currentUser + status.id))

                if (status.favorited) {
                    database.playerAndLikeDao().insert(PlayerAndLike(currentUser, status.id, currentUser + status.id))
                }
            }
            Log.d(SaveInDisk::class.java.simpleName, "保存了" + count + "条消息")
        }
    }
}