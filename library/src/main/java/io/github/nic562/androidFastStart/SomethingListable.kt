package io.github.nic562.androidFastStart

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.Companion.EMPTY_VIEW
import com.chad.library.adapter.base.BaseQuickAdapter.Companion.FOOTER_VIEW
import com.chad.library.adapter.base.BaseQuickAdapter.Companion.HEADER_VIEW
import com.chad.library.adapter.base.BaseQuickAdapter.Companion.LOAD_MORE_VIEW
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.module.UpFetchModule
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.ViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import java.lang.RuntimeException

/**
 * 实现基于RecyclerView的序列数据展示功能接口
 *
 * @param T 为 序列数据的类型
 * @param K 为 序列数据跟踪器（可重写 getItemDetailsProvider） ItemDetails 的关键字段数据类型
 *
 * @see SomethingListableBase<K>
 *
 * Created by Nic on 2019/12/20.
 */
interface SomethingListable<T, K> : SomethingListableBase<K> {

    /**
     * 暂定义，为后续根据该类功能单独扩展接口而预留
     */
    interface DataListableManager<T, K> : ListableManager<K>

    interface OnLoadDataCallback<T> {
        fun onLoadData(data: Collection<T>, totalCount: Int)
        fun onError()
    }

    fun loadListableData(page: Int, limit: Int,
                         dataCallback: OnLoadDataCallback<T>)

    fun listableItemConvert(helper: ViewHelper<K>, item: T)

    override fun instanceListableManager(vararg args: Any): DataListableManager<T, K> {
        if (!(args.isNotEmpty() && args[0] is Int)) {
            throw RuntimeException("The first arg means the [List Item Layout ID], it must be an Int.")
        }
        if (!(args.size > 1 && args[1] is MutableList<*>)) {
            throw RuntimeException("The second arg means the [Data List<T>], it must be an MutableList<T>")
        }

        var ext = ListableManager.EXT.NORMAL
        if (args.size > 2) {
            if (args[2] is ListableManager.EXT)
                ext = args[2] as ListableManager.EXT
            else
                throw RuntimeException("The third arg means the [external Tools], it must be an ListableManager.EXT")
        }

        @Suppress("UNCHECKED_CAST")
        return object : NormalListableManager<T, K>(args[0] as Int, args[1] as MutableList<T>, ext) {

            override fun itemConvert(helper: ViewHelper<K>, item: T) {
                listableItemConvert(helper, item)
            }

            override fun loadData(page: Int, limit: Int, dataCallback: OnLoadDataCallback<T>) {
                loadListableData(page, limit, dataCallback)
            }

            override fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
                return getListableItemDetailsProvider() ?: super.getItemDetailsProvider()
            }
        }
    }

    private abstract class Adapter<T, K>(layoutID: Int, list: MutableList<T>) :
            BaseQuickAdapter<T, ViewHolder<K>>(layoutID, list), LoadMoreModule, AnkoLogger {

        override fun onBindViewHolder(holder: ViewHolder<K>, position: Int) {
            bindItemDetails(holder, position)
            super.onBindViewHolder(holder, position)
        }

        override fun onBindViewHolder(holder: ViewHolder<K>, position: Int, payloads: MutableList<Any>) {
            bindItemDetails(holder, position)
            super.onBindViewHolder(holder, position, payloads)
        }

        /**
         * 以下调整，使得 数据列中，额外的头部数据控件，空数据占位控件，底部数据控件，加载更多的占位控件 的点击事件并不会触发 selectionTracker
         *
         * 除了 -1， 其他负数还是会触发 selectionTracker 所以全部归集到-1，目前尚不可区分对待
         *
         * 目前该 ItemDetails 只在 SelectionTracker 中用到，所以并不会对其他组件造成影响。
         */
        abstract fun bindItemDetails(holder: ViewHolder<K>, position: Int)

        /**
         * 局部刷新所用的方法
         *
         * 使用 selectionTracker 时 必须重写该方法
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
        override fun convert(helper: ViewHolder<K>, item: T, payloads: List<Any>) {
            warn("onBindViewHolder with playLoads ::: $payloads ${payloads[0].javaClass}")
            convert(helper, item)
        }
    }

    private abstract class AdapterWithDraggableAndUpFetch<T, K>(layoutID: Int, list: MutableList<T>) : Adapter<T, K>(layoutID, list), UpFetchModule, DraggableModule
    private abstract class AdapterWithDraggable<T, K>(layoutID: Int, list: MutableList<T>) : Adapter<T, K>(layoutID, list), DraggableModule
    private abstract class AdapterWithUpFetch<T, K>(layoutID: Int, list: MutableList<T>) : Adapter<T, K>(layoutID, list), UpFetchModule

    private abstract class NormalListableManager<T, K>(listItemLayoutID: Int, dataList: MutableList<T>, ext: ListableManager.EXT) : ListableManagerBase<T, K, ViewHolder<K>>(), DataListableManager<T, K> {

        private fun mBindItemDetails(holder: ViewHolder<K>, position: Int) {
            if (holder.itemDetailsProvider == null) {
                holder.itemDetailsProvider = this.getItemDetailsProvider()
            }
            when (holder.itemViewType) {
                EMPTY_VIEW, HEADER_VIEW, FOOTER_VIEW, LOAD_MORE_VIEW -> {
                    // 这部分组件 不响应SelectionTracker 事件
                    holder.getItemDetails()?.position = -1
                    return
                }
                else -> {
                    holder.getItemDetails()?.position = position
                }
            }
        }

        override val adapter: BaseQuickAdapter<T, ViewHolder<K>> by lazy {
            val a = when (ext) {
                ListableManager.EXT.WITH_DRAGGABLE_AND_UP_FETCH -> {
                    object : AdapterWithDraggableAndUpFetch<T, K>(listItemLayoutID, dataList) {
                        override fun convert(helper: ViewHolder<K>, item: T) {
                            if (item != null) {
                                itemConvert(helper, item)
                            }
                        }

                        override fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_DRAGGABLE -> {
                    object : AdapterWithDraggable<T, K>(listItemLayoutID, dataList) {
                        override fun convert(helper: ViewHolder<K>, item: T) {
                            if (item != null) {
                                itemConvert(helper, item)
                            }
                        }

                        override fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_UP_FETCH -> {
                    object : AdapterWithUpFetch<T, K>(listItemLayoutID, dataList) {
                        override fun convert(helper: ViewHolder<K>, item: T) {
                            if (item != null) {
                                itemConvert(helper, item)
                            }
                        }

                        override fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.NORMAL -> {
                    object : Adapter<T, K>(listItemLayoutID, dataList) {
                        override fun convert(helper: ViewHolder<K>, item: T) {
                            if (item != null) {
                                itemConvert(helper, item)
                            }
                        }

                        override fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
            }
            a.apply { setLoadMoreModule(loadMoreModule) }
        }

        private val onLoadDataCallback = object : OnLoadDataCallback<T> {
            override fun onLoadData(data: Collection<T>, totalCount: Int) {
                this@NormalListableManager.mTotalCount = totalCount
                if (data.isNotEmpty()) {
                    adapter.data.addAll(data)
                    adapter.notifyDataSetChanged()
                    increasePage()
                }
                adapter.loadMoreModule?.loadMoreComplete()
            }

            override fun onError() {
                adapter.loadMoreModule?.loadMoreFail()
            }
        }

        abstract fun itemConvert(helper: ViewHelper<K>, item: T)

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback<T>)

        override fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        @Suppress("UNCHECKED_CAST")
        override fun getItemDetails(view: View): ItemDetails<K>? {
            val viewHolder = recyclerView?.getChildViewHolder(view) ?: return null
            return (viewHolder as ViewHelper<K>).getItemDetails()
        }

    }

    override fun getItemDetails(view: View): ItemDetails<K>? {
        return listableManager.getItemDetails(view)
    }
}