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

import android.arch.lifecycle.Observer
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractLazyFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.TIMELINE_USER
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout.PASS_MODE_PARENT_FIRST
import com.sinyuk.fanfou.ui.timeline.TimelineView
import com.sinyuk.fanfou.util.FanfouFormatter
import com.sinyuk.fanfou.util.addFragmentInFragment
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.SearchViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.public_view.*
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class SearchView : AbstractLazyFragment(), Injectable {
    override fun layoutId(): Int? = R.layout.public_view

    @Inject lateinit var factory: FanfouViewModelFactory

    @Inject lateinit var toast: ToastUtils


    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    private val searchViewModel by lazy { obtainViewModelFromActivity(factory, SearchViewModel::class.java) }


    override fun lazyDo() {
        rootView.setPassMode(PASS_MODE_PARENT_FIRST)

        setupAdView()

        setupTrendList()

        val publicTimeline = TimelineView.newInstance(TIMELINE_USER)
        addFragmentInFragment(publicTimeline, R.id.fragment_container, false)

        searchViewModel.trends().asLiveData().observe(this@SearchView, Observer {
            when (it?.states) {
                States.SUCCESS -> {
                    adapter.setNewData(it.data)
                }
                States.ERROR -> {
                    it.message?.let { toast.toastShort(it) }
                }
                States.LOADING -> {

                }
            }

            publicTimeline.userVisibleHint = true
        })
    }


    private lateinit var adapter: TrendAdapter
    private lateinit var header: View

    private fun setupTrendList() {
        object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }.apply {
            isAutoMeasureEnabled = true
            trendList.layoutManager = this
        }

        trendList.setHasFixedSize(true)

        header = LayoutInflater.from(context).inflate(R.layout.trend_list_header, trendList, false)

        adapter = TrendAdapter().apply {
            addHeaderView(header)
            setOnItemClickListener { _, _, position ->
                toast.toastShort(getItem(position)?.name ?: "Hello")
            }
            trendList.adapter = this
        }
    }

    private fun setupAdView() {
        MobileAds.initialize(context, "ca-app-pub-7188837029502497~9103442019")
        //adView.adSize = AdSize.SMART_BANNER
        //adView.adUnitId = "ca-app-pub-7188837029502497/7810541646"
        adView.adListener = object : AdListener() {

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                adView.visibility = View.GONE
            }

            override fun onAdClosed() {
                super.onAdClosed()
                adView.visibility = View.GONE
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                adView.visibility = View.VISIBLE
            }
        }

        accountViewModel.user.observe(this@SearchView, Observer {
            accountViewModel.user.removeObservers(this@SearchView)
            buildAdRequest(it?.data)
        })
    }

    private fun buildAdRequest(data: Player?) {
        val builder = AdRequest.Builder()
        if (data != null) {
            data.birthday?.let { builder.setBirthday(FanfouFormatter.convertBirthdayStrToDate(it)) }
            data.screenName?.let { builder.addKeyword(it) }
            builder.setGender(FanfouFormatter.convertGenderToInt(data.gender))
        }

        adView.loadAd(builder.build())
    }


}