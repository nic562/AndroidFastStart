package io.github.nic562.androidFastStart

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.entity.node.NodeFooterImp
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.util.getItemView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.ViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAbleData
import org.jetbrains.anko.AnkoLogger

/**
 * 支持树状结构的数据列表
 *
 * @param K 为 序列数据跟踪器（可重写 getItemDetailsProvider） ItemDetails 的关键字段数据类型
 *
 * @see SomethingListableBase<K>
 *
 * Created by Nic on 2020/2/21.
 */
interface SomethingTreeListable<K> : SomethingListableBase<K> {

    interface TreeListableManager<K> : ListableManager<K> {
        /**
         * 展开或收起节点
         * @param position 需要收起或展开的节点所在位置。使用使用动画
         * @param notifyDataChange 是否发起数据变更通知，同： adapter.notifyDataSetChanged. 例如手动执行 UI 修改等，请设置为false。
         * @param animate 是否执行动画， 仅在 notifyDataChange 为 true 时才有效
         * @param payload 局部更新的标记对象，仅在 animate 为 true 时才有效
         * @return 结果为 true 时，表明该方法执行后为展开状态；为 false 时，表明该方法执行后为收起状态
         */
        fun expand(@IntRange(from = 0) position: Int, notifyDataChange: Boolean = true, animate: Boolean = true, payload: Any? = null): Boolean

        /**
         * 判断节点 的展开状态
         *  @return 展开时为 true，收起时为 false
         */
        fun isExpanded(@IntRange(from = 0) position: Int): Boolean

        fun addData(t: TreeAbleData, position: Int? = null)
        fun replaceData(t: TreeAbleData, position: Int)
        fun getData(position: Int): TreeAbleData?
        fun addViewNode(t: TreeAble)
    }

    interface OnLoadDataCallback {
        fun onLoadData(data: Collection<TreeAbleData>, totalCount: Int, page: Int)
        fun onError()
    }

    override fun getItemDetails(view: View): ItemDetails<K>? {
        return listableManager.getItemDetails(view)
    }

    fun loadListableData(page: Int, limit: Int,
                         dataCallback: OnLoadDataCallback)

    override fun instanceListableManager(vararg args: Any): TreeListableManager<K> {
        var ext = ListableManager.EXT.NORMAL
        if (args.isNotEmpty()) {
            if (args[0] is ListableManager.EXT)
                ext = args[0] as ListableManager.EXT
            else
                throw RuntimeException("The first arg means the [external Tools], it must be an ListableManager.EXT")
        }
        return object : MyListableManager<K>(ext) {
            override fun loadData(page: Int, limit: Int, dataCallback: OnLoadDataCallback) {
                loadListableData(page, limit, dataCallback)
            }

            override fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
                return getListableItemDetailsProvider() ?: super.getItemDetailsProvider()
            }
        }
    }

    private interface MyNode {
        val data: TreeAbleData

        fun mkNodes(ns: MutableList<TreeAbleData>?): MutableList<BaseNode>? {
            if (ns == null) {
                return null
            }
            val l = arrayListOf<BaseNode>()
            for (x in ns) {
                if (x.children?.isEmpty() == true) {
                    l.add(Node(x))
                } else {
                    l.add(ExpandNode(x))
                }
            }
            return l
        }

        fun mkFooter(footer: TreeAbleData?): BaseNode? {
            if (footer == null) {
                return null
            }
            return Node(footer)
        }
    }

    private class Node(mData: TreeAbleData) : BaseNode(), NodeFooterImp, MyNode {
        override val data: TreeAbleData = mData
        override val childNode: MutableList<BaseNode>? by lazy {
            mkNodes(data.children)
        }
        override val footerNode: BaseNode? by lazy {
            mkFooter(data.footer)
        }
    }

    private class ExpandNode(mData: TreeAbleData) : BaseExpandNode(), NodeFooterImp, MyNode {
        override val data: TreeAbleData = mData
        override val childNode: MutableList<BaseNode>? by lazy {
            mkNodes(data.children)
        }
        override val footerNode: BaseNode? by lazy {
            mkFooter(data.footer)
        }
    }

    private abstract class Adapter<K> : BaseNodeAdapter(mutableListOf()), LoadMoreModule, AnkoLogger {

        override fun getItemType(data: List<BaseNode>, position: Int): Int {
            return (data[position] as MyNode).data.tree
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            bindItemDetails(holder, position)
            super.onBindViewHolder(holder, position)
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: MutableList<Any>) {
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
        abstract fun bindItemDetails(holder: BaseViewHolder, position: Int)
    }

    private abstract class AdapterWithDraggableAndUpFetch<K> : Adapter<K>(), UpFetchModule, DraggableModule
    private abstract class AdapterWithDraggable<K> : Adapter<K>(), DraggableModule
    private abstract class AdapterWithUpFetch<K> : Adapter<K>(), UpFetchModule

    private abstract class MyListableManager<K>(ext: ListableManager.EXT) : ListableManagerBase<BaseNode, K, BaseViewHolder>(), TreeListableManager<K> {

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback)

        private fun mBindItemDetails(holder: BaseViewHolder, position: Int) {
            if (holder !is ViewHolder<*>) {
                return
            }
            val vh = exchangeViewHolder(holder)
            if (vh.itemDetailsProvider == null) {
                vh.itemDetailsProvider = this@MyListableManager.getItemDetailsProvider()
            }
            when (vh.itemViewType) {
                BaseQuickAdapter.EMPTY_VIEW, BaseQuickAdapter.HEADER_VIEW, BaseQuickAdapter.FOOTER_VIEW, BaseQuickAdapter.LOAD_MORE_VIEW -> {
                    vh.getItemDetails()?.position = -1
                    return
                }
                else -> {
                    vh.getItemDetails()?.position = position
                }
            }
        }

        override val adapter: Adapter<K> by lazy {
            val a = when (ext) {
                ListableManager.EXT.WITH_DRAGGABLE_AND_UP_FETCH -> {
                    object : AdapterWithDraggableAndUpFetch<K>() {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_DRAGGABLE -> {
                    object : AdapterWithDraggable<K>() {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_UP_FETCH -> {
                    object : AdapterWithUpFetch<K>() {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.NORMAL -> {
                    object : Adapter<K>() {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
            }
            a.apply {
                setLoadMoreModule(loadMoreModule)
            }
        }

        override fun getData(position: Int): TreeAbleData? {
            val d = adapter.getItemOrNull(position) ?: return null
            if (d is MyNode) {
                return d.data
            }
            return null
        }

        override fun addData(t: TreeAbleData, position: Int?) {
            val n = tree2Node(t)
            if (position == null) {
                adapter.addData(n)
            } else
                adapter.addData(position, n)
        }

        override fun replaceData(t: TreeAbleData, position: Int) {
            val n = tree2Node(t)
            adapter.data[position] = n
        }

        override fun addViewNode(t: TreeAble) {
            adapter.addNodeProvider(getNodeProvider(t))
        }

        override fun expand(position: Int, notifyDataChange: Boolean, animate: Boolean, payload: Any?): Boolean {
            adapter.expandOrCollapse(position, animate, notifyDataChange, payload)
            return isExpanded(position)
        }

        override fun isExpanded(position: Int): Boolean {
            val data = adapter.data[position]
            if (data is ExpandNode) {
                return data.isExpanded
            }
            return false
        }

        private fun tree2Node(t: TreeAbleData): BaseNode {
            return if (t.children?.isEmpty() == true) {
                Node(t)
            } else {
                ExpandNode(t)
            }
        }

        private val onLoadDataCallback = object : OnLoadDataCallback {
            override fun onLoadData(data: Collection<TreeAbleData>, totalCount: Int, page: Int) {
                this@MyListableManager.mTotalCount = totalCount
                if (data.isNotEmpty()) {
                    for (x in data) {
                        adapter.addData(tree2Node(x))
                    }
                    setPage(page)
                }
                adapter.loadMoreModule.loadMoreComplete()
            }

            override fun onError() {
                adapter.loadMoreModule.loadMoreFail()
            }
        }

        override fun getItemDetails(view: View): ItemDetails<K>? {
            val viewHolder = recyclerView?.getChildViewHolder(view) ?: return null
            return (exchangeViewHolder(viewHolder)).getItemDetails()
        }

        override fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        @Suppress("UNCHECKED_CAST")
        private fun exchangeViewHolder(h: RecyclerView.ViewHolder): ViewHolder<K> {
            return h as ViewHolder<K>
        }

        private fun getNodeProvider(treeN: TreeAble): BaseNodeProvider {
            return object : BaseNodeProvider() {
                override val itemViewType = treeN.tree
                override val layoutId: Int = treeN.layoutResID

                init {
                    if (treeN.childClickViewIds != null) {
                        addChildClickViewIds(*treeN.childClickViewIds!!)
                    }
                    if (treeN.childLongClickViewIds != null) {
                        addChildLongClickViewIds(*treeN.childLongClickViewIds!!)
                    }
                }

                override fun convert(helper: BaseViewHolder, item: BaseNode) {
                    val n = item as MyNode
                    treeN.convert(exchangeViewHolder(helper), n.data)
                }

                override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
                    val n = item as MyNode
                    treeN.convert(exchangeViewHolder(helper), n.data, payloads)
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<K> {
                    val w = treeN.onCreateItemViewWrapper(parent, viewType)
                    val vh =
                            if (w == null) ViewHolder<K>(parent.getItemView(layoutId))
                            else ViewHolder(w)
                    return vh.apply {
                        treeN.onViewHolderCreated(this, viewType)
                    }

                }

                override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
                    if (treeN.onChildClick == null)
                        return super.onChildClick(helper, view, data, position)
                    treeN.onChildClick!!.onChildClick(exchangeViewHolder(helper), view, (data as MyNode).data, position)
                }

                override fun onChildLongClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int): Boolean {
                    if (treeN.onChildLongClick == null)
                        return super.onChildLongClick(helper, view, data, position)
                    return treeN.onChildLongClick!!.onChildLongClick(exchangeViewHolder(helper), view, (data as MyNode).data, position)
                }

                override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
                    if (treeN.onClick == null)
                        return super.onClick(helper, view, data, position)
                    treeN.onClick!!.onClick(exchangeViewHolder(helper), view, (data as MyNode).data, position)
                }

                override fun onLongClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int): Boolean {
                    if (treeN.onLongClick == null)
                        return super.onLongClick(helper, view, data, position)
                    return treeN.onLongClick!!.onLongClick(exchangeViewHolder(helper), view, (data as MyNode).data, position)
                }
            }
        }
    }

}