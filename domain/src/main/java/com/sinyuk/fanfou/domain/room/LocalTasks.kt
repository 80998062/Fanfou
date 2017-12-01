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

import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListProvider
import com.sinyuk.fanfou.domain.entities.Player
import com.sinyuk.fanfou.domain.entities.Registration
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.domain.rest.Authorization

/**
 * Created by sinyuk on 2017/11/28.
 */
interface LocalTasks {
    fun queryRegistration(uniqueId: String): Registration?

    fun deleteRegistration(uniqueId: String): Int

    fun queryPlayer(uniqueId: String): LiveData<Player>

    fun queryAdmins(): LiveData<List<Player>>

    fun insertRegistration(uniqueId: String, account: String, password: String, authorization: Authorization): Long

    fun insertPlayer(player: Player): Long

    fun insertPlayers(players: List<Player>): Int

    fun homeTimeline(uniqueId: String): LivePagedListProvider<Int, Status>

    fun saveStatuses(statuses: List<Status>):List<Long>
}