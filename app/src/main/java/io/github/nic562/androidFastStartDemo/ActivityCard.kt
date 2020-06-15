package io.github.nic562.androidFastStartDemo

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.github.nic562.androidFastStart.*
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.random.Random

/**
 * Created by Nic on 2019/12/25.
 */
class ActivityCard : ActivityBase(), SomethingListable<String, Long>, ActionMode.Callback {

    private var actionMode: ActionMode? = null
    private val dataList = mutableListOf<String>()
    override val listableManager: SomethingListable.DataListableManager<String, Long> by lazy {
        instanceListableManager(R.layout.layout_item_card, dataList, ListableManager.EXT.WITH_DRAGGABLE)
        // 开启拖拽滑动支持
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<String>) {
        swipeRefreshLayout.isRefreshing = false
        listableManager.setCanLoadMore(true)
        if (Random(System.currentTimeMillis()).nextInt() % 3 == 0) {
            longToast("测试错误！！~~")
            println("page: $page 测试错误！！~~")
            dataCallback.onError()
            return
        }
        val l = arrayListOf<String>()
        for (i in 1..limit) {
            l.add("$page ${page * limit + i}")
        }
        dataCallback.onLoadData(l, 50, page)
    }

    override fun listableItemConvert(helper: ViewHelper<Long>, item: String) {
        helper.apply {
            hSetText(R.id.tv_title, item)
            hSetText(R.id.tv_subtitle, item)
            updateSelection(helper)
        }
    }

    private fun updateSelection(helper: ViewHelper<Long>) {
        helper.apply {
            val details = getItemDetails()
            val selectionTracker = listableManager.getSelectionTracker()
            val c = hGetView<MaterialCardView>(R.id.card)
            if (details != null && selectionTracker != null) {
                c.isChecked = selectionTracker.isSelected(details.selectionKey)
            } else {
                c.isChecked = false
            }
        }
    }

    /**
     * 演示局部更新，只有当payloads 有效时才执行对应的更新
     */
    override fun listableItemConvert(helper: ViewHelper<Long>, item: String, payloads: List<Any>) {
        println("payload update convert: $item # $payloads")
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                "Selection-Changed" -> {
                    updateSelection(helper)
                }
                "test" -> {
                    val t = StringBuilder()
                    t.append(helper.hGetView<TextView>(R.id.tv_subtitle).text)
                    t.append("\n test.")
                    helper.hSetText(R.id.tv_subtitle, t.toString())
                }
            }
        }
    }

    override fun getListableItemDetailsProvider(): ItemDetailsProvider<Long>? {
        return object : ItemDetailsProvider<Long> {
            override fun create(): ItemDetails<Long> {
                return object : ItemDetails<Long>() {
                    override fun getSelectionKey(): Long? {
                        return position.toLong()
                    }
//                    override fun inDragRegion(e: MotionEvent): Boolean {
//                        return true
//                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        swipeRefreshLayout.setOnRefreshListener {
            loadDelay()
        }

        listableManager.apply {
            setAnimationEnable(true, object : ListableManager.ItemLoadAnimators {
                override fun animators(v: View): Array<Animator> {
                    val sY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f, 1f).apply {
                        duration = 350
                        interpolator = DecelerateInterpolator()
                    }
                    val sX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f, 1f).apply {
                        duration = 350
                        interpolator = DecelerateInterpolator()
                    }
                    val aa = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
                    return arrayOf(sY, sX, aa)
                }
            })
            setAnimationFirstOnly(false)
            setHeaderWithEmptyEnable(true)
            setFooterWithEmptyEnable(true)
            setSelectionStorageStrategy(StorageStrategy.createLongStorage())
            setSelectionKeyProvider(object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
                override fun getKey(position: Int): Long {
                    return position.toLong()
                }

                override fun getPosition(key: Long): Int {
                    return key.toInt()
                }
            })

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
            setItemDragFlags(ItemTouchHelper.DOWN)
            setItemSwipeListener(object : OnItemSwipeListener {
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
                    println("swipe on $dX x $dY  ($isCurrentlyActive) ${dX / (canvas?.width ?: 1)}")
                    canvas?.drawColor(ContextCompat.getColor(this@ActivityCard, R.color.colorAccent))
                    if (isCurrentlyActive) {
                        val p = Paint().apply {
                            style = Paint.Style.FILL
                            color = Color.BLACK
                            strokeWidth = 12f
                            textSize = 100f
                        }
                        if (abs(dX) / (canvas?.width ?: 1) >= 0.7f) {
                            canvas?.drawText("loosen to Del", 50f, 200f, p)
                        } else {
                            canvas?.drawText("Del", 50f, 200f, p)
                        }
                    }
                }
            })
            setItemSwipeFlags(ItemTouchHelper.START)
            initListable(rv_cards, withDefaultSelectionTracker = true)
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            getSelectionTracker()?.apply {
                addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        println(">>> $selection")
                        if (selection.size() > 0) {
                            if (actionMode == null) {
                                actionMode = startSupportActionMode(this@ActivityCard)
                            }
                            actionMode?.title = selection.size().toString()
                        } else {
                            if (actionMode != null) {
                                actionMode?.finish()
                            }
                        }
                    }
                })
            }
            setItemDragEnable(false) // 先禁止拖拽模式，启用 selectionTracker 模式
            setItemSwipeEnable(false)
            addChildClickViewIds(R.id.btn_test, R.id.btn_del)
            setItemChildClickListener(object : OnItemChildClickListener {
                override fun onItemChildClick(view: View, position: Int) {
                    when (view.id) {
                        R.id.btn_test -> {
                            println("Click Item child >>>> $position")
                            notifyItemChanged(position, "test")
                        }
                        R.id.btn_del -> {
                            println("Click Delete Item child >>>> $position")
                            listableManager.removeData(position)
                        }
                    }
                }
            })
            addChildLongClickViewIds(R.id.btn_del)
            setItemChildLongClickListener(object : OnItemChildLongClickListener {
                override fun onItemChildLongClick(view: View, position: Int): Boolean {
                    when (view.id) {
                        R.id.btn_del -> {
                            println("Long Click Delete Item child >> $position")
                            val d = listableManager.getData(position)
                            listableManager.removeItem(d)
                            return true
                        }
                    }
                    return false
                }
            })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dragged, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drag -> {
                listableManager.apply {
                    setSelectionTrackerEnable(!getSelectionTrackerEnable())
                    setItemDragEnable(!getSelectionTrackerEnable())
                    setItemSwipeEnable(!getSelectionTrackerEnable())
                    if (getSelectionTrackerEnable()) {
                        toast("现在多选模式")
                    } else {
                        toast("现在拖拽滑动模式")
                    }

                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_del) {
            listableManager.getSelectionTracker()?.apply {
                val delItems = arrayListOf<String>()
                // 必须要注意这里，头部元素会占用列表一个席位，所以必须手动调整 selectionTracker 中记录的 position
                val fix = if (listableManager.hasHeaderLayout()) -1 else 0
                for (x in selection) {
                    val p = listableManager.getSelectionKeyProvider()!!.getPosition(x) + fix
                    delItems.add(listableManager.getData(p))
                }
                clearSelection()
                for (i in delItems) {
                    listableManager.removeItem(i)
                }
                return true
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val inf = mode?.menuInflater
        inf?.inflate(R.menu.card_actions, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        listableManager.getSelectionTracker()?.clearSelection()
        this.actionMode = null
    }
}