package io.github.nic562.androidFastStart

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper

/**
 * 实现基于RecyclerView的序列数据展示功能接口
 *
 * @param K 参考 ListableManager<K>
 * @see ListableManager<K>
 *
 * Created by Nic on 2020/02/22.
 */
interface SomethingListableBase<K> : SomethingWithContext {

    val listableManager: ListableManager<K>

    fun instanceListableManager(vararg args: Any): ListableManager<K>

    /**
     *  若使用到 SelectionTracker， 必须根据 K 值 重写该方法的实现，返回有效的 ItemDetailsProvider
     */
    fun getListableItemDetailsProvider(): ItemDetailsProvider<K>? {
        return null
    }

    /**
     * 可用于创建ItemViewHolder后的其他代码调用
     */
    fun onListableItemViewHolderCreated(helper: ViewHelper<K>, viewType: Int) {}

    /**
     * 获取多数情况下通用的垂直布局LayoutManager
     *
     */
    fun getDefaultListableLayoutManager(): RecyclerView.LayoutManager {
        val lm = LinearLayoutManager(getOwnerContext())
        lm.orientation = LinearLayoutManager.VERTICAL
        return lm
    }

    fun initListable(recyclerView: RecyclerView,
                     layoutManager: RecyclerView.LayoutManager = getDefaultListableLayoutManager(),
                     withDefaultSelectionTracker: Boolean = false) {
        recyclerView.layoutManager = layoutManager
        listableManager.setViewContainer(recyclerView)
        if (withDefaultSelectionTracker) {
            val selectionKeyProvider = listableManager.getSelectionKeyProvider()
                    ?: throw NotImplementedError("Using Default SelectionTracker please calling setSelectionKeyProvider(..) at first!")
            val storageStrategy = listableManager.getStorageStrategy()
                    ?: throw NotImplementedError("Using Default SelectionTracker please calling setSelectionStorageStrategy(..) at first!")
            val selectionTracker = SelectionTracker.Builder<K>(
                    "my_selection",
                    recyclerView,
                    selectionKeyProvider,
                    object : ItemDetailsLookup<K>() {
                        override fun getItemDetails(e: MotionEvent): ItemDetails<K>? {
                            if (!listableManager.getSelectionTrackerEnable()) {
                                // 目前找不到其他方法来禁用SelectionTracker，以此方法代为实现禁用，暂不知会否造成bug
                                return null
                            }
                            val v = recyclerView.findChildViewUnder(e.x, e.y)
                            if (v != null) {
                                return getItemDetails(v)
                            }
                            return null
                        }
                    },
                    storageStrategy
            ).withSelectionPredicate(SelectionPredicates.createSelectAnything<K>()).build()
            listableManager.setSelectionTracker(selectionTracker)
        }
    }

    fun getItemDetails(view: View): ItemDetails<K>?
}