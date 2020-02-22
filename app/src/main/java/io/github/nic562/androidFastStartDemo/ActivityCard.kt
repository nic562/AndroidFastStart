package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.*
import com.google.android.material.card.MaterialCardView
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.ListableManager
import io.github.nic562.androidFastStart.SomethingListable
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2019/12/25.
 */
class ActivityCard : ActivityBase(), SomethingListable<String, Long>, ActionMode.Callback {

    private var actionMode: ActionMode? = null
    private val dataList = mutableListOf<String>()
    override val listableManager: ListableManager<Long> by lazy {
        instanceListableManager(R.layout.layout_item_card, dataList)
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<String>) {
        val l = arrayListOf<String>()
        for (i in 1..limit) {
            l.add("$page ${page * limit + i}")
        }
        dataCallback.onLoadData(l, 50)
    }

    override fun listableItemConvert(helper: ViewHelper<Long>, item: String) {
        helper.apply {
            hSetText(R.id.tv_title, item)
            hSetText(R.id.tv_subtitle, item)
            val c = hGetView<MaterialCardView>(R.id.card)
            val details = helper.getItemDetails()
            val selectionTracker = listableManager.getSelectionTracker()
            if (details != null && selectionTracker != null) {
                c.isChecked = selectionTracker.isSelected(details.selectionKey)
            } else {
                c.isChecked = false
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

        listableManager.apply {
            setAnimationEnable(true)
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

            initListable(rv_cards, withDefaultSelectionTracker = true)
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            with(getSelectionTracker()!!) {
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
        }
    }

    override fun onStart() {
        super.onStart()
        listableManager.reloadData()
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_del) {
            with(listableManager.getSelectionTracker()!!) {
                val delItems = arrayListOf<String>()
                // 必须要注意这里，头部元素会占用列表一个席位，所以必须手动调整 selectionTracker 中记录的 position
                val fix = if(listableManager.hasHeaderLayout()) -1 else 0
                for (x in selection) {
                    val p = listableManager.getSelectionKeyProvider()!!.getPosition(x) + fix
                    delItems.add(dataList[p])
                }
                dataList.removeAll(delItems)
                clearSelection()
                listableManager.notifyDataSetChanged()
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