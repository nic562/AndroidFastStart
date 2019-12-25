package io.github.nic562.androidFastStart.viewholder

import androidx.recyclerview.selection.ItemDetailsLookup

/**
 * Created by Nic on 2019/12/25.
 */
abstract class ItemDetails<K> : ItemDetailsLookup.ItemDetails<K>() {
    private var p = 0
    fun setPosition(position: Int) {
        p = position
    }

    override fun getPosition(): Int {
        return p
    }
}