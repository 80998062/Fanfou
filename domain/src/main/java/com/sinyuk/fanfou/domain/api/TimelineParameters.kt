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

package com.sinyuk.fanfou.domain.api

import com.sinyuk.fanfou.domain.LINK_FORMAT_HTML
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.PROFILE_MODE_DEFAULT
import java.util.*

/**
 * Created by sinyuk on 2017/11/30.
 */
@Deprecated("好像没用啊")
class TimelineParameters private constructor(builder: Builder) {

    private var sinceId: String? = null
    private var maxId: String? = null
    private var count: Int? = null
    private var page: Int? = null
    private var mode: String? = null
    private var format: String? = null
    private var id: String? = null

    init {
        sinceId = builder.sinceId
        maxId = builder.maxId
        count = builder.count
        page = builder.page
        mode = builder.mode
        format = builder.format
        id = builder.id
    }

    fun toSortedMap(): SortedMap<String, Any> {
        val params: SortedMap<String, Any> = sortedMapOf()
        sinceId?.let { params["since_id"] = it }
        maxId?.let { params["max_id"] = it }
        id?.let { params["id"] = it }
        count?.let { params["count"] = it }
        page?.let { params["page"] = it }
        mode.let { params["mode"] = it }
        format?.let { params["format"] = it }
        return params
    }

    internal data class Builder constructor(
            var sinceId: String? = null,
            var maxId: String? = null,
            var count: Int? = PAGE_SIZE,
            var page: Int? = null,
            var mode: String? = PROFILE_MODE_DEFAULT,
            var format: String? = LINK_FORMAT_HTML,
            var id: String? = null
    ) {
        fun sinceId(sinceId: String?): Builder {
            this.sinceId = sinceId
            return this@Builder
        }


        fun maxId(maxId: String?): Builder {
            this.maxId = maxId
            return this@Builder
        }

        fun count(count: Int?): Builder {
            this.count = count
            return this@Builder
        }

        fun page(page: Int?): Builder {
            this.page = page
            return this@Builder
        }

        fun mode(mode: String?): Builder {
            this.mode = mode
            return this@Builder
        }

        fun format(format: String?): Builder {
            this.format = format
            return this@Builder
        }

        fun id(id: String?): Builder {
            this.id = id
            return this@Builder
        }

        fun build(): TimelineParameters = TimelineParameters(this@Builder)
    }


}