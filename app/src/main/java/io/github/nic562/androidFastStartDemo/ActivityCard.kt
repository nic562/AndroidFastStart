package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.*
import com.google.android.material.card.MaterialCardView
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingListable
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2019/12/25.
 */
class ActivityCard : ActivityBase(), SomethingListable<String, Long>, ActionMode.Callback {

    private var selectionTracker: SelectionTracker<Long>? = null

    private var actionMode: ActionMode? = null
    private val selectionKeyProvider = object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return position.toLong()
        }

        override fun getPosition(key: Long): Int {
            return key.toInt()
        }
    }

    override val listableManager: SomethingListable.ListableManager<String, Long> by lazy {
        object : SomethingListable.ListableManager<String, Long>() {
            override val listItemLayoutID = R.layout.layout_item_card

            override fun itemConvert(helper: ViewHelper<Long>, item: String) {
                with(helper) {
                    hSetText(R.id.tv_title, item)
                    hSetText(R.id.tv_subtitle, item)
                    val c = hGetView<MaterialCardView>(R.id.card)
                    val details = helper.getItemDetails()
                    if (details != null && selectionTracker != null) {
                        c.isChecked = selectionTracker!!.isSelected(details.selectionKey)
                    } else {
                        c.isChecked = false
                    }
                }
            }

            override fun loadData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<String>) {
                val l = arrayListOf<String>()
                for (i in 1..limit) {
                    l.add("$page ${page * limit + i}")
                }
                dataCallback.onLoadData(l, 50)
            }

            override fun getItemDetailsProvider(): ItemDetailsProvider<Long>? {
                return object : ItemDetailsProvider<Long> {
                    override fun create(): ItemDetails<Long> {
                        return object : ItemDetails<Long>() {
                            override fun getSelectionKey(): Long? {
                                return position.toLong()
                            }

                            override fun inDragRegion(e: MotionEvent): Boolean {
                                return true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        initListable(rv_cards)
        selectionTracker = SelectionTracker.Builder<Long>(
                "my_selection",
                rv_cards,
                selectionKeyProvider,
                object : ItemDetailsLookup<Long>() {
                    @Suppress("UNCHECKED_CAST")
                    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
                        val v = rv_cards.findChildViewUnder(e.x, e.y)
                        if (v != null) {
                            val viewHolder = rv_cards.getChildViewHolder(v)
                            return (viewHolder as ViewHelper<Long>).getItemDetails()
                        }
                        return null
                    }
                },
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything<Long>()).build()

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                if (selectionTracker?.selection?.size() ?: 0 > 0) {
                    if (actionMode == null) {
                        actionMode = startSupportActionMode(this@ActivityCard)
                    }
                    actionMode?.title = selectionTracker?.selection?.size().toString()
                } else {
                    if (actionMode != null) {
                        actionMode?.finish()
                    }
                }
            }
        })
        with(listableManager) {
            setAnimationEnable(true)
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
            val delItems = arrayListOf<String>()
            for (x in selectionTracker?.selection ?: return true) {
                val p = selectionKeyProvider.getPosition(x)
                delItems.add(listableManager.dataList[p])
            }
            listableManager.dataList.removeAll(delItems)
            selectionTracker?.clearSelection()
            listableManager.notifyDataSetChanged()
            return true
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
        selectionTracker?.clearSelection()
        this.actionMode = null
    }
}