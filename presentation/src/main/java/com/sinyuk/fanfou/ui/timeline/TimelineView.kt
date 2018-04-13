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

package com.sinyuk.fanfou.ui.timeline

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.refresh.RefreshCallback
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.ConnectionModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by sinyuk on 2017/11/30.
 *
 */
class TimelineView : AbstractFragment(), Injectable, StatusPagedListAdapter.StatusOperationListener {


    companion object {
        fun newInstance(path: String, id: String) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("id", id)
            }
        }

        const val TAG = "TimelineView"
    }

    override fun layoutId(): Int? = R.layout.timeline_view

    @Suppress("MemberVisibilityCanBePrivate")
    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    @Inject
    lateinit var toast: ToastUtils


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        setupSwipeRefresh()
        setupRecyclerView()

        // Cannot add the same observer with different lifeCycles! So use activity here.
        activity?.let {
            ConnectionModel.livedata(it.applicationContext).observe(activity!!, Observer {
                Log.d(TAG, it.toString())
            })
        }

        assert(arguments != null)
        arguments!!.apply { timelineViewModel.setRelativeUrl(getString("path"), getString("id"), getString("query")) }
        adapter.submitList(null)
        recyclerView.scrollToPosition(0)
    }


    var refreshCallback: RefreshCallback? = null

    private fun setupSwipeRefresh() {
        timelineViewModel.refreshState.observe(this@TimelineView, Observer {
            val refresh = it?.states == States.LOADING
            refreshCallback?.toggle(refresh)
            if (States.ERROR == it?.states) {
                it.message?.let { refreshCallback?.error(Throwable(it)) }
            } else if (States.SUCCESS == it?.states) {
                EventBus.getDefault().post(FetTopEvent(message = getString(R.string.hint_new_statuses_coming), type = TYPE.TOAST))
//                if ((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() > 0) { // 列表不是在顶部
//                    EventBus.getDefault().post(FetTopEvent(message = getString(R.string.hint_new_statuses_coming), type = TYPE.TOAST))
//                } else {
//                    EventBus.getDefault().post(FetTopEvent(message = getString(R.string.format_new_statuses_coming, it.data?.size)))
//                }
            }
        })
    }

    fun search(q: String) {

    }

    fun refresh() {
        if (BuildConfig.DEBUG) Log.d(TAG, "trigger refresh fromUser")
        timelineViewModel.refresh()
    }


    private lateinit var adapter: StatusPagedListAdapter

    @Suppress("MemberVisibilityCanBePrivate")
    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var sharedPreferences: SharedPreferences

    private fun setupRecyclerView() {
        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        adapter = StatusPagedListAdapter(this@TimelineView, { timelineViewModel.retry() }, sharedPreferences.getString(UNIQUE_ID, ""))

        val imageWidthPixels = resources.getDimensionPixelSize(R.dimen.timeline_illustration_size)
        val modelPreloader = StatusPagedListAdapter.StatusPreloadProvider(adapter, this, imageWidthPixels)
        val sizePreloader = FixedPreloadSizeProvider<Status>(imageWidthPixels, imageWidthPixels)
        val preloader = RecyclerViewPreloader<Status>(Glide.with(this@TimelineView), modelPreloader, sizePreloader, PAGE_SIZE)
        recyclerView.addOnScrollListener(preloader)
        adapter.statusOperationListener = this@TimelineView
        recyclerView.adapter = adapter

        timelineViewModel.statuses.observe(this, pagedListConsumer)
        timelineViewModel.networkState.observe(this, networkConsumer)
    }

    override fun onFavorited(favorited: Boolean, v: View?, p: Int, status: Status) {
        if (favorited) {
            timelineViewModel.createFavorite(status.id).observe(this@TimelineView, Observer {
                if (it?.states == States.ERROR) {
                    it.message?.let { toast.toastShort(it) }
                }
            })
        } else {
            timelineViewModel.destroyFavorite(status.id).observe(this@TimelineView, Observer {

            })
        }
    }


    override fun onDeleted(v: View?, p: Int, status: Status) {
        timelineViewModel.delete(status.id).observe(this@TimelineView, Observer {

        })
    }


    private val pagedListConsumer = Observer<PagedList<Status>> {
        // Preserves the user's scroll position if items are inserted outside the viewable area:
//        val recyclerViewState = recyclerView.layoutManager.onSaveInstanceState()
        adapter.submitList(it)
//        recyclerView.post { recyclerView.layoutManager.onRestoreInstanceState(recyclerViewState) }
    }

    private val networkConsumer = Observer<NetworkState> {
        adapter.setNetworkState(it)
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onScrollEvent(event: ScrollToTopEvent) {
        recyclerView?.smoothScrollToPosition(0)
    }


}