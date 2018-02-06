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

package com.sinyuk.fanfou.customtabs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * A BroadcastReceiver that handles the Action Intent from the Custom Tab and shows the Url
 * in a Toast.
 */
class ActionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val url = intent.dataString
        if (url != null) {
            val toastText = getToastText(context, intent.getIntExtra(KEY_ACTION_SOURCE, -1), url)
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getToastText(context: Context, actionId: Int, url: String): String {
        return when (actionId) {
            ACTION_ACTION_BUTTON -> "action button "
            ACTION_MENU_ITEM -> "menu item"
            ACTION_TOOLBAR -> "toolbar"
            else -> "uk"
        }
    }

    companion object {
        const val KEY_ACTION_SOURCE = "org.chromium.customtabsdemos.ACTION_SOURCE"
        const val ACTION_ACTION_BUTTON = 1
        const val ACTION_MENU_ITEM = 2
        const val ACTION_TOOLBAR = 3
    }
}