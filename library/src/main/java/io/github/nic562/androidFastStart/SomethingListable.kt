package io.github.nic562.androidFastStart

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.LinkedHashSet

/**
 * 实现循环列表展示数据功能接口
 *
 *
 *
 * Created by Nic on 2019/12/20.
 */
interface SomethingListable<T> : SomethingWithContext {

    val listableManager: ListableManager<T>

    /**
     * 获取多数情况下通用的垂直布局LayoutManager
     *
     */
    private
    fun getDefaultListableLayoutManager(): RecyclerView.LayoutManager {
        val lm = LinearLayoutManager(getOwnerContext())
        lm.orientation = LinearLayoutManager.VERTICAL
        return lm
    }

    private abstract class Adapter<T>(layoutID: Int, dataList: MutableList<T>) :
            BaseQuickAdapter<T, BaseViewHolder>(layoutID, dataList), LoadMoreModule

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int): Boolean
    }

    interface OnItemChildClickListener {
        fun onItemChildClick(view: View, position: Int)
    }

    interface OnItemChildLongClickListener {
        fun onItemChildLongClick(view: View, position: Int): Boolean
    }

    interface OnLoadDataCallback<T> {
        fun onLoadData(data: Collection<T>, totalCount: Int)
        fun onError()
    }

    interface ViewHelper {
        fun <T : View> getView(@IdRes viewId: Int): T
        fun setText(@IdRes viewId: Int, value: CharSequence?): ViewHelper
        fun setText(@IdRes viewId: Int, @StringRes strId: Int): ViewHelper?
        fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper
        fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHelper
        fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHelper
        fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHelper?
        fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHelper?
        fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper?
        fun setBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): ViewHelper?
        fun setVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHelper
        fun setGone(@IdRes viewId: Int, isGone: Boolean): ViewHelper
    }

    private class ViewHolder : ViewHelper {
        private var holder: BaseViewHolder? = null

        fun setHolder(hd: BaseViewHolder) {
            holder = hd
        }

        override fun <T : View> getView(@IdRes viewId: Int): T {
            return holder!!.getView(viewId)
        }

        override fun setText(@IdRes viewId: Int, value: CharSequence?): ViewHelper {
            holder!!.setText(viewId, value)
            return this
        }

        override fun setText(@IdRes viewId: Int, @StringRes strId: Int): ViewHelper? {
            holder!!.setText(viewId, strId)
            return this
        }

        override fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper {
            holder!!.setTextColor(viewId, color)
            return this
        }

        override fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHelper {
            holder!!.setTextColorRes(viewId, colorRes)
            return this
        }

        override fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHelper {
            holder!!.setImageResource(viewId, imageResId)
            return this
        }

        override fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHelper? {
            holder!!.setImageDrawable(viewId, drawable)
            return this
        }

        override fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHelper? {
            holder!!.setImageBitmap(viewId, bitmap)
            return this
        }

        override fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper? {
            holder!!.setBackgroundColor(viewId, color)
            return this
        }

        override fun setBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): ViewHelper? {
            holder!!.setBackgroundResource(viewId, backgroundRes)
            return this
        }

        override fun setVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHelper {
            holder!!.setVisible(viewId, isVisible)
            return this
        }

        override fun setGone(@IdRes viewId: Int, isGone: Boolean): ViewHelper {
            holder!!.setGone(viewId, isGone)
            return this
        }
    }

    abstract class ListableManager<T> {
        var totalCount = 0
            private set

        var currPage = 1
        var limit = 10
        var canLoadMore = true
        var autoLoadMore = true
        val dataList = mutableListOf<T>()

        abstract val listItemLayoutID: Int

        private val viewHolder = ViewHolder()

        private var recyclerView: RecyclerView? = null

        private val adapter: Adapter<T> by lazy {
            val a = object : Adapter<T>(listItemLayoutID, dataList) {
                override fun convert(helper: BaseViewHolder, item: T?) {
                    if (item != null) {
                        viewHolder.setHolder(helper)
                        itemConvert(viewHolder, item)
                    }
                }
            }

            a.loadMoreModule!!.isEnableLoadMore = canLoadMore
            a.loadMoreModule!!.isAutoLoadMore = autoLoadMore
            a.loadMoreModule!!.isEnableLoadMoreIfNotFullPage = false
            a.loadMoreModule!!.setOnLoadMoreListener {
                if (currPage * limit >= totalCount) {
                    a.loadMoreModule!!.loadMoreEnd(true)
                } else {
                    myLoadData(++currPage, limit)
                }
            }
            a
        }

        private val onLoadDataCallback = object : OnLoadDataCallback<T> {
            override fun onLoadData(data: Collection<T>, totalCount: Int) {
                this@ListableManager.totalCount = totalCount
                if (data.isNotEmpty()) {
                    adapter.data.addAll(data)
                    adapter.notifyDataSetChanged()
                }
                adapter.loadMoreModule?.loadMoreComplete()
            }

            override fun onError() {
                adapter.loadMoreModule?.loadMoreComplete()
            }
        }

        abstract fun itemConvert(helper: ViewHelper, item: T)

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback<T>)

        private fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        private fun createView(@LayoutRes resID: Int): View {
            if (recyclerView != null) {
                return LayoutInflater.from(recyclerView!!.context).inflate(resID, recyclerView, false)
            } else {
                throw RuntimeException("View container had not been provide! Please call `setViewContainer()` first.")
            }
        }

        fun setViewContainer(recyclerView: RecyclerView) {
            recyclerView.adapter = adapter
            this.recyclerView = recyclerView
        }

        fun clearData() {
            adapter.data.clear()
            adapter.notifyDataSetChanged()
            currPage = 1
        }

        fun reloadData() {
            clearData()
            myLoadData(currPage, limit)
        }

        fun removeData(position: Int) {
            adapter.remove(position)
            adapter.notifyDataSetChanged()
        }

        fun addData(data: T) {
            adapter.addData(data)
            adapter.notifyDataSetChanged()
        }

        fun setEmptyView(@LayoutRes resID: Int) {
            adapter.setEmptyView(resID)
        }

        fun setEmptyView(v: View) {
            adapter.setEmptyView(v)
        }

        fun addHeaderView(@LayoutRes resID: Int, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): View {
            val v = createView(resID)
            adapter.addHeaderView(v, index, orientation)
            return v
        }

        fun addHeaderView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL) {
            adapter.addHeaderView(view, index, orientation)
        }

        fun setHeaderView(@LayoutRes resID: Int, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): View {
            val v = createView(resID)
            adapter.setHeaderView(v, index, orientation)
            return v
        }

        fun setHeaderView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL) {
            adapter.setHeaderView(view, index, orientation)
        }

        fun hasHeaderLayout(): Boolean {
            return adapter.hasHeaderLayout()
        }

        fun removeHeaderView(header: View) {
            adapter.removeHeaderView(header)
        }

        fun removeAllHeaderView() {
            adapter.removeAllHeaderView()
        }

        fun addFooterView(@LayoutRes resID: Int, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): View {
            val v = createView(resID)
            adapter.addFooterView(v, index, orientation)
            return v
        }

        fun addFooterView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL) {
            adapter.addFooterView(view, index, orientation)
        }

        fun setFooterView(@LayoutRes resID: Int, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): View {
            val v = createView(resID)
            adapter.setFooterView(v, index, orientation)
            return v
        }

        fun setFooterView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL) {
            adapter.setFooterView(view, index, orientation)
        }

        fun hasFooterLayout(): Boolean {
            return adapter.hasFooterLayout()
        }

        fun removeFooterView(footer: View) {
            adapter.removeFooterView(footer)
        }

        fun removeAllFooterView() {
            adapter.removeAllFooterView()
        }

        /**
         * 当显示空布局时，是否显示 头布局
         */
        fun setHeaderWithEmptyEnable(boolean: Boolean) {
            adapter.headerWithEmptyEnable = boolean
        }

        /**
         * 当显示空布局时，是否显示 脚布局
         */
        fun setFooterWithEmptyEnable(boolean: Boolean) {
            adapter.footerWithEmptyEnable = boolean
        }

        fun setItemClickListener(listener: OnItemClickListener) {
            adapter.setOnItemClickListener { _, view, position ->
                listener.onItemClick(view, position)
            }
        }

        fun setItemLongClickListener(listener: OnItemLongClickListener) {
            adapter.setOnItemLongClickListener { _, view, position ->
                return@setOnItemLongClickListener listener.onItemLongClick(view, position)
            }
        }

        fun setItemChildClickListener(listener: OnItemChildClickListener) {
            adapter.setOnItemChildClickListener { _, view, position ->
                listener.onItemChildClick(view, position)
            }
        }

        fun setItemChildLongClickListener(listener: OnItemChildLongClickListener) {
            adapter.setOnItemChildLongClickListener { _, view, position ->
                return@setOnItemChildLongClickListener listener.onItemChildLongClick(view, position)
            }
        }

        fun addChildClickViewIds(@IdRes vararg viewIds: Int) {
            adapter.addChildClickViewIds(*viewIds)
        }

        fun getChildLongClickViewIds(): LinkedHashSet<Int> {
            return adapter.getChildLongClickViewIds()
        }

        fun setAnimationEnable(boolean: Boolean) {
            adapter.animationEnable = boolean
        }
    }

    fun initListable(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager = getDefaultListableLayoutManager()) {
        recyclerView.layoutManager = layoutManager
        listableManager.setViewContainer(recyclerView)
    }
}