package io.github.nic562.androidFastStart

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
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
 * <T, K> 中： T 为 序列数据的类型； K 为 序列数据跟踪器（可重写 getItemDetailsProvider） ItemDetails 的关键字段数据类型
 *
 * Created by Nic on 2019/12/20.
 */
interface SomethingListable<T, K> : SomethingListableBase<K> {

    interface OnLoadDataCallback<T> {
        fun onLoadData(data: Collection<T>, totalCount: Int)
        fun onError()
    }

    fun loadListableData(page: Int, limit: Int,
                         dataCallback: OnLoadDataCallback<T>)

    fun listableItemConvert(helper: ViewHelper<K>, item: T)

    override fun instanceListableManager(vararg args: Any): ListableManager<K> {
        if (!(args.isNotEmpty() && args[0] is Int)) {
            throw RuntimeException("The first arg means the [List Item Layout ID], it must be an Int.")
        }
        if (!(args.size > 1 && args[1] is MutableList<*>)) {
            throw RuntimeException("The second arg means the [Data List<T>], it must be an MutableList<T>")
        }
        return object : NormalListableManager<T, K>(args[0] as Int, args[1] as MutableList<T>) {

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

    private abstract class NormalListableManager<T, K>(listItemLayoutID: Int, dataList: MutableList<T>) : ListableManagerBase<T, K, ViewHolder<K>>() {

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

        override val adapter: BaseQuickAdapter<T, ViewHolder<K>> by lazy {
            object : Adapter<T, K>(listItemLayoutID, dataList) {
                override fun convert(helper: ViewHolder<K>, item: T?) {
                    if (item != null) {
                        itemConvert(helper, item)
                    }
                }

                override fun bindItemDetails(holder: ViewHolder<K>, position: Int) {
                    if (holder.itemDetailsProvider == null) {
                        holder.itemDetailsProvider = this@NormalListableManager.getItemDetailsProvider()
                    }
                    when (holder.itemViewType) {
                        EMPTY_VIEW, HEADER_VIEW, FOOTER_VIEW, LOAD_MORE_VIEW -> {
                            holder.getItemDetails()?.position = -1
                            return
                        }
                        else -> {
                            holder.getItemDetails()?.position = position
                        }
                    }
                }
            }.apply { setLoadMoreModule(loadMoreModule) }
        }

        private val onLoadDataCallback = object : OnLoadDataCallback<T> {
            override fun onLoadData(data: Collection<T>, totalCount: Int) {
                this@NormalListableManager.totalCount = totalCount
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

        abstract fun itemConvert(helper: ViewHelper<K>, item: T)

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback<T>)

        override fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        override fun getItemDetails(view: View): ItemDetails<K>? {
            val viewHolder = recyclerView?.getChildViewHolder(view) ?: return null
            return (viewHolder as ViewHelper<K>).getItemDetails()
        }

    }

    override fun getItemDetails(view: View): ItemDetails<K>? {
        return listableManager.getItemDetails(view)
    }
}