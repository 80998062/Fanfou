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
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.sinyuk.fanfou.BuildConfig
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.States
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.TYPE_GLOBAL
import com.sinyuk.fanfou.domain.UNIQUE_ID
import com.sinyuk.fanfou.ui.MarginDecoration
import com.sinyuk.fanfou.util.obtainViewModel
import com.sinyuk.fanfou.util.obtainViewModelFromActivity
import com.sinyuk.fanfou.viewmodel.AccountViewModel
import com.sinyuk.fanfou.viewmodel.ConnectionModel
import com.sinyuk.fanfou.viewmodel.FanfouViewModelFactory
import com.sinyuk.fanfou.viewmodel.TimelineViewModel
import com.sinyuk.myutils.system.ToastUtils
import kotlinx.android.synthetic.main.timeline_view.*
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

        fun contextTimeline(path: String, status: Status, photoExtra: Bundle?) = TimelineView().apply {
            arguments = Bundle().apply {
                putString("path", path)
                putBundle("photoExtra", photoExtra)
                putParcelable("status", status)
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

    private val accountViewModel by lazy { obtainViewModelFromActivity(factory, AccountViewModel::class.java) }

    @Inject
    lateinit var toast: ToastUtils

    private val handler by lazy { Handler() }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        // Cannot add the same observer with different lifeCycles! So use activity here.
        activity?.let {
            ConnectionModel.livedata(it.applicationContext).observe(activity!!,
                    Observer { if (it?.isConnected == true) refresh() })
        }

        configTimeline()
    }

    /**
     * 决定显示什么接口的列表
     */
    private fun configTimeline() {
        val path: String = arguments!!.getString("path")
        val validId: String = when {
            arguments!!.containsKey("status") -> arguments!!.getParcelable<Status>("status").id
            arguments!!.containsKey("player") -> arguments!!.getParcelable<Player>("player").uniqueId
            else -> sharedPreferences.getString(UNIQUE_ID, "")
        }
        setupSwipeRefresh()
        setupRecyclerView()
        timelineViewModel.setRelativeUrl(path, validId, null)
    }


    fun search(q: String) {

    }

    fun refresh() {
        if (isRefreshing) return
        if (BuildConfig.DEBUG) Log.d(TAG, "trigger refresh fromUser")
        timelineViewModel.refresh()
//        adapter.currentAccount = sharedPreferences.getString(UNIQUE_ID, null)
    }


    private lateinit var adapter: StatusPagedListAdapter

    @Suppress("MemberVisibilityCanBePrivate")
    @field:[Inject Named(TYPE_GLOBAL)]
    lateinit var sharedPreferences: SharedPreferences


    override fun onFavorited(favorited: Boolean, v: View?, p: Int, status: Status) {
        if (favorited) {
            timelineViewModel.createFavorite(status.id)
                    .observe(this@TimelineView, Observer {
                        if (it?.states == States.ERROR) {
                            it.message?.let { toast.toastShort(it) }
                            adapter.setFavorited(p, false)
                        }
                    })
        } else {
            timelineViewModel.destroyFavorite(status.id)
                    .observe(this@TimelineView, Observer {
                        if (it?.states == States.ERROR) {
                            it.message?.let { toast.toastShort(it) }
                            adapter.setFavorited(p, true)
                        }
                    })
        }
    }


    override fun onDeleted(v: View?, p: Int, status: Status) {
        timelineViewModel.delete(status.id)
                .observe(this@TimelineView, Observer {
                    if (it?.states == States.ERROR) {
                        it.message?.let { toast.toastShort(it) }
                        adapter.setDeleted(p, false)
                    }
                })
    }


    override fun onResume() {
        super.onResume()
//        if (!EventBus.getDefault().isRegistered(this))
//            EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
//        if (EventBus.getDefault().isRegistered(this))
//            EventBus.getDefault().unregister(this)
    }

    private var isRefreshing = false
    private fun setupSwipeRefresh() {
        timelineViewModel.refreshState.observe(this@TimelineView,
                Observer {
                    isRefreshing = it?.states == States.LOADING
                    if (!isRefreshing) {
//                        val toastView = View.inflate(context, R.layout.toast_fetch_top, null)
//                        val popup = PopupWindow(toastView, WRAP_CONTENT, WRAP_CONTENT)
//                        popup.isFocusable = false
//                        if (States.ERROR == it?.states) {
//                            toastView.textView.text = it.message
//                            toastView.textView.setOnClickListener {
//                                popup.dismiss()
//                            }
//                        } else if (States.SUCCESS == it?.states) {
//                            toastView.textView.text = "Succeed"
//                            toastView.textView.setOnClickListener {
//                                recyclerView?.scrollToPosition(0)
//                                popup.dismiss()
//                            }
//                            popup.showAtLocation(container,
//                                    Gravity.TOP or Gravity.CENTER_HORIZONTAL,
//                                    0, ConvertUtils.dp2px(context, 16f))
//                            handler.postDelayed({ popup.dismiss() }, 5000)
//                        }
                    }
                })
    }

    private fun setupRecyclerView() {
        recyclerView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL)
                KeyboardUtil.hideKeyboard(view)
            return@setOnTouchListener false
        }

        LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = PAGE_SIZE
            recyclerView.layoutManager = this
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(MarginDecoration(R.dimen.divider_size, false, context!!))

        val path: String = arguments!!.getString("path")

        adapter = StatusPagedListAdapter(
                this@TimelineView,
                { timelineViewModel.retry() },
                path,
                sharedPreferences.getString(UNIQUE_ID, null))

        if (arguments!!.containsKey("status")) {
            adapter.contextStatus = arguments!!.getParcelable<Status>("status").id
            adapter.contextStatusPhotoExtra = arguments!!.getBundle("photoExtra")
        }

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

    private var lastRecyclerViewPosition = 0
    private val pagedListConsumer = Observer<PagedList<Status>> {
        // Preserves the user's scroll position if items are inserted outside the viewable area:
        adapter.submitList(it)
//        recyclerView.post { recyclerView.layoutManager.onRestoreInstanceState(recyclerViewState) }
    }

    private val networkConsumer = Observer<NetworkState> {
        adapter.setNetworkState(it)
    }

//    private fun showEmptyView() {
//        viewAnimator.displayedChildId = R.id.layoutEmpty
//        if (layoutEmpty.title.text.isNullOrEmpty()) {
//            Log.d(TAG, "showEmptyView")
//            layoutEmpty.title.text = getString(R.string.state_title_empty)
//            val span = AndroidSpan().drawRelativeSizeSpan(getString(R.string.state_description_empty), 1f)
//                    .drawWithOptions("Refresh?", SpanOptions().addTextAppearanceSpan(context!!, R.style.text_bold_primary).addSpan(object : ClickableSpan() {
//                        override fun onClick(v: View?) {
//
//                        }
//                    })).spanText
//            layoutEmpty.description.movementMethod = LinkTouchMovementMethod.getInstance()
//            layoutEmpty.description.text = span
//            GlideApp.with(this).load(R.drawable.state_layout_empty).transition(withCrossFade()).into(layoutEmpty.image)
////            layoutEmpty.image.setImageResource(R.drawable.state_layout_empty)
//        }
//    }

}