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

package com.sinyuk.fanfou.ui.colormatchtabs.utils

import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.view.View

/**
 * Created by anna on 15.05.17.
 *
 */
fun View.getDimenToFloat(@DimenRes res: Int) = context.resources.getDimensionPixelOffset(res).toFloat()

fun View.getDimen(@DimenRes res: Int) = context.resources.getDimensionPixelOffset(res)

fun View.getColor(@ColorRes res: Int) = ContextCompat.getColor(context, res)