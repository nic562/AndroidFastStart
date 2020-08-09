package io.github.nic562.androidFastStart.viewholder

import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble

/**
 * Created by Nic on 2020/2/23.
 */
abstract class BaseTree: TreeAble {
    override val childClickViewIds: IntArray? = null
    override val childLongClickViewIds: IntArray? = null
    override val onClick: TreeAble.OnClick? = null
    override val onLongClick: TreeAble.OnLongClick? = null
    override val onChildClick: TreeAble.OnChildClick? = null
    override val onChildLongClick: TreeAble.OnChildLongClick? = null
}