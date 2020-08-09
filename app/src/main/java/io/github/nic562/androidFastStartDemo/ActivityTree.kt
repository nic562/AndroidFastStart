package io.github.nic562.androidFastStartDemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.SlidingConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import io.github.nic562.androidFastStart.*
import io.github.nic562.androidFastStart.viewholder.BaseTree
import io.github.nic562.androidFastStart.viewholder.BaseTreeData
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAbleData
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*
import org.jetbrains.anko.toast

/**
 * Created by Nic on 2020/2/22.
 */
class ActivityTree : ActivityBase(), SomethingTreeListable<Long> {
    companion object {
        fun rotationImageView(iv: ImageView, rotation: Float, animate: Boolean = true) {
            if (animate)
                ViewCompat.animate(iv).apply {
                    duration = 200
                    interpolator = DecelerateInterpolator()
                    rotation(rotation)
                    start()
                }
            else
                iv.rotation = rotation
        }
    }

    override val listableManager: SomethingTreeListable.TreeListableManager<Long> by lazy {
        instanceListableManager(ListableManager.EXT.WITH_DRAGGABLE)
    }

    private val onExpandClick = object : TreeAble.OnClick {
        override fun <K> onClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int) {
            listableManager.expand(position, payload = listOf(99, !listableManager.isExpanded(position)))
        }
    }

    private val onExpandLongClick = object : TreeAble.OnLongClick {
        override fun <K> onLongClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int): Boolean {
            if (listableManager.expand(position, payload = listOf(99, !listableManager.isExpanded(position)))) {
                toast("展开")
            } else {
                toast("收起")
            }
            return true
        }
    }

    private class FootNodeData(treeIdx: Int, val title: String) : BaseTreeData() {
        override val tree: Int = treeIdx
    }

    private class FootNode(treeIdx: Int, @LayoutRes resID: Int) : BaseTree() {

        override val tree: Int = treeIdx
        override val layoutResID: Int = resID
        override val childClickViewIds: IntArray? = intArrayOf(R.id.tv_content)
        override val onChildClick: TreeAble.OnChildClick? = object : TreeAble.OnChildClick {
            override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int) {
                data as FootNodeData
                Toast.makeText(view.context, "${data.title} child click", Toast.LENGTH_SHORT).show()
            }
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData) {
            data as FootNodeData
            helper.hSetText(R.id.tv_title, data.title)
        }
    }

    private class RootNodeData(val title: String, ch: MutableList<TreeAbleData>?) : BaseTreeData() {
        override val tree = 0
        override val children: MutableList<TreeAbleData>? = ch
        override val footer: TreeAbleData = FootNodeData(1, title)
    }

    private class RootNode(onclick: TreeAble.OnClick, val listableManager: SomethingTreeListable.TreeListableManager<Long>) : BaseTree() {

        override val tree: Int = 0
        override val layoutResID: Int = R.layout.layout_tree_root
        override val onClick: TreeAble.OnClick? = onclick
        override val childClickViewIds: IntArray? = intArrayOf(R.id.btn_test)
        override val onChildClick: TreeAble.OnChildClick? = object : TreeAble.OnChildClick {
            override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int) {
                data as RootNodeData
                Toast.makeText(view.context, "${data.title} child click", Toast.LENGTH_SHORT).show()
            }
        }
        override val childLongClickViewIds: IntArray? = intArrayOf(R.id.btn_test1)
        override val onChildLongClick: TreeAble.OnChildLongClick? = object : TreeAble.OnChildLongClick {
            override fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int): Boolean {
                data as RootNodeData
                Toast.makeText(view.context, "${data.title} child long click", Toast.LENGTH_SHORT).show()
                listableManager.removeData(position)
                return true
            }
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData) {
            data as RootNodeData
            helper.hSetText(R.id.tv_tree_root, data.title)
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData, payloads: List<Any>) {
            @Suppress("UNCHECKED_CAST")
            if (payloads.isNotEmpty() && payloads[0] is List<*>) {
                val cmd = payloads[0] as List<Any>
                when (cmd[0]) {
                    99 -> {
                        val isExpend = (cmd[1] as Boolean)
                        rotationImageView(helper.hGetView(R.id.iv_spin), if (isExpend) 0f else -90f)
                    }
                }
            }
        }
    }

    private class ChildNodeData(val title: String, ch: MutableList<TreeAbleData>?) : BaseTreeData() {
        override val tree: Int = 2
        override val children: MutableList<TreeAbleData>? = ch
        override val footer: TreeAbleData? = FootNodeData(3, title)
    }

    private class ChildNode(onLongClick: TreeAble.OnLongClick) : BaseTree() {

        override val tree: Int = 2
        override val layoutResID: Int = R.layout.layout_tree_child
        override val onLongClick: TreeAble.OnLongClick? = onLongClick
        override val childClickViewIds: IntArray? = intArrayOf(R.id.btn_test)
        override val onChildClick: TreeAble.OnChildClick? = object : TreeAble.OnChildClick {
            override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int) {
                val d = data as ChildNodeData
                Toast.makeText(view.context, "${d.title} child click", Toast.LENGTH_SHORT).show()
            }
        }
        override val childLongClickViewIds: IntArray? = intArrayOf(R.id.btn_test1)
        override val onChildLongClick: TreeAble.OnChildLongClick? = object : TreeAble.OnChildLongClick {
            override fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int): Boolean {
                val d = data as ChildNodeData
                Toast.makeText(view.context, "${d.title} child long click", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData) {
            data as ChildNodeData
            helper.hSetText(R.id.tv_tree_child, data.title)
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData, payloads: List<Any>) {
            @Suppress("UNCHECKED_CAST")
            if (payloads.isNotEmpty() && payloads[0] is List<*>) {
                val cmd = payloads[0] as List<Any>
                when (cmd[0]) {
                    99 -> {
                        val isExpend = (cmd[1] as Boolean)
                        rotationImageView(helper.hGetView(R.id.iv_spin), if (isExpend) 0f else -90f)
                    }
                }
            }
        }
    }

    private class ChildNode1Data(val title: String) : BaseTreeData() {
        override val tree: Int = 4
    }

    private class ChildNode1(childClick: TreeAble.OnChildClick) : BaseTree() {
        companion object {
            val ClickableChildIds = intArrayOf(R.id.btn_up, R.id.btn_down)
        }

        override val tree: Int = 4
        override val layoutResID: Int = R.layout.layout_tree_child1
        override val childClickViewIds: IntArray? = ClickableChildIds
        override val onChildClick: TreeAble.OnChildClick? = childClick

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData) {
            data as ChildNode1Data
            helper.hSetText(R.id.tv_tree_child, data.title)
        }

        override fun onCreateItemViewWrapper(parent: ViewGroup, viewType: Int): ViewGroup? {
            val v = LayoutInflater.from(parent.context).inflate(layoutResID, parent, false)
            val x = TextView(parent.context).apply {
                textSize = 14f
                setBackgroundColor(0xFFAA0000.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                gravity = Gravity.CENTER
                width = 200
            }
            return SmartSwipe.wrap(v).addConsumer(SlidingConsumer().apply {
                setRightDrawerView(x)
                releaseMode = SwipeConsumer.RELEASE_MODE_AUTO_CLOSE
                relativeMoveFactor = SlidingConsumer.FACTOR_FOLLOW
                setScrimColor(0x1F000000)
                addListener(object : SimpleSwipeListener() {
                    override fun onSwipeStart(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {
                        x.text = "hi swiped: ${System.currentTimeMillis()}"
                    }
                })
            }).wrapper
        }

        override fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData, payloads: List<Any>) {
            ValueAnimator.ofFloat(1f, 0.1f).apply {
                val v = helper.hGetView<View>(R.id.tv_tree_child)
                addUpdateListener {
                    v.alpha = it.animatedValue as Float
                }
                doOnStart {
                    helper.hGetView<Button>(R.id.btn_up).isEnabled = false
                    helper.hGetView<Button>(R.id.btn_down).isEnabled = false
                }
                doOnEnd {
                    ValueAnimator.ofFloat(0.1f, 1f).apply {
                        addUpdateListener {
                            v.alpha = it.animatedValue as Float
                        }
                        doOnStart {
                            convert(helper, data)
                        }
                        doOnEnd {
                            helper.hGetView<Button>(R.id.btn_up).isEnabled = true
                            helper.hGetView<Button>(R.id.btn_down).isEnabled = true
                        }
                        duration = 500
                        start()
                    }
                }
                duration = 500
                start()
            }
        }
    }


    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingTreeListable.OnLoadDataCallback) {
        swipeRefreshLayout.isRefreshing = false
        listableManager.setCanLoadMore(true)
        val data = arrayListOf<TreeAbleData>()
        for (i in 0 until limit) {
            val ch = arrayListOf<TreeAbleData>()
            for (j in 0 until 3) {
                val gh = arrayListOf<TreeAbleData>()
                for (k in 0 until 5) {
                    gh.add(ChildNode1Data("Grandchild: $page - $i>$j>$k"))
                }
                ch.add(ChildNodeData("Child: $page - $i>$j", gh))
            }
            data.add(RootNodeData("root: $page - $i", ch))
        }

        dataCallback.onLoadData(data, 20, page)
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
            initListable(rv_cards)
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            addViewNode(RootNode(onExpandClick, this))
            addViewNode(FootNode(1, R.layout.layout_tree_root_foot))
            addViewNode(ChildNode(onExpandLongClick))
            addViewNode(FootNode(3, R.layout.layout_tree_child_foot))
            addViewNode(ChildNode1(object : TreeAble.OnChildClick {
                override fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int) {
                    when (view.id) {
                        R.id.btn_up -> {
                            val d0 = listableManager.getData(position - 1) ?: return
                            val d1 = data
                            listableManager.replaceData(d1, position - 1)
                            listableManager.replaceData(d0, position)
                            listableManager.notifyItemChanged(position, "replace")
                            listableManager.notifyItemChanged(position - 1, "replace")
                        }
                        R.id.btn_down -> {
                            val d0 = listableManager.getData(position + 1) ?: return
                            val d1 = data
                            listableManager.replaceData(d1, position + 1)
                            listableManager.replaceData(d0, position)
                            listableManager.notifyItemChanged(position, "replace")
                            listableManager.notifyItemChanged(position + 1, "replace")
                        }
                    }

                }
            }))
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