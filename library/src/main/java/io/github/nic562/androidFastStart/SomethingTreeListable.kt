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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

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

    interface TreeListableManager<K>: ListableManager<K> {
        fun expand(@IntRange(from = 0) position: Int, animate: Boolean = true)
    }

    interface OnLoadDataCallback {
        fun onLoadData(data: Collection<TreeAble>, totalCount: Int)
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
        val treeNode: TreeAble

        fun mkNodes(ns: MutableList<TreeAble>?): MutableList<BaseNode>? {
            if (ns == null) {
                return null
            }
            val l = arrayListOf<BaseNode>()
            for (x in ns) {
                if (x.expandable) {
                    l.add(ExpandNode(x))
                } else {
                    l.add(Node(x))
                }
            }
            return l
        }

        fun mkFooter(footer: TreeAble?): BaseNode? {
            if (footer == null) {
                return null
            }
            return Node(footer)
        }
    }

    private class Node(tree: TreeAble) : BaseNode(), NodeFooterImp, MyNode {
        override val treeNode: TreeAble = tree
        override val childNode: MutableList<BaseNode>? by lazy {
            mkNodes(treeNode.children)
        }
        override val footerNode: BaseNode? by lazy {
            mkFooter(treeNode.footer)
        }
    }

    private class ExpandNode(tree: TreeAble) : BaseExpandNode(), NodeFooterImp, MyNode {
        override val treeNode: TreeAble = tree
        override val childNode: MutableList<BaseNode>? by lazy {
            mkNodes(treeNode.children)
        }
        override val footerNode: BaseNode? by lazy {
            mkFooter(treeNode.footer)
        }
    }

    private abstract class Adapter<K> : BaseNodeAdapter(mutableListOf()), LoadMoreModule, AnkoLogger {

        override fun getItemType(data: List<BaseNode>, position: Int): Int {
            return (data[position] as MyNode).treeNode.tree
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
        override fun convert(helper: BaseViewHolder, item: BaseNode?, payloads: List<Any>) {
            warn("onBindViewHolder with playLoads ::: $payloads ${payloads[0].javaClass}")
            convert(helper, item)
        }
    }

    private abstract class AdapterWithDraggableAndUpFetch<K>: Adapter<K>(), UpFetchModule, DraggableModule
    private abstract class AdapterWithDraggable<K>: Adapter<K>(), DraggableModule
    private abstract class AdapterWithUpFetch<K>: Adapter<K>(), UpFetchModule

    private abstract class MyListableManager<K>(ext: ListableManager.EXT) : ListableManagerBase<BaseNode, K, BaseViewHolder>(), TreeListableManager<K> {

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback)

        private val providerMap = mutableMapOf<Int, BaseNodeProvider>()

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

        override val adapter: BaseQuickAdapter<BaseNode, BaseViewHolder> by lazy {
            val a = when(ext) {
                ListableManager.EXT.WITH_DRAGGABLE_AND_UP_FETCH -> {
                    object : AdapterWithDraggableAndUpFetch<K> () {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_DRAGGABLE -> {
                    object : AdapterWithDraggable<K> () {
                        override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                            mBindItemDetails(holder, position)
                        }
                    }
                }
                ListableManager.EXT.WITH_UP_FETCH -> {
                    object : AdapterWithUpFetch<K> () {
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

        @Suppress("UNCHECKED_CAST")
        override fun expand(position: Int, animate: Boolean) {
            (adapter as Adapter<K>).expandOrCollapse(position, animate)
        }

        private val onLoadDataCallback = object : OnLoadDataCallback {
            override fun onLoadData(data: Collection<TreeAble>, totalCount: Int) {
                this@MyListableManager.mTotalCount = totalCount
                if (data.isNotEmpty()) {
                    for (x in data) {
                        findAllNodeProvider(x)
                        if (x.expandable) {
                            adapter.addData(ExpandNode(x))
                        } else {
                            adapter.addData(Node(x))
                        }
                    }
                    increasePage()
                }
                adapter.loadMoreModule?.loadMoreComplete()
            }

            override fun onError() {
                adapter.loadMoreModule?.loadMoreFail()
            }
        }

        override fun getItemDetails(view: View): ItemDetails<K>? {
            val viewHolder = recyclerView?.getChildViewHolder(view) ?: return null
            return (exchangeViewHolder(viewHolder)).getItemDetails()
        }

        override fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        private fun findAllNodeProvider(tree: TreeAble) {
            if (providerMap[tree.tree] == null) {
                providerMap[tree.tree] = getNodeProvider(tree)
            }
            if (tree.footer != null) {
                findAllNodeProvider(tree.footer!!)
            }
            if (!tree.children.isNullOrEmpty()) {
                for (x in tree.children!!) {
                    findAllNodeProvider(x)
                }
            }
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

                override fun convert(helper: BaseViewHolder, data: BaseNode?) {
                    if (data == null) {
                        return
                    }
                    val n = data as MyNode
                    n.treeNode.convert(exchangeViewHolder(helper))
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<K> {
                    return ViewHolder(parent.getItemView(layoutId))
                }

                override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
                    if (treeN.onChildClick == null)
                        return super.onChildClick(helper, view, data, position)
                    treeN.onChildClick!!.onChildClick(exchangeViewHolder(helper), view, (data as MyNode).treeNode, position)
                }

                override fun onChildLongClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int): Boolean {
                    if (treeN.onChildLongClick == null)
                        return super.onChildLongClick(helper, view, data, position)
                    return treeN.onChildLongClick!!.onChildLongClick(exchangeViewHolder(helper), view, (data as MyNode).treeNode, position)
                }

                override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
                    if (treeN.onClick == null)
                        return super.onClick(helper, view, data, position)
                    treeN.onClick!!.onClick(exchangeViewHolder(helper), view, (data as MyNode).treeNode, position)
                }

                override fun onLongClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int): Boolean {
                    if (treeN.onLongClick == null)
                        return super.onLongClick(helper, view, data, position)
                    return treeN.onLongClick!!.onLongClick(exchangeViewHolder(helper), view, (data as MyNode).treeNode, position)
                }
            }.apply {
                (adapter as BaseNodeAdapter).addNodeProvider(this)
            }
        }
    }

}