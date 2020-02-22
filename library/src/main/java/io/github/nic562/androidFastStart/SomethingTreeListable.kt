package io.github.nic562.androidFastStart

import android.view.View
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.ViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

/**
 * Created by Nic on 2020/2/21.
 */
interface SomethingTreeListable<K> : SomethingListableBase<K> {

    interface OnLoadDataCallback {
        fun onLoadData(data: Collection<TreeAble>, totalCount: Int)
        fun onError()
    }

    override fun getItemDetails(view: View): ItemDetails<K>? {
        return listableManager.getItemDetails(view)
    }

    fun loadListableData(page: Int, limit: Int,
                 dataCallback: OnLoadDataCallback)

    override fun instanceListableManager(vararg args: Any): ListableManager<K> {
        return object: TreeListableManager<K>() {
            override fun loadData(page: Int, limit: Int, dataCallback: OnLoadDataCallback) {
                loadListableData(page, limit, dataCallback)
            }

            override fun getItemDetailsProvider(): ItemDetailsProvider<K>? {
                return getListableItemDetailsProvider() ?: super.getItemDetailsProvider()
            }
        }
    }

    private abstract class TreeListableManager<K> : ListableManagerBase<BaseNode, K, BaseViewHolder>() {

        abstract fun loadData(page: Int, limit: Int,
                              dataCallback: OnLoadDataCallback)

        private val providerMap = mutableMapOf<Int, BaseNodeProvider>()

        private val viewHolderMap = mutableMapOf<Int, ViewHolder<K>>()

        private class Node(val treeNode: TreeAble) : BaseNode() {

            override val childNode: MutableList<BaseNode>? by lazy {
                if (treeNode.children == null) {
                    return@lazy null
                }
                val l = arrayListOf<BaseNode>()
                for (x in treeNode.children!!) {
                    l.add(Node(x))
                }
                l
            }
        }

        private abstract class NodeAdapter<K> : BaseNodeAdapter(mutableListOf()), LoadMoreModule, AnkoLogger {

            override fun getItemType(data: List<BaseNode>, position: Int): Int {
                return (data[position] as Node).treeNode.tree
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
            override fun convert(helper: BaseViewHolder, item: BaseNode?, payloads: List<Any>) {
                warn("onBindViewHolder with playLoads ::: $payloads ${payloads[0].javaClass}")
                convert(helper, item)
            }
        }

        override val adapter: BaseQuickAdapter<BaseNode, BaseViewHolder> by lazy {
            object : NodeAdapter<K>() {
                override fun getItemType(data: List<BaseNode>, position: Int): Int {
                    return (data[position] as Node).treeNode.tree
                }

                override fun bindItemDetails(holder: BaseViewHolder, position: Int) {
                    val vh = this@TreeListableManager.getViewHolder(holder.itemView)
                    if (vh.itemDetailsProvider == null) {
                        vh.itemDetailsProvider = this@TreeListableManager.getItemDetailsProvider()
                    }
                    when (vh.itemViewType) {
                        EMPTY_VIEW, HEADER_VIEW, FOOTER_VIEW, LOAD_MORE_VIEW -> {
                            vh.getItemDetails()?.position = -1
                            return
                        }
                        else -> {
                            vh.getItemDetails()?.position = position
                        }
                    }
                }
            }.apply {
                setLoadMoreModule(loadMoreModule)
            }
        }

        private val onLoadDataCallback = object : OnLoadDataCallback {
            override fun onLoadData(data: Collection<TreeAble>, totalCount: Int) {
                this@TreeListableManager.totalCount = totalCount
                if (data.isNotEmpty()) {
                    for (x in data) {
                        findAllNodeProvider(x)
                        adapter.addData(Node(x))
                    }
                }
                adapter.loadMoreModule?.loadMoreComplete()
            }

            override fun onError() {
                adapter.loadMoreModule?.loadMoreComplete()
            }
        }

        override fun getItemDetails(view: View): ItemDetails<K>? {
            return viewHolderMap[view.hashCode()]?.getItemDetails()
        }

        override fun myLoadData(page: Int, limit: Int) {
            loadData(page, limit, onLoadDataCallback)
        }

        private fun findAllNodeProvider(tree: TreeAble) {
            if (providerMap[tree.tree] == null) {
                providerMap[tree.tree] = getNodeProvider(tree.tree, tree.layoutResID)
            }
            if (!tree.children.isNullOrEmpty()) {
                for (x in tree.children!!) {
                    findAllNodeProvider(x)
                }
            }
        }

        private fun getNodeProvider(tree: Int, @LayoutRes layoutResID: Int): BaseNodeProvider {
            return object : BaseNodeProvider() {
                override val itemViewType = tree
                override val layoutId = layoutResID

                override fun convert(helper: BaseViewHolder, data: BaseNode?) {
                    if (data == null) {
                        return
                    }
                    val h = getViewHolder(helper.itemView)
                    val n = data as Node
                    n.treeNode.convert(h)
                }
            }.apply {
                (adapter as BaseNodeAdapter).addNodeProvider(this)
            }
        }

        private fun getViewHolder(view: View): ViewHolder<K> {
            val c = view.hashCode()

            if (viewHolderMap[c] == null) {
                viewHolderMap[c] = ViewHolder(view)
            }
            return viewHolderMap[c]!!
        }
    }

}