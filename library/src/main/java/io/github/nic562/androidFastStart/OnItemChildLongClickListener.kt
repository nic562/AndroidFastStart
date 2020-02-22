package io.github.nic562.androidFastStart

import android.view.View

/**
 * Created by Nic on 2020/2/22.
 */
interface OnItemChildLongClickListener {
    fun onItemChildLongClick(view: View, position: Int): Boolean
}