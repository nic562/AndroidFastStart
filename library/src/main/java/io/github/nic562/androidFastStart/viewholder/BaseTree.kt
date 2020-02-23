package io.github.nic562.androidFastStart.viewholder

import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble

/**
 * Created by Nic on 2020/2/23.
 */
abstract class BaseTree(childList: MutableList<TreeAble>? = null): TreeAble {
    override val expandable: Boolean = false
    override val children: MutableList<TreeAble>? = childList
    override val footer: TreeAble? = null
    override val childClickViewIds: IntArray? = null
    override val childLongClickViewIds: IntArray? = null
    override val onClick: TreeAble.OnClick? = null
    override val onLongClick: TreeAble.OnLongClick? = null
    override val onChildClick: TreeAble.OnChildClick? = null
    override val onChildLongClick: TreeAble.OnChildLongClick? = null
}