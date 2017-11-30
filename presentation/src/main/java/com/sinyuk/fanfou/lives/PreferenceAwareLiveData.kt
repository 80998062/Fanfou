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

package com.sinyuk.fanfou.lives

import android.arch.lifecycle.LiveData
import android.util.Log
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by sinyuk on 2017/11/30.
 */
class PreferenceAwareLiveData<T> constructor(private val preference: Preference<T>) : LiveData<T>() {

    var disposable: Disposable? = null

    override fun onActive() {
        super.onActive()
        Log.d("PreferenceAwareLiveData","onActive")
        disposable = preference.asObservable()
                .subscribeOn(Schedulers.computation())
                .doOnError { it.printStackTrace() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("PreferenceAwareLiveData","传递到Fragment")
                    setValue(it)
                })
    }

    override fun onInactive() {
        super.onInactive()
        Log.d("PreferenceAwareLiveData","onInactive")
        disposable?.dispose()
    }
}