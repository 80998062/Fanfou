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

package com.sinyuk.fanfou.util

import java.util.*

/**
 * Created by sinyuk on 2017/12/19.
 *
 */
class FanfouFormatter {
    companion object {
        @JvmStatic
        fun convertDateToStr(date: Date): String {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.time = date
            var str = ""

            cal.get(Calendar.MONTH).let {
                str += if (it in 0..8) {
                    "0" + (it + 1)
                } else {
                    (it + 1)
                }
            }

            cal.get(Calendar.DAY_OF_MONTH).let {
                str += "/"
                str += if (it in 1..9) {
                    "0" + it
                } else {
                    it
                }
            }

            cal.get(Calendar.HOUR_OF_DAY).let {
                str += " "
                str += if (it in 0..9) {
                    "0" + it
                } else {
                    it
                }
            }

            cal.get(Calendar.MINUTE).let {
                str += ":"
                str += if (it in 0..9) {
                    "0" + it
                } else {
                    it
                }
            }

            return str
        }
    }
}