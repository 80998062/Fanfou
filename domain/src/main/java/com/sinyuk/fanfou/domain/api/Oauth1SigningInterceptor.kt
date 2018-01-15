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


import android.content.SharedPreferences
import android.util.Log
import com.sinyuk.fanfou.domain.ACCESS_SECRET
import com.sinyuk.fanfou.domain.ACCESS_TOKEN
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.util.UrlEscapeUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.ByteString
import java.io.IOException
import java.net.URLDecoder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Named
import javax.inject.Singleton


/**
 * The type Oauth 1 signing interceptor.
 */
@Singleton
class Oauth1SigningInterceptor(@Named(TYPE_GLOBAL) private val p: SharedPreferences) : Interceptor {
    private val consumerKey = BuildConfig.CONSUMER_KEY
    private val consumerSecret = BuildConfig.CONSUMER_SECRET
    private val random = SecureRandom()
    private val clock = Clock()


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = p.getString(ACCESS_TOKEN, null)
        val secret = p.getString(ACCESS_SECRET, null)

        return if (token == null || secret == null) {
            chain.proceed(chain.request().newBuilder().removeHeader("Authorization").build())
        } else {
            chain.proceed(signRequest(chain.request(), token, secret))
        }
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
        fun millis(): String = (System.currentTimeMillis() / 1000).toString()

    }


    /**
     * Gets authorization.
     *
     * @param request the request
     * @return the authorization
     */

    private fun signRequest(request: Request, accessToken: String?, accessSecret: String?): Request {

        val nonce = ByteArray(32)
        random.nextBytes(nonce)
        val oauthNonce = ByteString.of(*nonce).base64().replace("\\W".toRegex(), "")
        val oauthTimestamp = clock.millis()

        val parameters = TreeMap<String, String>()

        val consumerKeyValue = UrlEscapeUtils.escape(consumerKey)
        val accessTokenValue = UrlEscapeUtils.escape(accessToken)

        parameters.put(OAUTH_CONSUMER_KEY, consumerKeyValue)
        parameters.put(OAUTH_ACCESS_TOKEN, accessTokenValue)
        parameters.put(OAUTH_NONCE, oauthNonce)
        parameters.put(OAUTH_TIMESTAMP, oauthTimestamp)
        parameters.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
        parameters.put(OAUTH_VERSION, OAUTH_VERSION_VALUE)


        val httpUrl = request.url()
        val contentType = request.header("Content-Type")
        if (request.method() == "GET" || (request.method() == "POST")
                && contentType == "application/x-www-form-urlencoded") {
            val querySize = request.url().querySize()
            for (i in 0 until querySize) {
                parameters.put(httpUrl.queryParameterName(i), httpUrl.queryParameterValue(i))
            }
        }


        // no need for request body
        val base = Buffer()
        val method = request.method()
        base.writeUtf8(method)
        base.writeByte('&'.toInt())
        base.writeUtf8(UrlEscapeUtils.escape(request.url().newBuilder().query(null).build().toString()))
        base.writeByte('&'.toInt())


        var first = true
        for (entry in parameters.entries) {
            if (!first) base.writeUtf8(UrlEscapeUtils.escape("&"))
            first = false
            base.writeUtf8(UrlEscapeUtils.escape(entry.key))
            base.writeUtf8(UrlEscapeUtils.escape("="))
            if (entry.key == "q") {
                base.writeUtf8(UrlEscapeUtils.escape(URLDecoder.decode(entry.value, "utf-8")))
                Log.d("Interceptor", "q: " + entry.value)
                Log.d("Interceptor", "q: " + UrlEscapeUtils.escape(entry.value))
            } else {
                base.writeUtf8(UrlEscapeUtils.escape(entry.value))
            }
        }

        val signingKey = UrlEscapeUtils.escape(consumerSecret) + "&" + UrlEscapeUtils.escape(accessSecret)

        val keySpec = SecretKeySpec(signingKey.toByteArray(), "HmacSHA1")
        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA1")
            mac.init(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        } catch (e: InvalidKeyException) {
            throw IllegalStateException(e)
        }

        val result = mac.doFinal(base.readByteArray())
        val signature = ByteString.of(*result).base64()

        val authorization = ("OAuth " + OAUTH_CONSUMER_KEY + "=\"" + consumerKeyValue + "\", " + OAUTH_NONCE + "=\""
                + oauthNonce + "\", " + OAUTH_SIGNATURE + "=\"" + UrlEscapeUtils.escape(signature)
                + "\", " + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
                + OAUTH_TIMESTAMP + "=\"" + oauthTimestamp + "\", " + OAUTH_ACCESS_TOKEN + "=\""
                + accessTokenValue + "\", " + OAUTH_VERSION + "=\"" + OAUTH_VERSION_VALUE + "\"")

        return request.newBuilder().addHeader("Authorization", authorization).build()
    }


    private val TAG = "Interceptor"
    private val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
    private val OAUTH_NONCE = "oauth_nonce"
    private val OAUTH_SIGNATURE = "oauth_signature"
    private val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
    private val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
    private val OAUTH_TIMESTAMP = "oauth_timestamp"
    private val OAUTH_ACCESS_TOKEN = "oauth_token"
    private val OAUTH_VERSION = "oauth_version"
    private val OAUTH_VERSION_VALUE = "1.0"
    private val OAUTH_TYPE = "OAuth "
    private val CHARSET = "UTF-8"
}