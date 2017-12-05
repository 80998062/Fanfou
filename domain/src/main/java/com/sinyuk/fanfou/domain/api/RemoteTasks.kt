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

package com.sinyuk.fanfou.domain.api

import com.sinyuk.fanfou.domain.vo.Player
import com.sinyuk.fanfou.domain.vo.Status
import io.reactivex.Single

/**
 * Created by sinyuk on 2017/11/28.
 */
interface RemoteTasks {
    fun requestToken(account: String, password: String): Single<Authorization?>
    fun updateProfile(): Single<Player>
    fun fetchPlayer(uniqueId: String): Single<Player>
    fun fetchFromPath(path: String, since: String?, max: String?): Single<MutableList<Status>>
    fun fetchFavorites(uniqueId: String?, since: String?, max: String?): Single<MutableList<Status>>

}