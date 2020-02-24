package io.github.nic562.androidFastStart

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import java.util.LinkedHashSet

/**
 *
 * @param K 为SelectionTracker 需要跟中的特点标识
 * Selection key type. Built in support is provided for String, Long, and Parcelable
 *           types. Use the respective factory method to create a StorageStrategy instance
 *           appropriate to the desired type.
 * @see androidx.recyclerview.selection.StorageStrategy
 *
 * Created by Nic on 2020/2/22.
 */
interface ListableManager<K> {

    /**
     * 额外功能支持
     * @property WITH_UP_FETCH 提供该上拉加载事件支持。只有提供该标识，setUpFetch* 才生效
     * @property WITH_DRAGGABLE 提供拖拽、侧滑事件支持。只有提供该标识 setItemDrag* 和 setItemSwipe* 才生效。另外如果列表还实现了SelectionTracker支持，请一定要处理事件的生效与关闭，否则会出现冲突。
     * @property WITH_DRAGGABLE_AND_UP_FETCH 上述事件支持均生效
     * @property NORMAL 上述事件支持均不生效
     */
    enum class EXT {
        WITH_UP_FETCH, WITH_DRAGGABLE, WITH_DRAGGABLE_AND_UP_FETCH, NORMAL
    }

    fun getPage(): Int
    fun increasePage(): Int

    fun getTotalCount(): Int

    fun getLimit(): Int

    fun setLimit(limit: Int)

    fun getCanLoadMore(): Boolean

    fun setCanLoadMore(b: Boolean)

    fun getAutoLoadMore(): Boolean

    fun setAutoLoadMore(b: Boolean)

    fun getSelectionTrackerEnable(): Boolean
    fun setSelectionTrackerEnable(b: Boolean)
    fun setSelectionTracker(selectionTracker: SelectionTracker<K>)

    fun getSelectionTracker(): SelectionTracker<K>?

    fun getSelectionKeyProvider(): ItemKeyProvider<K>?

    fun setSelectionKeyProvider(selectionKeyProvider: ItemKeyProvider<K>)

    fun setSelectionStorageStrategy(storageStrategy: StorageStrategy<K>)

    fun getStorageStrategy(): StorageStrategy<K>?

    fun getItemDetailsProvider(): ItemDetailsProvider<K>?

    fun getItemDetails(view: View): ItemDetails<K>?

    fun setViewContainer(recyclerView: RecyclerView)

    fun myLoadData(page: Int, limit: Int)

    fun clearData()

    fun reloadData()

    fun notifyDataSetChanged()

    fun removeData(position: Int)

    fun setEmptyView(@LayoutRes resID: Int)

    fun setEmptyView(v: View)

    fun addHeaderView(@LayoutRes resID: Int, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): View

    fun addHeaderView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL)

    fun setHeaderView(@LayoutRes resID: Int, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): View

    fun setHeaderView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL)

    fun hasHeaderLayout(): Boolean

    fun removeHeaderView(header: View)

    fun removeAllHeaderView()

    fun addFooterView(@LayoutRes resID: Int, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): View

    fun addFooterView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL)

    fun setFooterView(@LayoutRes resID: Int, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): View

    fun setFooterView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL)

    fun hasFooterLayout(): Boolean

    fun removeFooterView(footer: View)

    fun removeAllFooterView()

    /**
     * 当显示空布局时，是否显示 头布局
     */
    fun setHeaderWithEmptyEnable(boolean: Boolean)

    /**
     * 当显示空布局时，是否显示 脚布局
     */
    fun setFooterWithEmptyEnable(boolean: Boolean)

    fun setItemClickListener(listener: OnItemClickListener?)

    fun setItemLongClickListener(listener: OnItemLongClickListener?)

    fun setItemChildClickListener(listener: OnItemChildClickListener?)

    fun setItemChildLongClickListener(listener: OnItemChildLongClickListener?)

    fun addChildClickViewIds(@IdRes vararg viewIds: Int)

    fun getChildLongClickViewIds(): LinkedHashSet<Int>

    fun setAnimationEnable(boolean: Boolean)

    fun setItemDragFlags(flag: Int)
    fun setItemDragEnable(boolean: Boolean)
    fun setItemDragListener(listener: OnItemDragListener?)

    fun setItemSwipeFlags(flag: Int)
    fun setItemSwipeEnable(boolean: Boolean)
    fun setItemSwipeListener(listener: OnItemSwipeListener?)

    fun setUpFetchListener(listener: OnUpFetchListener?)
    fun setUpFetchEnable(boolean: Boolean)
    fun setUpFetching(boolean: Boolean)
}