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

package com.sinyuk.fanfou.domain.common

import android.arch.lifecycle.LiveData
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.RestAPI
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Created by sinyuk on 2018/4/18.
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
 *  implements the RestAPI with controllable requests which return 200/OK
 */
 class MockSucceedAPI constructor(val delegate: BehaviorDelegate<RestAPI>) : RestAPI {
    override fun show_user(uniqueId: String): LiveData<ApiResponse<Player>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verify_credentials(): LiveData<ApiResponse<Player>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch_profile(): Call<Player> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch_from_path(path: String, count: Int, since: String?, max: String?, id: String?, page: Int?): Call<MutableList<Status>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch_favorites(id: String?, count: Int, page: Int): Call<MutableList<Status>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search_statuses(url: String): Call<MutableList<Status>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search_user_statuses(query: String, count: Int, id: String, page: Int?): Call<MutableList<Status>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search_users(query: String, count: Int, id: String, page: Int?): Call<RestAPI.PlayerList> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch_friends(id: String?, count: Int, page: Int?): Call<MutableList<Player>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch_followers(id: String?, count: Int, page: Int?): Call<MutableList<Player>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun trends(): Call<RestAPI.TrendList> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createFavorite(id: String): Call<Status> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteFavorite(id: String): Call<Status> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteStatus(id: String): Call<Status> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun photos(count: Int, since: String?, max: String?, id: String?, page: Int?): Call<MutableList<Status>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}