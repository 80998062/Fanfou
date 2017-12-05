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

package com.sinyuk.fanfou.ui.timeline

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sinyuk.fanfou.domain.Repository
import com.sinyuk.fanfou.domain.TIMELINE_PUBLIC
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.domain.entities.Status
import com.sinyuk.fanfou.lives.PreferenceAwareLiveData
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sinyuk on 2017/11/30.
 */
class TimelineViewModel @Inject constructor(
        private val context: Application,
        private val repository: Repository,
        @Named(TYPE_GLOBAL) private val preferences: RxSharedPreferences) : AndroidViewModel(context) {

    internal val accountRelay: PreferenceAwareLiveData<String> = PreferenceAwareLiveData(preferences.getString(UNIQUE_ID))


    internal fun timeline(timelinePath: String, targetPlayer: String?) =
            when (timelinePath) {
                TIMELINE_PUBLIC -> repository.homeTimeline()
                else -> repository.homeTimeline()
            }


    fun fetchTimeline(path: String, playerId: String?, since: String?, max: String?): Single<MutableList<Status>> {
        return repository.fetchTimeline(path, since, max)
    }


}