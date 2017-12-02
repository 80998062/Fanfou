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
import android.arch.paging.LivePagedListProvider
import com.sinyuk.fanfou.domain.entities.*
import com.sinyuk.fanfou.domain.rest.Authorization
import java.util.*

/**
 * Created by sinyuk on 2017/11/28.
 */
class LocalDataSource constructor(private val application: Application, private val database: LocalDatabase) : LocalTasks {

    override fun mapPlayerAndLike(playerId: String, statusId: String) =
            database.playerAndLikeDao().insert(PlayerAndLike(playerId, statusId))

    override fun mapPlayerAndStatus(playerId: String, statusId: String): Long =
            database.playerAndStatusDao().insert(PlayerAndStatus(playerId, statusId))

    override fun queryStatus(id: String) = database.statusDao().query(id)

    override fun insertStatus(status: Status): Long = database.statusDao().insert(status)

    override fun insertStatuses(statuses: List<Status>) = database.statusDao().inserts(statuses)

    override fun updateStatuses(statuses: List<Status>) = database.statusDao().updates(statuses)

    override fun updateStatus(status: Status) = database.statusDao().update(status)

    override fun homeTimeline(uniqueId: String): LivePagedListProvider<Int, Status> =
            database.playerAndStatusDao().query(uniqueId)

    override fun queryRegistration(uniqueId: String): Registration? = database.registrationDao().query(uniqueId)

    override fun deleteRegistration(uniqueId: String): Int = database.registrationDao().delete(Registration(uniqueId))

    override fun queryPlayer(uniqueId: String): LiveData<Player> = database.playerDao().query(uniqueId)

    override fun queryAdmins(): LiveData<List<Player>> = database.playerDao().admins()

    override fun insertRegistration(uniqueId: String,
                                    account: String,
                                    password: String,
                                    authorization: Authorization): Long =
            database.registrationDao().insert(Registration(uniqueId,
                    account,
                    password,
                    Date(System.currentTimeMillis()),
                    authorization.token,
                    authorization.secret))

    override fun insertPlayer(player: Player) = database.playerDao().insert(player)

    override fun insertPlayers(players: List<Player>) = database.playerDao().inserts(players)

    override fun updatePlayer(player: Player) = database.playerDao().update(player)

    override fun updatePlayers(players: List<Player>): Int = database.playerDao().updates(players)

}