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

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.sinyuk.fanfou.domain.rest.VisibleThrowable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import java.io.IOException
import java.net.SocketException

/**
 * Created by sinyuk on 2017/11/29.
 */
class GlobalErrorHandler constructor(private val application: Application) : Consumer<Throwable> {

    override fun accept(e: Throwable?) {
        e?.let {
            if (e is VisibleThrowable) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
                return
            }

            val throwable: Throwable = (if (e is UndeliverableException) {
                e.cause
            } else {
                e
            }) ?: return

            if ((throwable is IOException) || (throwable is SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                if (throwable is VisibleThrowable) {
                    Toast.makeText(application, throwable.message, Toast.LENGTH_SHORT).show()
                }
                return
            }
            if (throwable is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                if (throwable is VisibleThrowable) {
                    Toast.makeText(application, throwable.message, Toast.LENGTH_SHORT).show()
                }
                return
            }
            if ((throwable is NullPointerException) || (throwable is IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), throwable)
                return
            }
            if (throwable is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), throwable)
                return
            }
            Log.w("UndeliverableException:", throwable)
        }
    }
}