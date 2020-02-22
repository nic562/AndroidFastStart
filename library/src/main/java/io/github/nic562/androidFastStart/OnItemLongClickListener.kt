package io.github.nic562.androidFastStart

import android.view.View

/**
 * Created by Nic on 2020/2/22.
 */
interface OnItemLongClickListener {
    fun onItemLongClick(view: View, position: Int): Boolean
}