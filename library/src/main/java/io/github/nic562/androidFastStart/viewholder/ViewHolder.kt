package io.github.nic562.androidFastStart.viewholder

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper

/**
 * Created by Nic on 2019/12/25.
 */
internal class ViewHolder<K>(view: View) : BaseViewHolder(view), ViewHelper<K> {
    private var itemDetails: ItemDetails<K>? = null
    var itemDetailsProvider: ItemDetailsProvider<K>? = null
    override fun hGetItemView(): View {
        return itemView
    }

    override fun getItemDetails(): ItemDetails<K>? {
        if (itemDetails == null) {
            itemDetails = createItemDetails()
        }
        return itemDetails
    }

    override fun createItemDetails(): ItemDetails<K>? {
        return itemDetailsProvider?.create()
    }

    override fun <T : View> hGetView(@IdRes viewId: Int): T {
        return getView(viewId)
    }

    override fun hSetText(@IdRes viewId: Int, value: CharSequence?): ViewHelper<K> {
        setText(viewId, value)
        return this
    }

    override fun hSetText(@IdRes viewId: Int, @StringRes strId: Int): ViewHelper<K> {
        setText(viewId, strId)
        return this
    }

    override fun hSetTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper<K> {
        setTextColor(viewId, color)
        return this
    }

    override fun hSetTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHelper<K> {
        setTextColorRes(viewId, colorRes)
        return this
    }

    override fun hSetImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHelper<K> {
        setImageResource(viewId, imageResId)
        return this
    }

    override fun hSetImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHelper<K> {
        setImageDrawable(viewId, drawable)
        return this
    }

    override fun hSetImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHelper<K> {
        setImageBitmap(viewId, bitmap)
        return this
    }

    override fun hSetBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHelper<K> {
        setBackgroundColor(viewId, color)
        return this
    }

    override fun hSetBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): ViewHelper<K> {
        setBackgroundResource(viewId, backgroundRes)
        return this
    }

    override fun hSetVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHelper<K> {
        setVisible(viewId, isVisible)
        return this
    }

    override fun hSetGone(@IdRes viewId: Int, isGone: Boolean): ViewHelper<K> {
        setGone(viewId, isGone)
        return this
    }
}