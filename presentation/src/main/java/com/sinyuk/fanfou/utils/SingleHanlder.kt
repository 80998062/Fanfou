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

package com.sinyuk.fanfou.utils

import com.sinyuk.fanfou.domain.UNHANDLE_VISIBLE_ERROR_MESSAGE
import com.sinyuk.fanfou.domain.rest.VisibleThrowable
import com.sinyuk.myutils.system.ToastUtils
import io.reactivex.observers.DisposableSingleObserver

/**
 * Created by sinyuk on 2017/12/1.
 */
open class SingleHanlder<T> constructor(private val toast: ToastUtils?) : DisposableSingleObserver<T>() {
    override fun onSuccess(t: T) {
    }

    constructor() : this(null)


    override fun onError(e: Throwable) {
        if (e is VisibleThrowable) {
            toast?.let {
                toast.toastShort(e.message ?: UNHANDLE_VISIBLE_ERROR_MESSAGE)
            }
        }
    }

}