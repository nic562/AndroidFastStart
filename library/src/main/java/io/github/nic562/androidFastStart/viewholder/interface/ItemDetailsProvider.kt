package io.github.nic562.androidFastStart.viewholder.`interface`

import io.github.nic562.androidFastStart.viewholder.ItemDetails

/**
 * Created by Nic on 2019/12/25.
 */
interface ItemDetailsProvider<K> {
    fun create(): ItemDetails<K>
}