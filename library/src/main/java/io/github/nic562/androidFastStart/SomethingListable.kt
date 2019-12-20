package io.github.nic562.androidFastStart

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 实现循环列表展示数据功能接口
 *
 *
 *
 * Created by Nic on 2019/12/20.
 */
interface SomethingListable<T> : SomethingWithContext {

    fun getListableRecyclerView(): RecyclerView

    val listableManager: ListableManager<T>

    /**
     * 获取多数情况下通用的垂直布局LayoutManager
     *
     * 可根据实际应用重写该方法
     */
    fun getListableLayoutManager(): RecyclerView.LayoutManager {
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

    class ViewHolder {
        private var holder: BaseViewHolder? = null

        fun setHolder(hd: BaseViewHolder) {
            holder = hd
        }

        fun <T : View> getView(@IdRes viewId: Int): T {
            return holder!!.getView(viewId)
        }

        fun setText(@IdRes viewId: Int, value: CharSequence?): ViewHolder {
            holder!!.setText(viewId, value)
            return this
        }

        fun setText(@IdRes viewId: Int, @StringRes strId: Int): ViewHolder? {
            holder!!.setText(viewId, strId)
            return this
        }

        fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHolder {
            holder!!.setTextColor(viewId, color)
            return this
        }

        fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHolder {
            holder!!.setTextColorRes(viewId, colorRes)
            return this
        }

        fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHolder {
            holder!!.setImageResource(viewId, imageResId)
            return this
        }

        fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHolder? {
            holder!!.setImageDrawable(viewId, drawable)
            return this
        }

        fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHolder? {
            holder!!.setImageBitmap(viewId, bitmap)
            return this
        }

        fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHolder? {
            holder!!.setBackgroundColor(viewId, color)
            return this
        }

        fun setBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): ViewHolder? {
            holder!!.setBackgroundResource(viewId, backgroundRes)
            return this
        }

        fun setVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHolder {
            holder!!.setVisible(viewId, isVisible)
            return this
        }

        fun setGone(@IdRes viewId: Int, isGone: Boolean): ViewHolder {
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

        abstract fun itemConvert(helper: ViewHolder, item: T)

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback<T>)

        private fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        fun setViewContainer(recyclerView: RecyclerView) {
            recyclerView.adapter = adapter
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

        fun setEmptyView(resID: Int) {
            adapter.setEmptyView(resID)
        }

        fun setEmptyView(v: View) {
            adapter.setEmptyView(v)
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
    }

    fun initListable() {
        val rv = getListableRecyclerView()
        rv.layoutManager = getListableLayoutManager()
        listableManager.setViewContainer(rv)
    }
}