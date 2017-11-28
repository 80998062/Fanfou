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

/**
 * Created by sinyuk on 2017/3/15.
 */
object FanfouApi {

    /**
     * The constant ACCESS_TOKEN_URL.
     */
    @JvmStatic
    val ACCESS_TOKEN_URL = "http://fanfou.com/oauth/access_token"

    /**
     * The constant VERIFY_URL.
     */
    @JvmStatic
    val VERIFY_URL = "http://api.fanfou.com/account/verify_credentials.xml"

    /**
     * The constant X_AUTH_MODE.
     */
    @JvmStatic
    val X_AUTH_MODE = "client_auth"

    /**
     * The constant CONSUMER_KEY.
     */
    @JvmStatic
    val CONSUMER_KEY = "ceab0dcd7b9fb9fa2ef5785bcd320e70"
    /**
     * The constant CONSUMER_SECRET.
     */
    @JvmStatic
    val CONSUMER_SECRET = "bc9d15a8458d863cc6524feb6d495f4b"
    /**
     * The constant RANDOM_LENGTH.
     */
    @JvmStatic
    val RANDOM_LENGTH = 8
    /**
     * The constant SUCCEED_CODE.
     */
    @JvmStatic
    val SUCCEED_CODE = 200

    /**
     * The constant OAUTH_TOKEN_KEY.
     */
    @JvmStatic
    val OAUTH_TOKEN_KEY = "oauth_token"
    /**
     * The constant OAUTH_SECRET_KEY.
     */
    @JvmStatic
    val OAUTH_SECRET_KEY = "oauth_token_secret"


    /**
     * Created by sinyuk on 2017/3/17.
     */
    interface TimelinePath {
        companion object {
            /**
             * The constant HOME.
             */
            val HOME = "home_timeline"
            /**
             * The constant CONTEXT.
             */
            val CONTEXT = "context_timeline"
            /**
             * The constant PUBLIC.
             */
            val PUBLIC = "public_timeline"
            /**
             * The constant REPLY.
             */
            val REPLY = "replies"
            /**
             * The constant USER.
             */
            val USER = "user_timeline"
            /**
             * The constant MENTION.
             */
            val MENTION = "mentions"

            /**
             * The constant FAVOR.
             */
            val FAVOR = "favorite"
        }
    }


    /**
     * The interface Search type.
     */
    interface SearchPath {
        companion object {
            /**
             * The constant PUBLIC.
             */
            val PUBLIC = "public_timeline"
            /**
             * The constant USER.
             */
            val USER = "user_timeline"
        }
    }

    /**
     * The interface User path.
     */
    interface UserPath {
        companion object {
            /**
             * The constant TAGGED.
             */
            val TAGGED = "tagged"
            /**
             * The constant FOLLOWERS.
             */
            val FOLLOWERS = "followers"

            /**
             * The constant FRIENDS.
             */
            val FRIENDS = "friends"
        }

    }

    /**
     * The interface Id path.
     */
    interface IdPath {
        companion object {

            /**
             * The constant FOLLOWERS.
             */
            val FOLLOWERS = "followers"
            /**
             * The constant FOLLOWERS.
             */
            val FRIENDS = "friends"
        }
    }
}
