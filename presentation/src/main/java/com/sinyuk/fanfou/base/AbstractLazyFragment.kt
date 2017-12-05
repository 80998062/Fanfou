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

package com.sinyuk.fanfou.base

import android.os.Bundle
import android.util.Log

/**
 * Created by sinyuk on 2017/11/30.
 */
abstract class AbstractLazyFragment : AbstractFragment() {

    /**
     * The Is view initiated.
     */
    private var isViewInitiated: Boolean = false
    /**
     * The Is visible to user.
     */
    private var isVisibleToUser: Boolean = false
    /**
     * The Is data initiated.
     */
    private var isDataInitiated: Boolean = false


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isViewInitiated = true
        prepareFetchData()

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(this::class.java.simpleName, "~UserVisibleHint: " + isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        prepareFetchData()
    }


    /**
     * Prepare fetch data boolean.
     *
     * @return the boolean
     */
    private fun prepareFetchData(): Boolean {
        return prepareFetchData(false)
    }

    /**
     * Prepare fetch data boolean.
     *
     * @param forceUpdate the force update
     * @return the boolean
     */
    private fun prepareFetchData(forceUpdate: Boolean): Boolean {
        if (isVisibleToUser && isViewInitiated && (!isDataInitiated || forceUpdate)) {
            Log.d(this::class.java.simpleName, "~prepareFetchData: ")
            lazyDo()
            isDataInitiated = true
            return true
        }
        return false
    }

    /**
     * Lazy do.
     */
    protected abstract fun lazyDo()

}