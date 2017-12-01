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

package com.sinyuk.fanfou.domain.funcs

import com.sinyuk.fanfou.domain.TIMELINE_HOME
import com.sinyuk.fanfou.domain.entities.PlayerExtracts
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.domain.room.LocalTasks
import io.reactivex.functions.Function

/**
 * Created by sinyuk on 2017/12/1.
 */
class SaveStatusFunc constructor(val localTasks: LocalTasks, val type: String, val id: String) : Function<List<Status>, List<Status>> {

    override fun apply(t: List<Status>): List<Status> {
        when (type) {
            TIMELINE_HOME -> saveInDatabase(t)
        }

        return t
    }

    private fun saveInDatabase(t: List<Status>) {
        for (status in t) {
            status.playerExtracts = PlayerExtracts(status.user)
        }
        localTasks.saveStatuses(t)
    }
}