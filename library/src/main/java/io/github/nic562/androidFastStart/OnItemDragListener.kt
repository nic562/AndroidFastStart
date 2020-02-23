package io.github.nic562.androidFastStart

import androidx.recyclerview.widget.RecyclerView

/**
 * 响应拖拽事件
 * Created by Nic on 2020/2/23.
 */
interface OnItemDragListener {
    fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int)

    fun onItemDragMoving(source: RecyclerView.ViewHolder?, from: Int, target: RecyclerView.ViewHolder?, to: Int)

    fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int)
}