package io.github.nic562.androidFastStart

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import org.jetbrains.annotations.NotNull

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

    interface ItemLoadAnimators {
        fun animators(@NotNull v: View): Array<Animator>
    }

    interface LoadMoreView {
        fun getRootView(parent: ViewGroup): View
        fun getLoadingView(viewHolder: RecyclerView.ViewHolder): View
        fun getLoadCompleteView(viewHolder: RecyclerView.ViewHolder): View
        fun getLoadEndView(viewHolder: RecyclerView.ViewHolder): View
        fun getLoadFailView(viewHolder: RecyclerView.ViewHolder): View
    }

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

    /**
     * 获取当前页码
     * 一般情况下，不需直接 setPage 是由于防止在加载失败时的页码与当前数据不对应。
     * 后续接口的实现，在加载数据成功时才能修改页码
     */
    fun getPage(): Int

    /**
     * 建议在特殊情况下，需要手动跳页才使用
     */
    fun setPage(page: Int)

    fun getTotalCount(): Int

    fun getLimit(): Int

    fun setLimit(limit: Int)

    fun setLoadMoreView(loadMoreView: LoadMoreView)

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

    /**
     * 清理数据
     */
    fun clearData()

    /**
     * 重新加载数据
     */
    fun reloadData()

    /**
     * 加载指定页数据，加载成功则修改当前页码
     */
    fun loadData(page: Int)

    fun notifyDataSetChanged()

    /**
     * 通知局部更新事件。
     *
     * @param ignoredHeaderLayout 默认为 true，表示 响应更新事件的数据所在 position 不从头部元素开始计算。这个在头部元素为数据列表之外其他View的时候需要注意。
     */
    fun notifyItemChanged(position: Int, @Nullable payload: Any, ignoredHeaderLayout: Boolean = true)

    /**
     * 使用时必须要小心，不能按位置循环执行该删除方法，因为每删除一个数据后，position均会改变
     */
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

    fun addChildLongClickViewIds(@IdRes vararg viewIds: Int)

    fun setAnimationEnable(boolean: Boolean, itemAnimators: ItemLoadAnimators? = null)
    fun setAnimationFirstOnly(boolean: Boolean)

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