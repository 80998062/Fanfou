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

package com.sinyuk.fanfou.domain.vo

/**
 * Created by sinyuk on 2017/12/1.
 */
data class PlayerExtracts constructor(
        var uniqueId: String = "",
        var id: String = "",
        var screenName: String? = "",
        var profileImageUrl: String? = "",
        var profileImageUrlLarge: String? = "",
        var profileBackgroundImageUrl: String? = "") {

    constructor(player: Player) : this(
            uniqueId = player.uniqueId,
            id = player.id,
            screenName = player.screenName,
            profileImageUrl = player.profileImageUrl,
            profileImageUrlLarge = player.profileImageUrlLarge,
            profileBackgroundImageUrl = player.profileBackgroundImageUrl
    )
}