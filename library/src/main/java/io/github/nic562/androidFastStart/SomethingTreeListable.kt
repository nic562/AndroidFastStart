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
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import org.jetbrains.anko.AnkoLogger

/**
 * Created by Nic on 2020/2/21.
 */
interface SomethingTreeListable<K> : SomethingListableBase<K> {

    override val listableManager: TreeListableManager<K>

    interface OnLoadDataCallback {
        fun onLoadData(data: Collection<TreeAble>, totalCount: Int)
        fun onError()
    }

    override fun getItemDetails(view: View): ItemDetails<K>? {
        return listableManager.getItemDetails(view)
    }

    abstract class TreeListableManager<K> : ListableManagerBase<BaseNode, K>() {

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
        }

        override val adapter: BaseQuickAdapter<BaseNode, BaseViewHolder> = object : NodeAdapter<K>() {
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

        private val onLoadDataCallback = object : OnLoadDataCallback {
            override fun onLoadData(data: Collection<TreeAble>, totalCount: Int) {
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

        fun getItemDetails(view: View): ItemDetails<K>? {
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