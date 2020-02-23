package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingTreeListable
import io.github.nic562.androidFastStart.viewholder.BaseTree
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2020/2/22.
 */
class ActivityTree : ActivityBase(), SomethingTreeListable<Long> {
    override val listableManager: SomethingTreeListable.TreeListableManager<Long> by lazy {
        instanceListableManager()
    }

    private val onExpandClick = object : TreeAble.OnClick {
        override fun <K> onClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int) {
            listableManager.expand(position)
        }
    }

    private val onExpandLongClick = object : TreeAble.OnLongClick {
        override fun <K> onLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean {
            listableManager.expand(position)
            return true
        }
    }

    private class FootNode(private val title: String, @LayoutRes resID: Int, treeIdx: Int) : BaseTree() {
        companion object {
            val ClickableChildIds = intArrayOf(R.id.tv_content)
            val OnChildClick = object : TreeAble.OnChildClick {
                override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int) {
                    val d = data as FootNode
                    Toast.makeText(view.context, "${d.title} child click", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override val tree: Int = treeIdx
        override val layoutResID: Int = resID
        override val childClickViewIds: IntArray? = ClickableChildIds
        override val onChildClick: TreeAble.OnChildClick? = OnChildClick

        override fun <K> convert(helper: ViewHelper<K>) {
            helper.hSetText(R.id.tv_title, title)
        }
    }

    private class RootNode(private val title: String, onclick: TreeAble.OnClick, ch: MutableList<TreeAble>?) : BaseTree(ch) {
        companion object {
            val ClickableChildIds = intArrayOf(R.id.btn_test)
            val LongClickableChildIds = intArrayOf(R.id.btn_test1)
            val OnChildClick = object : TreeAble.OnChildClick {
                override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int) {
                    val d = data as RootNode
                    Toast.makeText(view.context, "${d.title} child click", Toast.LENGTH_SHORT).show()
                }
            }
            val OnChildLongClick = object : TreeAble.OnChildLongClick {
                override fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean {
                    val d = data as RootNode
                    Toast.makeText(view.context, "${d.title} child long click", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }

        override val tree: Int = 0
        override val layoutResID: Int = R.layout.layout_tree_root
        override val expandable: Boolean = true
        override val footer: TreeAble? = FootNode("foot: $title", R.layout.layout_tree_root_foot, 1)
        override val onClick: TreeAble.OnClick? = onclick
        override val childClickViewIds: IntArray? = ClickableChildIds
        override val onChildClick: TreeAble.OnChildClick? = OnChildClick
        override val childLongClickViewIds: IntArray? = LongClickableChildIds
        override val onChildLongClick: TreeAble.OnChildLongClick? = OnChildLongClick

        override fun <K> convert(helper: ViewHelper<K>) {
            helper.hSetText(R.id.tv_tree_root, title)
        }
    }

    private class ChildNode(private val title: String, onLongClick: TreeAble.OnLongClick, ch: MutableList<TreeAble>?) : BaseTree(ch) {
        companion object {
            val ClickableChildIds = intArrayOf(R.id.btn_test)
            val LongClickableChildIds = intArrayOf(R.id.btn_test1)
            val OnChildClick = object : TreeAble.OnChildClick {
                override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int) {
                    val d = data as ChildNode
                    Toast.makeText(view.context, "${d.title} child click", Toast.LENGTH_SHORT).show()
                }
            }
            val OnChildLongClick = object : TreeAble.OnChildLongClick {
                override fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean {
                    val d = data as ChildNode
                    Toast.makeText(view.context, "${d.title} child long click", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }

        override val tree: Int = 2
        override val layoutResID: Int = R.layout.layout_tree_child
        override val expandable: Boolean = true
        override val footer: TreeAble? = FootNode("child foot: $title", R.layout.layout_tree_child_foot, 3)
        override val onLongClick: TreeAble.OnLongClick? = onLongClick
        override val childClickViewIds: IntArray? = ClickableChildIds
        override val onChildClick: TreeAble.OnChildClick? = OnChildClick
        override val childLongClickViewIds: IntArray? = LongClickableChildIds
        override val onChildLongClick: TreeAble.OnChildLongClick? = OnChildLongClick

        override fun <K> convert(helper: ViewHelper<K>) {
            helper.hSetText(R.id.tv_tree_child, title)
        }
    }

    private class ChildNode1(private val title: String) : BaseTree() {
        companion object {
            val ClickableChildIds = intArrayOf(R.id.btn_test)
            val LongClickableChildIds = intArrayOf(R.id.btn_test1)
            val OnChildClick = object : TreeAble.OnChildClick {
                override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int) {
                    val d = data as ChildNode1
                    Toast.makeText(view.context, "${d.title} child click", Toast.LENGTH_SHORT).show()
                }
            }
            val OnChildLongClick = object : TreeAble.OnChildLongClick {
                override fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean {
                    val d = data as ChildNode1
                    Toast.makeText(view.context, "${d.title} child long click", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }

        override val tree: Int = 4
        override val layoutResID: Int = R.layout.layout_tree_child1
        override val childClickViewIds: IntArray? = ClickableChildIds
        override val onChildClick: TreeAble.OnChildClick? = OnChildClick
        override val childLongClickViewIds: IntArray? = LongClickableChildIds
        override val onChildLongClick: TreeAble.OnChildLongClick? = OnChildLongClick

        override fun <K> convert(helper: ViewHelper<K>) {
            helper.hSetText(R.id.tv_tree_child, title)
        }
    }


    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingTreeListable.OnLoadDataCallback) {
        val data = arrayListOf<TreeAble>()
        for (i in 0 until limit) {
            val ch = arrayListOf<TreeAble>()
            for (j in 0 until 3) {
                val gh = arrayListOf<TreeAble>()
                for (k in 0 until 5) {
                    gh.add(ChildNode1("Grandchild: $page - $i $j $k"))
                }
                ch.add(ChildNode("Child: $page - $i $j", onExpandLongClick, gh))
            }
            data.add(RootNode("root: $page $i", onExpandClick, ch))
        }

        dataCallback.onLoadData(data, 20)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        listableManager.apply {
            setAnimationEnable(true)
            setHeaderWithEmptyEnable(true)
            setFooterWithEmptyEnable(true)

            initListable(rv_cards)
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
//            setItemClickListener(object: OnItemClickListener {
//                override fun onItemClick(view: View, position: Int) {
//                    println("$view >>>>>> $position")
//                }
//            })
        }
    }

    override fun onStart() {
        super.onStart()
        listableManager.reloadData()
    }

    override fun getOwnerContext(): Context {
        return this
    }
}