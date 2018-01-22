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

import android.content.Intent


/**
 * Created by sinyuk on 2018/1/17.
 *
 */
object PictureHelper {

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    fun fileSearchIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        addCategory(Intent.CATEGORY_OPENABLE)
        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        type = "image/*"
    }

}