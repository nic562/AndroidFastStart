package io.github.nic562.androidFastStart.viewholder.`interface`

import android.view.View

/**
 * @property tree 树叉号，不同值标识不同级别
 * @property layoutResID 用于实例化 ViewHolder (ViewHelper)
 * @property children 子树叉
 * @property expandable 标识展示时是否可以收起子树叉
 * @property footer 位于本树的子树底部，是不可收起的树
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
    val children: MutableList<TreeAble>?
    fun <K> convert(helper: ViewHelper<K>)

    /**
     * 局部更新请重写该方法
     */
    fun <K> convert(helper: ViewHelper<K>, payloads: List<Any>) {}

    val expandable: Boolean
    val footer: TreeAble?

    val childClickViewIds: IntArray?
    val childLongClickViewIds: IntArray?

    val onClick: OnClick?
    val onLongClick: OnLongClick?
    val onChildClick: OnChildClick?
    val onChildLongClick: OnChildLongClick?

    interface OnClick {
        fun <K> onClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int)
    }

    interface OnLongClick {
        fun <K> onLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean
    }

    interface OnChildClick {
        fun <K> onChildClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int)
    }

    interface OnChildLongClick {
        fun <K> onChildLongClick(helper: ViewHelper<K>, view: View, data: TreeAble, position: Int): Boolean
    }
}