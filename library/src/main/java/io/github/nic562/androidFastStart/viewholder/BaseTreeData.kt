package io.github.nic562.androidFastStart.viewholder

import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAbleData

/**
 * Created by Nic on 2020/8/9.
 */
abstract class BaseTreeData : TreeAbleData {
    override val children: MutableList<TreeAbleData>? = null
    override val footer: TreeAbleData? = null
}