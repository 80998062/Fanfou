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

package com.sinyuk.fanfou.domain.util


import com.sinyuk.fanfou.domain.BuildConfig
import okhttp3.HttpUrl
import okio.ByteString
import java.util.*

/**
 * Created by sinyuk on 2017/3/16.
 */
class XauthUtils private constructor(private val username: String, private val password: String) {


    private val random = getRandom()


    /**
     * Url string.
     *
     * @return the string
     */
    fun url(): HttpUrl {
        val nonce = ByteArray(32)
        random.nextBytes(nonce)
        val oauthNonce = ByteString.of(*nonce).base64().replace("\\W".toRegex(), "")

        val oauthTimestamp = Clock().millis()

        val parameters = TreeMap<String, String>()

        parameters.put(CONSUMER_KEY, BuildConfig.CONSUMER_KEY)

        // parameters for XAuth
        parameters.put(X_AUTH_USERNAME, username)
        parameters.put(X_AUTH_PASSWORD, password)
        parameters.put(X_AUTH_MODE, BuildConfig.X_AUTH_MODE)

        parameters.put(NONCE, oauthNonce)
        parameters.put(TIMESTAMP, oauthTimestamp)
        parameters.put(SIGNATURE_METHOD, SIGNATURE_METHOD_VALUE)
        parameters.put(VERSION, VERSION_VALUE)

        var first = true
        val sb = StringBuilder("http://fanfou.com/oauth/access_token")

        for ((key, value) in parameters) {
            if (first) {
                sb.append("?")
                first = false
            } else {
                sb.append("&")
            }

            sb.append(key)
            sb.append("=")
            sb.append(value)
        }

        return HttpUrl.parse(sb.toString())!!
    }

    /**
     * Simple clock like class, to allow time mocking.
     */
    class Clock {
        /**
         * Returns the current time in milliseconds divided by 1K.
         *
         * @return the string
         */
        fun millis(): String {
            return java.lang.Long.toString(System.currentTimeMillis() / 1000L)
        }
    }

    companion object {

        private val CONSUMER_KEY = "oauth_consumer_key"
        private val NONCE = "nonce"
        private val METHOD_GET = "GET"
        private val SIGNATURE = "signature"
        private val SIGNATURE_METHOD = "signature_method"
        private val SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
        private val TIMESTAMP = "timestamp"
        private val VERSION = "version"
        private val VERSION_VALUE = "1.0"
        private val X_AUTH_USERNAME = "x_auth_username"
        private val X_AUTH_PASSWORD = "x_auth_password"
        private val X_AUTH_MODE = "x_auth_mode"


        /**
         * New instance x auth factory.
         *
         * @param username the username
         * @param password the password
         * @return the x auth factory
         */
        fun newInstance(username: String, password: String): XauthUtils {
            return XauthUtils(username, password)
        }

        private fun getRandom(): Random {
            return object : Random() {
                override fun nextBytes(bytes: ByteArray) {
                    if (bytes.size != 32) throw AssertionError()
                    val hex = ByteString.decodeBase64(randomString(8))
                    val nonce = hex!!.toByteArray()
                    System.arraycopy(nonce, 0, bytes, 0, nonce.size)
                }
            }
        }

        private fun randomString(length: Int): String {
            val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val random = Random()
            val buf = StringBuilder()
            for (i in 0 until length) {
                val num = random.nextInt(str.length - 1)
                buf.append(str[num])
            }
            return buf.toString()
        }
    }
}
