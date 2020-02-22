package io.github.nic562.androidFastStart.viewholder.`interface`

/**
 * Created by Nic on 2020/2/21.
 */
interface TreeAble {
    val tree: Int
    val layoutResID: Int
    val children: MutableList<TreeAble>?
    fun <K> convert(helper: ViewHelper<K>)
}