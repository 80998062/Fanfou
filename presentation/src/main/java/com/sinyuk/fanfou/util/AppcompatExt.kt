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

package com.sinyuk.fanfou.util

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.sinyuk.fanfou.viewmodel.ViewModelFactory

/**
 * Created by sinyuk on 2017/11/28.
 */
fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, frameId: Int) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
}


fun AppCompatActivity.addFragmentInActivity(fragment: Fragment, resId: Int, addToBackStack: Boolean) {
    val tag = fragment::class.java.simpleName
    if (supportFragmentManager.findFragmentByTag(tag) == null) {
        val ft = supportFragmentManager
                .beginTransaction()
                .replace(resId, fragment, tag)
        if (addToBackStack) {
            ft.addToBackStack(tag)
        }
        ft.commit()
    }
}

fun <T : ViewModel> AppCompatActivity.obtainViewModel(viewModelFactory: ViewModelFactory, viewModelClass: Class<T>) =
        ViewModelProviders.of(this, viewModelFactory).get(viewModelClass)

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}


fun Fragment.addFragmentInFragment(fragment: Fragment, resId: Int, addToBackStack: Boolean) {
    val tag = fragment.javaClass.simpleName
    if (childFragmentManager.findFragmentByTag(tag) == null) {
        val ft = childFragmentManager.beginTransaction().replace(resId, fragment, tag)
        if (addToBackStack) {
            ft.addToBackStack(tag)
        }
        ft.commit()
    }
}

fun <T : ViewModel> Fragment.obtainViewModel(viewModelFactory: ViewModelFactory, viewModelClass: Class<T>) =
        ViewModelProviders.of(this.activity as AppCompatActivity, viewModelFactory).get(viewModelClass)
