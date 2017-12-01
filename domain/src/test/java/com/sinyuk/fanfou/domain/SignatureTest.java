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

import com.sinyuk.fanfou.domain.utils.UrlEscapeUtils;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okio.ByteString;

import static org.junit.Assert.assertEquals;

/**
 * Created by sinyuk on 2017/12/1.
 */

public class SignatureTest {
    @Test
    public void signature_isCorrect() throws Exception {

        String expected = "r3GSsO00kPwqxigucnPA0N0QKmc%3D";
        String wrong = "XSfcgJF0XAgvU%2Ff%2F9dhKQXQiaRA%3D";
        Request request = new Request.Builder()
                .url("http://api.fanfou.com/statuses/home_timeline.json?count=60&format=html&id=~x9SCdqM0Kks")
                .build();

        String result = getSignature(request);
        System.out.println("Wrong  :" + wrong);
        System.out.println("Actual :" + result);
        assertEquals(expected, UrlEscapeUtils.escape(result));
    }

    public SortedMap<String, String> getParameters() {
        String oauthNonce = "vfrElWfY3OOUNg14uDLRbXTzIufAJ4KbAAAAAAAAAAA";

        String oauthTimestamp = "1512111487";

        String consumerKeyValue = UrlEscapeUtils.escape("ceab0dcd7b9fb9fa2ef5785bcd320e70");

        String accessTokenValue = UrlEscapeUtils.escape("1394833-dcb00385a3f550be20880ad428c7917d");

        SortedMap<String, String> parameters = new TreeMap<>();
        parameters.put("oauth_consumer_key", consumerKeyValue);
        parameters.put("oauth_token", accessTokenValue);
        parameters.put("oauth_nonce", oauthNonce);
        parameters.put("oauth_timestamp", oauthTimestamp);
        parameters.put("oauth_signature_method", "HMAC-SHA1");
        parameters.put("oauth_version", "1.0");
//        parameters.put("oauth_signature", getSignature(parameters, request));

        SortedMap<String, String> encoded = new TreeMap<>();
        for (String key : parameters.keySet()) {
            encoded.put(UrlEscapeUtils.escape(key), UrlEscapeUtils.escape(parameters.get(key)));
        }
        return encoded;
    }


    public String getSignature(final Request request) {
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String> params = new TreeMap<>();

        StringBuilder sb = new StringBuilder();
        HttpUrl httpUrl = request.url();

        final int querySize = request.url().querySize();

        for (int i = 0; i < querySize; i++) {
            queryParams.put(UrlEscapeUtils.escape(httpUrl.queryParameterName(i)), UrlEscapeUtils.escape(httpUrl.queryParameterValue(i)));
        }


        params.putAll(getParameters());
        params.putAll(queryParams);


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
        signingKey = "bc9d15a8458d863cc6524feb6d495f4b" + "&" + "6c561874817975548f4c352b78f29ded";
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
