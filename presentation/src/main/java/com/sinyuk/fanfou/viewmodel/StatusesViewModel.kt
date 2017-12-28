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

package com.sinyuk.fanfou.viewmodel

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.repo.InMemory.TimelineCacheRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/21.
 *
 */
class StatusesViewModel @Inject constructor(private val repo: TimelineCacheRepository) : ViewModel() {
    val listing = MutableLiveData<Resource<MutableList<Status>>>()
    private val oldItems = mutableListOf<Status>()

    fun update(index: Int, status: Status) {
        if (index < oldItems.size && oldItems[index].id == status.id) {
            oldItems[index] = status
            listing.postValue(Resource.success(oldItems))
        }
    }

    fun refresh(path: String, uniqueId: String, pageSize: Int = PAGE_SIZE, owner: LifecycleOwner) {
        repo.fetchAfterTop(path = path, uniqueId = uniqueId, pageSize = pageSize)
                .observe(owner, Observer<Resource<MutableList<Status>>> {
                    when (it?.states) {
                        States.ERROR -> listing.postValue(Resource.error(it.message, it.data))
                        States.LOADING -> listing.postValue(Resource.loading(it.data))
                        States.SUCCESS -> {
                            if (oldItems.isEmpty()) {
                                listing.postValue(Resource.success(it.data))
                            } else {
                                val oldFirst = oldItems.first().id
                                if (it.data?.isNotEmpty() == true) {
                                    val newList = it.data!!
                                    val spilt = newList.indices.firstOrNull { index -> newList[index].id == oldFirst } ?: newList.size
                                    val append = newList.indices
                                            .filter { it >= spilt }
                                            .map { newList[it] }
                                            .toMutableList()
                                    if (append.isNotEmpty()) {
                                        oldItems.addAll(append)
                                        listing.postValue(Resource.success(oldItems))
                                    } else {
                                        listing.postValue(Resource.success(null))
                                    }
                                }
                            }
                        }
                        else -> TODO()
                    }
                })
    }


    fun fetchAfterTop(path: String, uniqueId: String, max: String, pageSize: Int = PAGE_SIZE, owner: LifecycleOwner) {
        Log.d("StatusesViewModel", "last_id: " + oldItems.last().id)
        Log.d("StatusesViewModel", "max_id: " + max)

        if (oldItems.isNotEmpty() && oldItems.last().id == max) {
            repo.fetchAfterTop(path = path, uniqueId = uniqueId, max = max, pageSize = pageSize)
                    .observe(owner, Observer<Resource<MutableList<Status>>> {
                        when (it?.states) {
                            States.ERROR -> listing.postValue(Resource.error(it.message, it.data))
                            States.LOADING -> listing.postValue(Resource.loading(it.data))
                            States.SUCCESS -> {
                                if (it.data?.isNotEmpty() == true) {
                                    oldItems.addAll(it.data!!)
                                    listing.postValue(Resource.success(oldItems))
                                } else {
                                    listing.postValue(Resource.success(null))
                                }
                            }
                            else -> TODO()
                        }
                    })
        }
    }
}