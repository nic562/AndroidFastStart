package io.github.nic562.androidFastStart

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.*
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import io.github.nic562.androidFastStart.viewholder.ViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import java.util.LinkedHashSet

/**
 * 实现基于RecyclerView的序列数据展示功能接口
 *
 * <T, K> 中： T 为 序列数据的类型； K 为 序列数据跟踪器（可重写 getItemDetailsProvider） ItemDetails 的关键字段数据类型
 *
 * Created by Nic on 2019/12/20.
 */
interface SomethingListable<T, K> : SomethingWithContext {

    val listableManager: ListableManager<T, K>

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

    abstract class ListableManager<T, K> {
        var totalCount = 0
            private set

        var currPage = 1
        var limit = 10
        var canLoadMore = true
        var autoLoadMore = true
        val dataList = mutableListOf<T>()

        abstract val listItemLayoutID: Int

        private var recyclerView: RecyclerView? = null

        private var selectionTracker: SelectionTracker<K>? = null
        private var selectionKeyProvider: ItemKeyProvider<K>? = null
        private var storageStrategy: StorageStrategy<K>? = null

        private abstract class Adapter<T, K>(layoutID: Int, dataList: MutableList<T>) :
                BaseQuickAdapter<T, ViewHolder<K>>(layoutID, dataList), LoadMoreModule, AnkoLogger {
            override fun onBindViewHolder(holder: ViewHolder<K>, position: Int) {
                bindItemDetails(holder, position)
                super.onBindViewHolder(holder, position)
            }

            override fun onBindViewHolder(holder: ViewHolder<K>, position: Int, payloads: MutableList<Any>) {
                bindItemDetails(holder, position)
                super.onBindViewHolder(holder, position, payloads)
            }

            private fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                if (holder.itemDetailsProvider == null) {
                    holder.itemDetailsProvider = getItemDetailsProvider()
                }
                holder.getItemDetails()?.position = position
            }

            abstract fun getItemDetailsProvider(): ItemDetailsProvider<K>?

            /**
             * 必须重写该方法
             *
             * selectionTracker 的事件触发调用了onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>)
             *
             * 父类实现并没有实现该方法，造成selectionTracker 触发后UI上无更新
             *
             * @param payloads 如果是 selectionTracker 触发的，会相应到一个 `Selection-Changed` 的字符串
             * 官方RecyclerView.Adapter 源码中的
             * onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) 也直接忽略了 payloads参数而，所以本实现也依照该方式。
             * 这可能需要 或者针对不同的事件做不同响应，暂时无法预料，先留坑。
             */
            override fun convert(helper: ViewHolder<K>, item: T?, payloads: List<Any>) {
                warn("onBindViewHolder with playLoads ::: $payloads ${payloads[0].javaClass}")
                convert(helper, item)
            }
        }

        private val adapter: Adapter<T, K> by lazy {
            val a = object : Adapter<T, K>(listItemLayoutID, dataList) {
                override fun convert(helper: ViewHolder<K>, item: T?) {
                    if (item != null) {
                        itemConvert(helper, item)
                    }
                }

                override fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
                    return this@ListableManager.getItemDetailsProvider()
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

        fun setSelectionTracker(selectionTracker: SelectionTracker<K>) {
            this.selectionTracker = selectionTracker
        }

        fun getSelectionTracker(): SelectionTracker<K>? {
            return selectionTracker
        }

        fun getSelectionKeyProvider(): ItemKeyProvider<K>? {
            return selectionKeyProvider
        }

        fun setSelectionKeyProvider(selectionKeyProvider: ItemKeyProvider<K>) {
            this.selectionKeyProvider = selectionKeyProvider
        }

        fun setSelectionStorageStrategy(storageStrategy: StorageStrategy<K>) {
            this.storageStrategy = storageStrategy
        }

        fun getStorageStrategy(): StorageStrategy<K>? {
            return storageStrategy
        }

        open fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
            if (selectionTracker != null) {
                throw NotImplementedError("SelectionTracker need a ItemDetailsProvide. Please override this function")
            }
            return null
        }

        abstract fun itemConvert(helper: ViewHelper<K>, item: T)

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

        fun notifyDataSetChanged() {
            adapter.notifyDataSetChanged()
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

    fun initListable(recyclerView: RecyclerView,
                     layoutManager: RecyclerView.LayoutManager = getDefaultListableLayoutManager(),
                     withDefaultSelectionTracker: Boolean = false) {
        recyclerView.layoutManager = layoutManager
        listableManager.setViewContainer(recyclerView)
        if (withDefaultSelectionTracker) {
            val selectionKeyProvider = listableManager.getSelectionKeyProvider()
                    ?: throw NotImplementedError("Using SelectionTracker please calling setSelectionKeyProvider(..) at first!")
            val storageStrategy = listableManager.getStorageStrategy()
                    ?: throw NotImplementedError("Using SelectionTracker please calling setSelectionStorageStrategy(..) at first!")
            val selectionTracker = SelectionTracker.Builder<K>(
                    "my_selection",
                    recyclerView,
                    selectionKeyProvider,
                    object : ItemDetailsLookup<K>() {
                        @Suppress("UNCHECKED_CAST")
                        override fun getItemDetails(e: MotionEvent): ItemDetails<K>? {
                            val v = recyclerView.findChildViewUnder(e.x, e.y)
                            if (v != null) {
                                val viewHolder = recyclerView.getChildViewHolder(v)
                                return (viewHolder as ViewHelper<K>).getItemDetails()
                            }
                            return null
                        }
                    },
                    storageStrategy
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything<K>()).build()
            listableManager.setSelectionTracker(selectionTracker)
        }
    }
}