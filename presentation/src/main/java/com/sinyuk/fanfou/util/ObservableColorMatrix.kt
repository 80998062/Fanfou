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

import android.graphics.ColorMatrix
import com.sinyuk.myutils.animation.AnimUtils

/**
 * An extension to [ColorMatrix] which caches the saturation value for animation purposes.
 */
class ObservableColorMatrix : ColorMatrix() {

    private var saturation = 1f

    internal fun getSaturation(): Float {
        return saturation
    }

    override fun setSaturation(saturation: Float) {
        this.saturation = saturation
        super.setSaturation(saturation)
    }

    companion object {

        val SATURATION = AnimUtils.createFloatProperty(object : AnimUtils.FloatProp<ObservableColorMatrix>("saturation") {
            override fun get(ocm: ObservableColorMatrix): Float {
                return ocm.getSaturation()
            }

            override fun set(ocm: ObservableColorMatrix, saturation: Float) {
                ocm.setSaturation(saturation)
            }
        })
    }

}