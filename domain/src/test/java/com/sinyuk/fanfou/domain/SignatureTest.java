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

package com.sinyuk.fanfou.domain;

import com.sinyuk.fanfou.domain.rest.Oauth1SigningInterceptor;
import com.sinyuk.fanfou.domain.utils.UrlEscapeUtils;

import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.ByteString;

/**
 * Created by sinyuk on 2017/12/1.
 */

public class SignatureTest {
    @Test
    public void signature_isCorrect() throws Exception {

        String expected = "hCtSmYh+iHYCEqBWrE7C7hYmtUk=";
        Request request = new Request.Builder()
                .url("http://api.fanfou.com/statuses/home_timeline.json")
                .build();


        System.out.println(signRequest(request));
    }

    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_ACCESS_TOKEN = "oauth_token";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_VERSION_VALUE = "1.0";

    private final String consumerKey = "ceab0dcd7b9fb9fa2ef5785bcd320e70";
    private final String consumerSecret = "bc9d15a8458d863cc6524feb6d495f4b";
    private final String accessToken = "1394833-dcb00385a3f550be20880ad428c7917d";
    private final String accessSecret = "6c561874817975548f4c352b78f29ded";
    private final Random random = new SecureRandom();
    private final Oauth1SigningInterceptor.Clock clock = new Oauth1SigningInterceptor.Clock();

    public String signRequest(Request request) throws IOException {
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String oauthNonce = ByteString.of(nonce).base64().replaceAll("\\W", "");
        String oauthTimestamp = clock.millis();

        String consumerKeyValue = UrlEscapeUtils.escape(consumerKey);
        String accessTokenValue = UrlEscapeUtils.escape(accessToken);

        SortedMap<String, String> parameters = new TreeMap<>();
        parameters.put(OAUTH_CONSUMER_KEY, consumerKeyValue);
        parameters.put(OAUTH_ACCESS_TOKEN, accessTokenValue);
        parameters.put(OAUTH_NONCE, oauthNonce);
        parameters.put(OAUTH_TIMESTAMP, oauthTimestamp);
        parameters.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE);
        parameters.put(OAUTH_VERSION, OAUTH_VERSION_VALUE);

        HttpUrl url = request.url();
        for (int i = 0; i < url.querySize(); i++) {
            parameters.put(UrlEscapeUtils.escape(url.queryParameterName(i)),
                    UrlEscapeUtils.escape(url.queryParameterValue(i)));
        }

        Buffer body = new Buffer();

        RequestBody requestBody = request.body();
        if (requestBody != null) {
            requestBody.writeTo(body);
        }

        while (!body.exhausted()) {
            long keyEnd = body.indexOf((byte) '=');
            if (keyEnd == -1)
                throw new IllegalStateException("Key with no value: " + body.readUtf8());
            String key = body.readUtf8(keyEnd);
            body.skip(1); // Equals.

            long valueEnd = body.indexOf((byte) '&');
            String value = valueEnd == -1 ? body.readUtf8() : body.readUtf8(valueEnd);
            if (valueEnd != -1) body.skip(1); // Ampersand.

            parameters.put(key, value);
        }

        Buffer base = new Buffer();
        String method = request.method();
        base.writeUtf8(method);
        base.writeByte('&');
        base.writeUtf8(
                UrlEscapeUtils.escape(request.url().newBuilder().query(null).build().toString()));
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
                UrlEscapeUtils.escape(consumerSecret) + "&" + UrlEscapeUtils.escape(accessSecret);

        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        byte[] result = mac.doFinal(base.readByteArray());
        String signature = ByteString.of(result).base64();

        String authorization =
                "OAuth " + OAUTH_CONSUMER_KEY + "=\"" + consumerKeyValue + "\", " + OAUTH_NONCE + "=\""
                        + oauthNonce + "\", " + OAUTH_SIGNATURE + "=\"" + UrlEscapeUtils.escape(signature)
                        + "\", " + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", "
                        + OAUTH_TIMESTAMP + "=\"" + oauthTimestamp + "\", " + OAUTH_ACCESS_TOKEN + "=\""
                        + accessTokenValue + "\", " + OAUTH_VERSION + "=\"" + OAUTH_VERSION_VALUE + "\"";

        return authorization;
    }

    public String getSignature(final Request request) {
        Map<String, String> params = new TreeMap<>();

        StringBuilder sb = new StringBuilder();

        HttpUrl httpUrl = request.url();


//        SortedSet<String> keys = new TreeSet<>(params.keySet());

        ArrayList sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        for (int i = 0; i < sortedKeys.size(); i++) {
            String key = (String) sortedKeys.get(i);
            sb.append("&");
            sb.append(String.format("%s=%s", key, params.get(key)));
        }

        String queryItems = sb.toString().substring(1);
        System.out.println("Params: " + queryItems);
        String baseString = String.format("%s&%s&%s", request.method(),
                UrlEscapeUtils.escape("http://" + httpUrl.url().getHost() + httpUrl.url().getPath()),
                queryItems);
        System.out.println("base_string  :" + baseString);
        return doSign(baseString);
    }

    public String doSign(final String baseString) {
        String signingKey;
        signingKey = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";
        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] result = mac.doFinal(baseString.getBytes("UTF-8"));
            return ByteString.of(result).base64();
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }


}
