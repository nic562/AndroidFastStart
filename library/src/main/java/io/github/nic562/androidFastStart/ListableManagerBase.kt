package io.github.nic562.androidFastStart

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import java.util.LinkedHashSet

/**
 * @param T 为列表的主数据类型
 * @param K 为SelectionTracker 需要跟中的特点标识
 * @param VH 为 BaseViewHolder 或其子类。
 * @see ListableManager
 *
 * Created by Nic on 2020/2/21.
 */
internal
abstract class ListableManagerBase<T, K, VH: BaseViewHolder> : ListableManager<K> {
    var mTotalCount = 0
    protected set

    private var currPage = 1
    private var mLimit = 10
    private var mCanLoadMore = true
    private var mAutoLoadMore = true

    var recyclerView: RecyclerView? = null
        private set

    private var selectionTracker: SelectionTracker<K>? = null
    private var selectionKeyProvider: ItemKeyProvider<K>? = null
    private var storageStrategy: StorageStrategy<K>? = null

    protected abstract val adapter: BaseQuickAdapter<T, VH>

    protected fun setLoadMoreModule(loadMoreModule: BaseLoadMoreModule?) {
        loadMoreModule?.apply {
            isEnableLoadMore = mCanLoadMore
            isAutoLoadMore = mAutoLoadMore
            isEnableLoadMoreIfNotFullPage = mAutoLoadMore
            setOnLoadMoreListener {
                if (currPage * mLimit >= mTotalCount) {
                    loadMoreEnd(true)
                } else {
                    myLoadData(++currPage, mLimit)
                }
            }
        }
    }

    private fun createView(@LayoutRes resID: Int): View {
        if (recyclerView != null) {
            return LayoutInflater.from(recyclerView!!.context).inflate(resID, recyclerView, false)
        } else {
            throw RuntimeException("View container had not been provide! Please call `setViewContainer()` first.")
        }
    }

    override fun getTotalCount(): Int {
        return mTotalCount
    }

    override fun getLimit(): Int {
        return mLimit
    }

    override fun setLimit(limit: Int) {
        this.mLimit = limit
    }

    override fun getCanLoadMore(): Boolean {
        return mCanLoadMore
    }

    override fun setCanLoadMore(b: Boolean) {
        this.mCanLoadMore = b
    }

    override fun getAutoLoadMore(): Boolean {
        return mAutoLoadMore
    }

    override fun setAutoLoadMore(b: Boolean) {
        this.mAutoLoadMore = b
    }

    override fun reloadData() {
        clearData()
        myLoadData(currPage, mLimit)
    }

    override fun setSelectionTracker(selectionTracker: SelectionTracker<K>) {
        this.selectionTracker = selectionTracker
    }

    override fun getSelectionTracker(): SelectionTracker<K>? {
        return selectionTracker
    }

    override fun getSelectionKeyProvider(): ItemKeyProvider<K>? {
        return selectionKeyProvider
    }

    override fun setSelectionKeyProvider(selectionKeyProvider: ItemKeyProvider<K>) {
        this.selectionKeyProvider = selectionKeyProvider
    }

    override fun setSelectionStorageStrategy(storageStrategy: StorageStrategy<K>) {
        this.storageStrategy = storageStrategy
    }

    override fun getStorageStrategy(): StorageStrategy<K>? {
        return storageStrategy
    }

    override fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
        if (selectionTracker != null) {
            throw NotImplementedError("SelectionTracker need a ItemDetailsProvide. Please override this function")
        }
        return null
    }

    override fun setViewContainer(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter
        this.recyclerView = recyclerView
    }

    override fun clearData() {
        adapter.data.clear()
        adapter.notifyDataSetChanged()
        currPage = 1
    }

    override fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun removeData(position: Int) {
        adapter.remove(position)
        adapter.notifyDataSetChanged()
    }

    override fun setEmptyView(@LayoutRes resID: Int) {
        adapter.setEmptyView(resID)
    }

    override fun setEmptyView(v: View) {
        adapter.setEmptyView(v)
    }

    fun addHeaderView(@LayoutRes resID: Int): View {
        return addHeaderView(resID, -1, LinearLayout.VERTICAL)
    }

    override fun addHeaderView(@LayoutRes resID: Int, index: Int, orientation: Int): View {
        val v = createView(resID)
        adapter.addHeaderView(v, index, orientation)
        return v
    }

    fun addHeaderView(view: View) {
        return addHeaderView(view, -1, LinearLayout.VERTICAL)
    }

    override fun addHeaderView(view: View, index: Int, orientation: Int) {
        adapter.addHeaderView(view, index, orientation)
    }

    fun setHeaderView(@LayoutRes resID: Int): View {
        return setHeaderView(resID, 0, LinearLayout.VERTICAL)
    }

    override fun setHeaderView(@LayoutRes resID: Int, index: Int, orientation: Int): View {
        val v = createView(resID)
        adapter.setHeaderView(v, index, orientation)
        return v
    }

    fun setHeaderView(view: View) {
        return setHeaderView(view, 0, LinearLayout.VERTICAL)
    }

    override fun setHeaderView(view: View, index: Int, orientation: Int) {
        adapter.setHeaderView(view, index, orientation)
    }

    override fun hasHeaderLayout(): Boolean {
        return adapter.hasHeaderLayout()
    }

    override fun removeHeaderView(header: View) {
        adapter.removeHeaderView(header)
    }

    override fun removeAllHeaderView() {
        adapter.removeAllHeaderView()
    }

    fun addFooterView(@LayoutRes resID: Int): View {
        return addFooterView(resID, -1, LinearLayout.VERTICAL)
    }

    override fun addFooterView(@LayoutRes resID: Int, index: Int, orientation: Int): View {
        val v = createView(resID)
        adapter.addFooterView(v, index, orientation)
        return v
    }

    fun addFooterView(view: View) {
        return addFooterView(view, -1, LinearLayout.VERTICAL)
    }

    override fun addFooterView(view: View, index: Int, orientation: Int) {
        adapter.addFooterView(view, index, orientation)
    }

    fun setFooterView(@LayoutRes resID: Int): View {
        return setFooterView(resID, 0, LinearLayout.VERTICAL)
    }

    override fun setFooterView(@LayoutRes resID: Int, index: Int, orientation: Int): View {
        val v = createView(resID)
        adapter.setFooterView(v, index, orientation)
        return v
    }

    fun setFooterView(view: View) {
        return setFooterView(view, 0, LinearLayout.VERTICAL)
    }

    override fun setFooterView(view: View, index: Int, orientation: Int) {
        adapter.setFooterView(view, index, orientation)
    }

    override fun hasFooterLayout(): Boolean {
        return adapter.hasFooterLayout()
    }

    override fun removeFooterView(footer: View) {
        adapter.removeFooterView(footer)
    }

    override fun removeAllFooterView() {
        adapter.removeAllFooterView()
    }

    /**
     * 当显示空布局时，是否显示 头布局
     */
    override fun setHeaderWithEmptyEnable(boolean: Boolean) {
        adapter.headerWithEmptyEnable = boolean
    }

    /**
     * 当显示空布局时，是否显示 脚布局
     */
    override fun setFooterWithEmptyEnable(boolean: Boolean) {
        adapter.footerWithEmptyEnable = boolean
    }

    override fun setItemClickListener(listener: OnItemClickListener) {
        adapter.setOnItemClickListener { _, view, position ->
            listener.onItemClick(view, position)
        }
    }

    override fun setItemLongClickListener(listener: OnItemLongClickListener) {
        adapter.setOnItemLongClickListener { _, view, position ->
            return@setOnItemLongClickListener listener.onItemLongClick(view, position)
        }
    }

    override fun setItemChildClickListener(listener: OnItemChildClickListener) {
        adapter.setOnItemChildClickListener { _, view, position ->
            listener.onItemChildClick(view, position)
        }
    }

    override fun setItemChildLongClickListener(listener: OnItemChildLongClickListener) {
        adapter.setOnItemChildLongClickListener { _, view, position ->
            return@setOnItemChildLongClickListener listener.onItemChildLongClick(view, position)
        }
    }

    override fun addChildClickViewIds(@IdRes vararg viewIds: Int) {
        adapter.addChildClickViewIds(*viewIds)
    }

    override fun getChildLongClickViewIds(): LinkedHashSet<Int> {
        return adapter.getChildLongClickViewIds()
    }

    override fun setAnimationEnable(boolean: Boolean) {
        adapter.animationEnable = boolean
    }
}