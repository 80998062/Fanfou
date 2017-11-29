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

package com.sinyuk.fanfou.domain.utils;


import com.sinyuk.fanfou.domain.rest.FanfouApi;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okio.Buffer;
import okio.ByteString;

/**
 * Created by sinyuk on 2017/3/16.
 */
public class XauthUtils {

    private static final String CONSUMER_KEY = "oauth_consumer_key";
    private static final String NONCE = "nonce";
    private static final String METHOD_GET = "GET";
    private static final String SIGNATURE = "signature";
    private static final String SIGNATURE_METHOD = "signature_method";
    private static final String SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String TIMESTAMP = "timestamp";
    private static final String VERSION = "version";
    private static final String VERSION_VALUE = "1.0";

    private static final String X_AUTH_USERNAME = "x_auth_username";
    private static final String X_AUTH_PASSWORD = "x_auth_password";
    private static final String X_AUTH_MODE = "x_auth_mode";


    private Random random;
    private String username;
    private String password;


    /**
     * New instance x auth factory.
     *
     * @param username the username
     * @param password the password
     * @return the x auth factory
     */
    public static XauthUtils newInstance(String username, String password) {
        return new XauthUtils(username, password);
    }

    private XauthUtils(String username, String password) {
        this.random = getRandom();
        this.username = username;
        this.password = password;
    }

    /**
     * Url string.
     *
     * @return the string
     */
    public HttpUrl url() {
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String oauthNonce = ByteString.of(nonce).base64().replaceAll("\\W", "");

        String oauthTimestamp = new Clock().millis();

        SortedMap<String, String> parameters = new TreeMap<>();

        parameters.put(CONSUMER_KEY, FanfouApi.getCONSUMER_KEY());

        // parameters for XAuth
        parameters.put(X_AUTH_USERNAME, username);
        parameters.put(X_AUTH_PASSWORD, password);
        parameters.put(X_AUTH_MODE, FanfouApi.getX_AUTH_MODE());

        parameters.put(NONCE, oauthNonce);
        parameters.put(TIMESTAMP, oauthTimestamp);
        parameters.put(SIGNATURE_METHOD, SIGNATURE_METHOD_VALUE);
        parameters.put(VERSION, VERSION_VALUE);

        boolean first = true;
        StringBuilder sb = new StringBuilder(FanfouApi.getACCESS_TOKEN_URL());

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (first) {
                sb.append("?");
                first = false;
            } else {
                sb.append("&");
            }

            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        return HttpUrl.parse(sb.toString());
    }

    /**
     * <p>
     * Expected basestring is
     * GET&http://fanfou.com/oauth/access_token
     * &oauth_consumer_key=ceab0dcd7b9fb9fa2ef5785bcd320e70
     * &oauth_nonce=6HPy4q
     * &oauth_signature_method=HMAC-SHA1
     * &oauth_timestamp=1489591205
     * &oauth_token=fa9d4a8ce79e5f8010cecd467e191754
     * &oauth_version=1.0
     * ...
     * </p>
     *
     * @param parameters the parameters
     * @return string string
     */
    public String signature(final SortedMap<String, String> parameters) {

        Buffer base = new Buffer();
        base.writeUtf8(METHOD_GET);
        base.writeByte('&');
        base.writeUtf8(
                UrlEscapeUtils.escape(FanfouApi.getACCESS_TOKEN_URL()));
        base.writeByte('&');

        boolean first = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) base.writeUtf8(UrlEscapeUtils.escape("&"));
            first = false;
            base.writeUtf8(UrlEscapeUtils.escape(entry.getKey()));
            base.writeUtf8(UrlEscapeUtils.escape("="));
            base.writeUtf8(UrlEscapeUtils.escape(entry.getValue()));
        }

        String signingKey =
                UrlEscapeUtils.escape(FanfouApi.getCONSUMER_KEY()) + "&" + UrlEscapeUtils.escape(FanfouApi.getCONSUMER_SECRET());

        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        byte[] result = mac.doFinal(base.readByteArray());

        return ByteString.of(result).base64();
    }

    private static Random getRandom() {
        return new Random() {
            @Override
            public void nextBytes(byte[] bytes) {
                if (bytes.length != 32) throw new AssertionError();
                ByteString hex = ByteString.decodeBase64(randomString(FanfouApi.getRANDOM_LENGTH()));
                byte[] nonce = hex.toByteArray();
                System.arraycopy(nonce, 0, bytes, 0, nonce.length);
            }
        };
    }

    private static String randomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(str.length() - 1);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    /**
     * Simple clock like class, to allow time mocking.
     */
    static class Clock {
        /**
         * Returns the current time in milliseconds divided by 1K.
         *
         * @return the string
         */
        String millis() {
            return Long.toString(System.currentTimeMillis() / 1000L);
        }
    }
}
