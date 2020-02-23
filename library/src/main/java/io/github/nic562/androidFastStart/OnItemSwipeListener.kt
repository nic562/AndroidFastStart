package io.github.nic562.androidFastStart

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView

/**
 * 响应侧滑事件
 * Created by Nic on 2020/2/23.
 */
interface OnItemSwipeListener {
    fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int)

    fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int)

    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int)

    fun onItemSwipeMoving(canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, isCurrentlyActive: Boolean)
}