package io.github.nic562.androidFastStartDemo

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2019/12/25.
 */
class ActivityCardRaw : ActivityBase(), ActionMode.Callback {

    private var selectionTracker: SelectionTracker<Long>? = null
    private val selectionKeyProvider = object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return position.toLong()
        }

        override fun getPosition(key: Long): Int {
            return key.toInt()
        }
    }

    private var actionMode: ActionMode? = null

    private class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val itemDetails = object : ItemDetails<Long>() {
            override fun getSelectionKey(): Long? {
                return position.toLong()
            }

            override fun inDragRegion(e: MotionEvent): Boolean {
                return true
            }
        }

        val title1: TextView
        val title2: TextView
        val cardView: MaterialCardView

        init {
            title1 = v.findViewById(R.id.tv_title)
            title2 = v.findViewById(R.id.tv_subtitle)
            cardView = v.findViewById(R.id.card)
        }
    }

    private val adapter = object : RecyclerView.Adapter<MyViewHolder>() {
        val items = arrayListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val l = LayoutInflater.from(parent.context)
            val v = l.inflate(R.layout.layout_item_card, parent, false)
            return MyViewHolder(v)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.itemDetails.position = position
            val s = items[position]
            holder.title1.text = s
            holder.title2.text = s
            if (selectionTracker != null) {
                holder.cardView.isChecked = selectionTracker!!.isSelected(holder.itemDetails.selectionKey)
            } else {
                holder.cardView.isChecked = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        val ss = arrayListOf<String>()
        for (i in 0..20) {
            ss.add("$i **********")
        }
        adapter.items.addAll(ss)

        rv_cards.adapter = adapter
        rv_cards.layoutManager = LinearLayoutManager(this)

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
                            return (viewHolder as MyViewHolder).itemDetails
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
                        actionMode = startSupportActionMode(this@ActivityCardRaw)
                    }
                    actionMode?.title = selectionTracker?.selection?.size().toString()
                } else {
                    if (actionMode != null) {
                        actionMode?.finish()
                    }
                }
            }
        })
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_del) {
            val delItems = arrayListOf<String>()
            for (x in selectionTracker?.selection ?: return true) {
                val p = selectionKeyProvider.getPosition(x)
                delItems.add(adapter.items[p])
            }
            adapter.items.removeAll(delItems)
            selectionTracker?.clearSelection()
            adapter.notifyDataSetChanged()
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