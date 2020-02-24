package io.github.nic562.androidFastStartDemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.nic562.androidFastStart.*
import io.github.nic562.androidFastStart.viewholder.BaseTree
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2020/2/22.
 */
class ActivityTree : ActivityBase(), SomethingTreeListable<Long> {
    override val listableManager: SomethingTreeListable.TreeListableManager<Long> by lazy {
        instanceListableManager(ListableManager.EXT.WITH_DRAGGABLE)
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
        swipeRefreshLayout.isRefreshing = false
        listableManager.setCanLoadMore(true)
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

        swipeRefreshLayout.setOnRefreshListener {
            loadDelay()
        }

        listableManager.apply {
            setAnimationEnable(true)
            setHeaderWithEmptyEnable(true)
            setFooterWithEmptyEnable(true)
            setItemDragListener(object : OnItemDragListener {
                override fun onItemDragMoving(source: RecyclerView.ViewHolder?, from: Int, target: RecyclerView.ViewHolder?, to: Int) {
                    println("drag move from ${source?.adapterPosition} ::: $from to ${target?.adapterPosition} ::: $to")
                }

                override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    println("drag start from :::::: $pos")
                    val startColor = Color.WHITE
                    val endColor = Color.rgb(245, 245, 245)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ValueAnimator.ofArgb(startColor, endColor).apply {
                            addUpdateListener {
                                viewHolder?.itemView?.setBackgroundColor(it.animatedValue as Int)
                            }
                            duration = 300
                            start()
                        }
                    } else {
                        print("Value Animator not support!!!!!")
                    }
                }

                override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    println("drag end to :::::: $pos")
                    val endColor = Color.WHITE
                    val startColor = Color.rgb(245, 245, 245)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ValueAnimator.ofArgb(startColor, endColor).apply {
                            addUpdateListener {
                                viewHolder?.itemView?.setBackgroundColor(it.animatedValue as Int)
                            }
                            duration = 300
                            start()
                        }
                    } else {
                        print("Value Animator not support!!!!!")
                    }
                }
            })
            setItemDragFlags(ItemTouchHelper.DOWN or ItemTouchHelper.UP)
            setItemSwipeListener(object: OnItemSwipeListener {
                override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    println("swipe start from $pos")
                }

                override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    println("swipe clear!!!!!!!!")
                }

                override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                    println("swipe end::: $pos")
                }

                override fun onItemSwipeMoving(canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, isCurrentlyActive: Boolean) {
                    println("swipe on $dX x $dY  ($isCurrentlyActive)")
                    canvas?.drawColor(ContextCompat.getColor(this@ActivityTree, R.color.colorAccent))
                }
            })
            setItemSwipeFlags(ItemTouchHelper.START)
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
        loadDelay()
    }

    private fun loadDelay() {
        listableManager.setCanLoadMore(false) // 防止触发下拉加载数据事件
        swipeRefreshLayout.isRefreshing = true
        rv_cards.postDelayed({
            listableManager.reloadData()
        }, 2000)
    }

    override fun getOwnerContext(): Context {
        return this
    }
}