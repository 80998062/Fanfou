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
import android.support.v7.widget.RecyclerView
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.glide.GlideApp
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.ui.refresh.RefreshCallback
import com.sinyuk.fanfou.util.linkfy.LinkTouchMovementMethod
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.span.AndroidSpan
import com.sinyuk.fanfou.util.span.SpanOptions
import com.sinyuk.fanfou.viewmodel.ConnectionModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.state_layout_empty.view.*
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
        fun newInstance(path: String) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
            }
        }

        fun contextTimeline(path: String, status: String) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putString("status", status)
            }
        }

        fun playerTimeline(path: String, player: Player) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putParcelable("player", player)
            }
        }

        const val TAG = "TimelineView"
    }

    override fun layoutId(): Int? = R.layout.timeline_view

    @Suppress("MemberVisibilityCanBePrivate")
    @Inject
    lateinit var factory: FanfouViewModelFactory

    private val timelineViewModel by lazy { obtainViewModel(factory, TimelineViewModel::class.java) }

//    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    @Inject
    lateinit var toast: ToastUtils


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        // Cannot add the same observer with different lifeCycles! So use activity here.
        activity?.let {
            ConnectionModel.livedata(it.applicationContext).observe(activity!!, Observer {
                Log.d(TAG, it.toString())
            })
        }

        configTimeline()
    }

    /**
     * 决定显示什么接口的列表
     */
    private fun configTimeline() {
        var delayLoading = false // 如果是加锁了 不用加载列表
        val validId: String
        val path: String
        assert(arguments != null)

        if (TIMELINE_PUBLIC == arguments!!.getString("path")) {
            path = TIMELINE_PUBLIC
            validId = sharedPreferences.getString(UNIQUE_ID, "")
        } else if (arguments!!.containsKey("player")) {
            val player = arguments!!.getParcelable<Player>("player")
            path = arguments!!.getString("path")!!
            validId = player.uniqueId
            if (player.protectedX == true && player.uniqueId != sharedPreferences.getString(UNIQUE_ID, "")) { // 用户加锁了且不是本人
                delayLoading = true
                showPrivacyView(player)
            }
        } else {
            path = arguments!!.getString("path")!!
            validId = arguments!!.getString("status")!!
        }

        if (!delayLoading) {
            setupSwipeRefresh()
            setupRecyclerView()
            timelineViewModel.setRelativeUrl(path, validId, null)
        }
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

    private fun setupRecyclerView() {
//        adapter.submitList(null)
//        recyclerView.scrollToPosition(0)

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
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter.itemCount == 0) {
                    showEmptyView()
                } else {
                    viewAnimator.displayedChildId = R.id.recyclerView
                }
            }
        })
        recyclerView.adapter = adapter

        timelineViewModel.statuses.observe(this, pagedListConsumer)
        timelineViewModel.networkState.observe(this, networkConsumer)
    }

    /**
     * 显示加锁视图
     */
    private fun showPrivacyView(player: Player) {
        viewAnimator.displayedChildId = R.id.layoutPrivacy
        if (layoutPrivacy.title.text == null) {
            layoutPrivacy.title.text = getString(R.string.state_title_privacy)
            val span = AndroidSpan().drawRelativeSizeSpan(getString(R.string.state_description_privacy), 1f)
                    .drawWithOptions("Follow ${player.gender}", SpanOptions().addTextAppearanceSpan(context!!, R.style.text_bold_primary).addSpan(object : ClickableSpan() {
                        override fun onClick(v: View?) {

                        }
                    })).spanText
            layoutPrivacy.description.movementMethod = LinkTouchMovementMethod.getInstance()
            layoutPrivacy.description.text = span
            GlideApp.with(this).load(R.drawable.state_layout_forbidden).transition(withCrossFade()).into(layoutPrivacy.image)
        }
    }

    /**
     * 显示空视图
     */
    private fun showEmptyView() {
        viewAnimator.displayedChildId = R.id.layoutEmpty
        if (layoutEmpty.title.text == null) {
            layoutEmpty.title.text = getString(R.string.state_title_empty)
            val span = AndroidSpan().drawRelativeSizeSpan(getString(R.string.state_description_empty), 1f)
                    .drawWithOptions("Refresh?", SpanOptions().addTextAppearanceSpan(context!!, R.style.text_bold_primary).addSpan(object : ClickableSpan() {
                        override fun onClick(v: View?) {

                        }
                    })).spanText
            layoutEmpty.description.movementMethod = LinkTouchMovementMethod.getInstance()
            layoutEmpty.description.text = span
            GlideApp.with(this).load(R.drawable.state_layout_empty).transition(withCrossFade()).into(layoutEmpty.image)
        }
    }

}