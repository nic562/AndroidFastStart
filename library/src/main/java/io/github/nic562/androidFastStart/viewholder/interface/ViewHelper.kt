package io.github.nic562.androidFastStart.viewholder.`interface`

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import io.github.nic562.androidFastStart.viewholder.ItemDetails

/**
 * Created by Nic on 2019/12/25.
 */
interface ViewHelper<K> {
    fun <T : View> hGetView(@IdRes viewId: Int): T
    fun hSetText(@IdRes viewId: Int, value: CharSequence?): ViewHelper<K>
    fun hSetText(@IdRes viewId: Int, @StringRes strId: Int): ViewHelper<K>?
    fun hSetTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper<K>
    fun hSetTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHelper<K>
    fun hSetImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHelper<K>
    fun hSetImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHelper<K>?
    fun hSetImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHelper<K>?
    fun hSetBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper<K>?
    fun hSetBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): ViewHelper<K>?
    fun hSetVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHelper<K>
    fun hSetGone(@IdRes viewId: Int, isGone: Boolean): ViewHelper<K>
    fun createItemDetails(): ItemDetails<K>?
    fun getItemDetails(): ItemDetails<K>?
}