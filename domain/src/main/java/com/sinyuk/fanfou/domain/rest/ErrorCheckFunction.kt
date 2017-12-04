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

package com.sinyuk.fanfou.domain.rest

import com.google.gson.Gson
import io.reactivex.functions.Function
import io.reactivex.exceptions.Exceptions
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException


/**
 * Created by sinyuk on 2017/11/27.
 */
class ErrorCheckFunction<R>(private val gson: Gson) : Function<Response<R>, R> {

    private val DEFAULT_ERROR_MESSAGE = "Opps!"

    @Suppress("UNCHECKED_CAST")
    override fun apply(t: Response<R>): R {
        var msg: String? = null
        if (t.isSuccessful) {
            if (t.body() != null) {
                return t.body() as R
            } else {
                msg = DEFAULT_ERROR_MESSAGE
            }
        } else {
            try {
                val errorResponse: Error? = gson.fromJson(t.errorBody().toString(), Error::class.java)
                msg = errorResponse?.error
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            if (msg == null) {
                when (t.code()) {
                    400 -> msg = "请求错误"
                    401 -> msg = "请登录或者重新登录"
                    403 -> msg = "操作被服务器拒绝"
                    404 -> msg = "你好像来错了地方"
                    405 -> msg = "操作被禁止"
                    406 -> msg = "无法使用请求的内容特性响应请求的网页"
                    407 -> msg = "需要代理授权"
                    408 -> msg = "请求超时"
                    409 -> msg = "服务器在完成请求时发生冲突"
                    410 -> msg = "请求的资源已永久删除"
                    411 -> msg = "请求不含有效内容长度标头字段"
                    412 -> msg = "服务器不能满足你的要求"
                    413 -> msg = "我们太穷了,买不起更好的服务器"
                    414 -> msg = "请求的 URI（通常为网址）过长"
                    415 -> msg = "页面无法提供请求的范围"
                    416 -> msg = "服务器不能满足你的要求"
                    501, 502, 503, 504, 505 -> msg = "服务器大姨妈了"
                    else -> msg = DEFAULT_ERROR_MESSAGE
                }
            }
        }

        try {
            throw IOException(msg)
        } catch (e: IOException) {
            throw Exceptions.propagate(e)
        }
    }
}