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

package com.sinyuk.fanfou.domain

/**
 * Created by sinyuk on 2017/11/28.
 *
 */

/**
 * 显示指定用户及其好友的消息
 */
const val TIMELINE_HOME = "home_timeline"

/**
 * 显示指定用户及其好友的消息
 */
const val TIMELINE_FAVORITES = "favorites_timeline"

/**
 * 按照时间先后顺序显示消息上下文
 */
const val TIMELINE_CONTEXT = "context_timeline"

/**
 *
 */
const val TIMELINE_PHOTO = "photo_timeline"


const val TIMELINE_DRAFT = "draft_timeline"

/**
 * 浏览指定用户已发送消息
 */
const val TIMELINE_USER = "user_timeline"

/**
 * 回复当前用户的20条消息
 */
const val TIMELINE_REPLIES = "replies"

/**
 * 回复/提到当前用户的20条消息
 */
const val TIMELINE_MENTIONS = "mentions"


const val TIMELINE_PUBLIC = "public_timeline"

const val SEARCH_TIMELINE_PUBLIC = "search_public_timeline"

const val SEARCH_USERS = "search_users"

const val SEARCH_USER_TIMELINE = "search_user_timeline"

const val USERS_ADMIN = "byPath"

const val USERS_FRIENDS = "friends"

const val USERS_FOLLOWERS = "followers"

/**
 * @ format
 */
const val LINK_FORMAT_HTML = "html"

/**
 * profile.mode
 */

const val PROFILE_MODE_DEFAULT = "default"
const val PROFILE_MODE_LITE = "lite"

/**
 * page
 */
const val PAGE_SIZE = 10
const val PREFETCH_DISTANCE = 5
const val PHOTO_SIZE = 18

/**
 * message
 */
const val AUTHOR_FAILED_MSG = "获取第三方应用授权失败"
const val UNHANDLE_VISIBLE_ERROR_MESSAGE = "☍ (¦3ꇤ[▓▓] 我也不知道怎么了"


const val SUGGESTION_HISTORY_LIMIT = 5

const val STATUS_LIMIT = 140

object StatusCreation {
    const val CREATE_NEW = 0x00
    const val REPOST_STATUS = 0x11
    const val REPLY_TO_STATUS = 0x12
    const val REPLY_TO_USER = 0x13
}
