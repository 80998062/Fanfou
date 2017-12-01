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


import android.text.TextUtils
import android.util.Log
import com.sinyuk.fanfou.domain.utils.UrlEscapeUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * The type Oauth 1 signing interceptor.
 */
class Oauth1SigningInterceptor(authorization: Authorization?) : Interceptor {
    private var accessToken: String? = authorization?.secret
    private var accessSecret: String? = authorization?.token
    private val consumerKey = FanfouApi.CONSUMER_KEY
    private val consumerSecret = FanfouApi.CONSUMER_SECRET
    private val random = generateRandom()
    private val clock = Clock()


    fun authenticator(authorization: Authorization?) {
        this.accessSecret = authorization?.secret
        this.accessToken = authorization?.token
    }


    private fun generateRandom(): Random {
        return object : Random() {
            override fun nextBytes(bytes: ByteArray) {
                if (bytes.size != 32) throw AssertionError()
                val hex = ByteString.decodeBase64(randomString(RANDOM_LENGTH))
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

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        val request: Request
        request = if (TextUtils.isEmpty(accessSecret) || TextUtils.isEmpty(accessToken)) {
            builder.removeHeader("Authorization").build()
        } else {
            builder.header("Authorization", getAuthorization(originalRequest)).build()
        }
        return chain.proceed(request)
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
        internal fun millis(): String = (System.currentTimeMillis() / 1000).toString()

    }


    /**
     * Gets authorization.
     *
     * @param request the request
     * @return the authorization
     */
    private fun getAuthorization(request: Request): String {
        val nonce = ByteArray(32)
        random.nextBytes(nonce)
        val oauthNonce = ByteString.of(*nonce).base64().replace("\\W".toRegex(), "")

        val oauthTimestamp = clock.millis()

        val consumerKeyValue = UrlEscapeUtils.escape(consumerKey)

        var accessTokenValue: String? = null
        if (accessToken != null) {
            accessTokenValue = UrlEscapeUtils.escape(accessToken)
        }

        val parameters = TreeMap<String, String>()
        parameters.put(OAUTH_CONSUMER_KEY, consumerKeyValue)
        parameters.put(OAUTH_ACCESS_TOKEN, accessTokenValue!!)
        parameters.put(OAUTH_NONCE, oauthNonce)
        parameters.put(OAUTH_TIMESTAMP, oauthTimestamp)
        parameters.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
        parameters.put(OAUTH_VERSION, OAUTH_VERSION_VALUE)
        parameters.put(OAUTH_SIGNATURE, getSignature(parameters, request))

        val entrySet = parameters.entries

        val sb = StringBuilder()
        sb.append(OAUTH_TYPE)
        for ((key, value) in entrySet) {
            if (sb.length > OAUTH_TYPE.length) {
                sb.append(", ")
            }
            sb.append(String.format("%s=\"%s\"", key,
                    UrlEscapeUtils.escape(value)))
        }
        Log.d(TAG, "Authorization: " + sb.toString())
        return sb.toString()
    }

    /**
     * Gets signature.
     *
     * @param parameters the parameters
     * @param request    the request
     * @return the signature
     */
    private fun getSignature(
            parameters: SortedMap<String, String>,
            request: Request): String {
        val queryParams = HashMap<String, String>()
        val params = TreeMap<String, String>()

        val sb = StringBuilder()
        val httpUrl = request.url()

        val querySize = request.url().querySize()

        for (i in 0 until querySize) {
            Log.d("参数们: ", httpUrl.queryParameterName(i) + ": " + httpUrl.queryParameterValue(i))
            queryParams.put(httpUrl.queryParameterName(i), httpUrl.queryParameterValue(i))
        }
        params.putAll(parameters)
        params.putAll(queryParams)

        val entrySet = params.entries

        for ((key, value) in entrySet) {
            sb.append("&")
            sb.append(String.format("%s=%s", key, value))
        }
        val queryItems = sb.toString().substring(1)
        val baseString = String.format("%s&%s&%s", request.method(),
                UrlEscapeUtils.escape("http://" + httpUrl.url().host + httpUrl.url().path),
                UrlEscapeUtils.escape(queryItems))
        Log.d(TAG, "BaseString: " + baseString)
        return doSign(baseString)
    }

    /**
     * Do sign string.
     *
     * @param baseString the base string
     * @return the string
     */
    private fun doSign(baseString: String): String {
        val signingKey = UrlEscapeUtils.escape(consumerSecret) + "&" + UrlEscapeUtils.escape(accessSecret)
        val keySpec = SecretKeySpec(signingKey.toByteArray(), "HmacSHA1")
        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA1")
            mac.init(keySpec)
            val result = mac.doFinal(baseString.toByteArray(charset(CHARSET)))
            return ByteString.of(*result).base64()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw IllegalStateException(e)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
            throw IllegalStateException(e)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            throw IllegalStateException(e)
        }

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
    private val RANDOM_LENGTH = 32

}