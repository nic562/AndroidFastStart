package io.github.nic562.androidFastStart.viewholder.`interface`

import android.view.View
import android.view.ViewGroup

/**
 * 树 UI 结构，事件触发
 * @property tree 树叉号，不同值标识不同级别
 * @property layoutResID 用于实例化 ViewHolder (ViewHelper)
 * @property childClickViewIds 根据layoutResID实例化后本view中可触发单击事件的view ID
 * @property childLongClickViewIds 根据layoutResID实例化后本view中可触发长按事件的view ID
 * @property onClick 本树整View的点击事件
 * @property onLongClick 本树整View的长按事件
 * @property onChildClick 本树中子View的点击事件
 * @property onChildLongClick 本树中子View的长按事件
 *
 * Created by Nic on 2020/2/21.
 */
interface TreeAble {
    val tree: Int
    val layoutResID: Int
    fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData)

    /**
     * 局部更新请重写该方法
     */
    fun <K> convert(helper: ViewHelper<K>, data: TreeAbleData, payloads: List<Any>) {}

    fun <K> onViewHolderCreated(helper: ViewHelper<K>, viewType: Int) {}

    fun onCreateItemViewWrapper(parent: ViewGroup, viewType: Int): ViewGroup? {
        return null
    }

    val childClickViewIds: IntArray?
    val childLongClickViewIds: IntArray?

    val onClick: OnClick?
    val onLongClick: OnLongClick?
    val onChildClick: OnChildClick?
    val onChildLongClick: OnChildLongClick?

    interface OnClick {
        fun <K> onClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int)
    }

    interface OnLongClick {
        fun <K> onLongClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int): Boolean
    }

    interface OnChildClick {
        fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int)
    }

    interface OnChildLongClick {
        fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAbleData, position: Int): Boolean
    }
}