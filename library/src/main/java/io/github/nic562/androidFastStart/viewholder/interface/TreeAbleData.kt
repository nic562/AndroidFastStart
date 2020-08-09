package io.github.nic562.androidFastStart.viewholder.`interface`

/**
 * 树数据
 * Created by Nic on 2020/8/9.
 *
 * @property tree 树叉号，不同值标识不同级别
 * @property children 子树叉
 * @property footer 位于本树的子树底部，是不可收起的树
 */
interface TreeAbleData {
    val tree: Int
    val children: MutableList<TreeAbleData>?
    val footer: TreeAbleData?
}