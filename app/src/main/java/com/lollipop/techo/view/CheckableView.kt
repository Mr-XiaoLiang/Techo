package com.lollipop.techo.view

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.core.view.isInvisible
import com.lollipop.base.util.onClick
import com.lollipop.techo.R
import kotlin.math.sqrt

/**
 * @author lollipop
 * @date 2021/11/11 22:39
 */
class CheckableView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : FrameLayout(context, attributeSet, style), Checkable {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private val defaultStatusView by lazy {
        AppCompatImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
        }
    }
    private val checkedStatusView by lazy {
        AppCompatImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private var isCheckStatus = false

    private var lastAnimation: Animator? = null

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        addView(defaultStatusView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(checkedStatusView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        onClick {
            onClick()
        }
        checkStatus()
        setStyle(CheckStyle.SQUARE)
        attributeSet?.let { attrs ->
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CheckableView)
            var defStyle = CheckStyle.CIRCULAR
            val index = typeArray.getInt(
                R.styleable.CheckableView_checkStyle,
                CheckStyle.CIRCULAR.ordinal
            )
            if (index in CheckStyle.values().indices) {
                defStyle = CheckStyle.values()[index]
            }
            setStyle(defStyle)
            isChecked = typeArray.getBoolean(R.styleable.CheckableView_android_checked, false)
            typeArray.recycle()
        }
    }

    fun setDefaultDrawable(drawable: Drawable?) {
        defaultStatusView.setImageDrawable(drawable)
    }

    fun setCheckedDrawable(drawable: Drawable?) {
        checkedStatusView.setImageDrawable(drawable)
    }

    fun setDefaultDrawable(resId: Int) {
        defaultStatusView.setImageResource(resId)
    }

    fun setCheckedDrawable(resId: Int) {
        checkedStatusView.setImageResource(resId)
    }

    fun onCheckedChange(listener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = listener
    }

    fun setStyle(style: CheckStyle) {
        when (style) {
            CheckStyle.CIRCULAR -> {
                setDefaultDrawable(R.drawable.bg_checkbox_circular)
                setCheckedDrawable(R.drawable.fg_checkbox_circular)
            }
            CheckStyle.SQUARE -> {
                setDefaultDrawable(R.drawable.bg_checkbox_square)
                setCheckedDrawable(R.drawable.fg_checkbox_square)
            }
        }
    }

    override fun setChecked(checked: Boolean) {
        isCheckStatus = checked
        checkStatus()
        onCheckedCheng()
    }

    override fun isChecked(): Boolean {
        return isCheckStatus
    }

    override fun toggle() {
        isChecked = !isCheckStatus
        onCheckedCheng()
    }

    private fun onClick() {
        isCheckStatus = !isCheckStatus
        checkStatus(true)
        onCheckedCheng()
    }

    private fun checkStatus(animation: Boolean = false) {
        lastAnimation?.cancel()
        lastAnimation = null
        if (!animation) {
            checkedStatusView.isInvisible = !isChecked
            return
        }
        val w = (width - paddingLeft - paddingRight) * 0.5
        val h = (height - paddingTop - paddingBottom) * 0.5
        val radius = sqrt(w * w + h * h).toFloat()
        val newAnimator = ViewAnimationUtils.createCircularReveal(
            checkedStatusView,
            w.toInt() + paddingLeft,
            h.toInt() + paddingTop,
            if (isChecked) {
                0F
            } else {
                radius
            },
            if (isChecked) {
                radius
            } else {
                0F
            }
        )
        lastAnimation = newAnimator
        newAnimator.addListener(
            onStart = {
                checkedStatusView.isInvisible = false
            },
            onEnd = {
                if (!isChecked) {
                    checkedStatusView.isInvisible = true
                }
            }
        )
        newAnimator.duration = 150L
        newAnimator.interpolator = AccelerateInterpolator()
        newAnimator.start()
    }

    private fun onCheckedCheng() {
        this.onCheckedChangeListener?.onCheckedChange(this, isChecked)
    }

    enum class CheckStyle {
        CIRCULAR,
        SQUARE
    }

    fun interface OnCheckedChangeListener {
        fun onCheckedChange(view: CheckableView, isChecked: Boolean)
    }

}