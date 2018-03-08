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

package com.sinyuk.fanfou.ui.search

import android.os.Bundle
import android.support.v4.app.SharedElementCallback
import android.transition.TransitionInflater
import android.view.View
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import kotlinx.android.synthetic.main.suggestion_view.*

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
class SuggestionView : AbstractFragment(), Injectable {


    override fun layoutId() = R.layout.suggestion_view


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.fade)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.fade)
        postponeEnterTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navBack.setOnClickListener { pop() }
        setupKeyboard()
        startPostponedEnterTransition()
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementEnd(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, sharedElementSnapshots: MutableList<View>?) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                if (findChildFragment(HistoryView::class.java) == null) {
                    childFragmentManager.beginTransaction()
                            .replace(R.id.historyViewContainer, HistoryView.newInstance(true))
                            .disallowAddToBackStack()
                            .commit()
                } else {
                    showHideFragment(findChildFragment(HistoryView::class.java))
                }
            }
        })
    }


    private fun setupKeyboard() {

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}