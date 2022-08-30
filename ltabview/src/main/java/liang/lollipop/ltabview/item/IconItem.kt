package liang.lollipop.ltabview.item

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import liang.lollipop.ltabview.LTabItem

/**
 * @date: 2019-04-20 15:00
 * @author: lollipop
 *
 */
abstract class IconItem(context: Context, attr: AttributeSet?,
                      defStyleAttr: Int, defStyleRes: Int): FrameLayout(context, attr, defStyleAttr, defStyleRes), LTabItem {

    constructor(context: Context, attr: AttributeSet?,
                defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    protected abstract var iconSize: Int

    abstract var icon: Drawable

    abstract var text: CharSequence

    abstract var textColor: Int

    abstract var selectedIconColor: Int

    abstract var unselectedIconColor: Int

    private var tabClickListener: TabClickListener? = null

    fun setIcon(resId: Int) {
        icon = resources.getDrawable(resId, context.theme).mutate()
    }

    fun setIcon(bitmap: Bitmap) {
        icon = BitmapDrawable(resources, bitmap)
    }

    fun setSelectedIconColorById(id: Int) {
        selectedIconColor = ContextCompat.getColor(context, id)
    }

    fun setUnselectedIconColorById(id: Int) {
        unselectedIconColor = ContextCompat.getColor(context, id)
    }

    fun setTextColorById(id: Int) {
        textColor = ContextCompat.getColor(context, id)
    }

    private class TabClickListener(private val callback: (LTabItem) -> Unit) {
        fun onClick(item: LTabItem) {
            callback(item)
        }
    }

    override fun onTabClick(listener: (LTabItem) -> Unit) {
        this.tabClickListener = TabClickListener(listener)
    }

    protected fun bindIconClickListener(iconView: View) {
        iconView.setOnClickListener {
            tabClickListener?.onClick(this)
        }
    }

}