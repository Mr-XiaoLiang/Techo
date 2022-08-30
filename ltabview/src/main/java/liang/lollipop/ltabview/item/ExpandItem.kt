package liang.lollipop.ltabview.item

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import liang.lollipop.ltabview.R
import liang.lollipop.ltabview.drawable.ExpandDrawable
import liang.lollipop.ltabview.utils.TintUtil


/**
 * @date: 2019-04-19 22:16
 * @author: lollipop
 * 可以横向展开的Item
 */
class ExpandItem(context: Context, attr: AttributeSet?,
                 defStyleAttr: Int, defStyleRes: Int): IconItem(context, attr, defStyleAttr, defStyleRes){

    constructor(context: Context, attr: AttributeSet?,
                defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    override val miniSize: Int
        get() {
            return iconSize
        }

    private val expandDrawable = ExpandDrawable()

    override var iconSize = 0
        set(value) {
            field = value
            expandDrawable.minWidth = miniSize
        }

    private val nameView: TextView

    private val iconView: ImageView

    private val bodyView: View

    override var selectedIconColor = Color.BLACK

    override var unselectedIconColor = Color.BLACK

    var expandColor
        set(value) {
            expandDrawable.color = value
        }
        get() = expandDrawable.color

    override var textColor: Int
        set(value) {
            nameView.setTextColor(value)
        }
        get() {
            return nameView.textColors.defaultColor
        }

    override var icon: Drawable
        set(value) {
            iconView.setImageDrawable(value)
        }
        get() {
            return iconView.drawable
        }

    override var text: CharSequence
        set(value) {
            nameView.text = value
        }
        get() = nameView.text

    init {
        bodyView = LayoutInflater.from(context).inflate(R.layout.item_expand, this, false)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_VERTICAL
        addView(bodyView, layoutParams)
        bodyView.background = expandDrawable
        nameView = findViewById(R.id.nameView)
        iconView = findViewById(R.id.iconView)
        bindIconClickListener(iconView)
    }

    override fun schedule(progress: Float) {
        expandDrawable.progress = progress
        val iconColor = TintUtil.colorTransition(unselectedIconColor, selectedIconColor, progress)
        TintUtil.tintDrawable(icon).setColor(iconColor).tint()
        nameView.alpha = progress
        nameView.translationX = (progress - 1) * nameView.measuredWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        iconSize = iconView.measuredWidth + bodyView.paddingLeft + bodyView.paddingRight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val lp = child.layoutParams as LayoutParams
            val childLeft = paddingLeft + lp.leftMargin

            var childTop = (height - child.measuredHeight) / 2
            if (childTop < paddingTop + lp.topMargin) {
                childTop = paddingTop + lp.topMargin
            }
            val childRight = width - paddingRight - lp.rightMargin
            var childBottom = childTop + child.measuredHeight
            if (height - childBottom < paddingBottom + lp.bottomMargin) {
                childBottom = height - paddingBottom - lp.bottomMargin
            }
            child.layout(childLeft, childTop, childRight, childBottom)
        }
        setTouchDelegate()
    }

    private fun setTouchDelegate() {
        val view = iconView
        val parent = view.parent as? View ?: return
        val bounds = Rect()
        view.isEnabled = true
        view.getHitRect(bounds)
        log("setTouchDelegate-from: $bounds")
        bounds.left -= parent.paddingLeft
        bounds.top -= view.top
        bounds.right += parent.paddingRight
        bounds.bottom += view.bottom
        log("setTouchDelegate-to: $bounds")
        parent.touchDelegate = TouchDelegate(bounds, view)
    }

    private fun log(value: String) {
        Log.d("Lollipop", "ExpandItem: $value")
    }

}