package com.lollipop.widget

import android.animation.Animator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.util.onClick
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

    private val lastAnimation = ArrayList<Animator>()

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    var isReplaceMode = false

    init {
        addView(defaultStatusView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(checkedStatusView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        onClick {
            onClick()
        }
        if (attributeSet != null) {
            val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.CheckableView)
            val defDrawable = typeArray.getDrawable(R.styleable.CheckableView_defaultDrawable)
            val cheDrawable = typeArray.getDrawable(R.styleable.CheckableView_checkedDrawable)
            if (defDrawable != null && cheDrawable != null) {
                setDefaultDrawable(defDrawable)
                setCheckedDrawable(cheDrawable)
            } else {
                var defStyle = CheckStyle.CIRCULAR
                val index = typeArray.getInt(
                    R.styleable.CheckableView_checkStyle,
                    CheckStyle.CIRCULAR.ordinal
                )
                if (index in CheckStyle.entries.indices) {
                    defStyle = CheckStyle.entries[index]
                }
                setStyle(defStyle)
            }

            val stateList = typeArray.getColorStateList(R.styleable.CheckableView_iconTint)
            if (stateList != null) {
                tint(stateList)
            } else {
                val color = typeArray.getColor(R.styleable.CheckableView_iconTint, 0)
                if (color != 0) {
                    tint(ColorStateList.valueOf(color))
                }
            }

            isReplaceMode = typeArray.getBoolean(R.styleable.CheckableView_replaceMode, false)

            isChecked = typeArray.getBoolean(R.styleable.CheckableView_android_checked, false)
            typeArray.recycle()
        } else {
            setStyle(CheckStyle.SQUARE)
            checkStatus()
        }
    }

    fun tint(colorStateList: ColorStateList?) {
        defaultStatusView.imageTintList = colorStateList
        checkedStatusView.imageTintList = colorStateList
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
        isReplaceMode = false
        tint(null)
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
        lastAnimation.forEach { it.cancel() }
        lastAnimation.clear()
        if (!animation || isInEditMode) {
            checkedStatusView.isInvisible = !isChecked
            if (isReplaceMode) {
                defaultStatusView.isInvisible = isChecked
            } else {
                defaultStatusView.isVisible = true
            }
            return
        }
        lastAnimation.add(doCircularRevealAnimation(checkedStatusView, isChecked))
        if (isReplaceMode) {
            lastAnimation.add(doCircularRevealAnimation(defaultStatusView, !isChecked))
        } else {
            defaultStatusView.isVisible = true
        }
    }

    private fun doCircularRevealAnimation(view: View, isOpen: Boolean): Animator {
        val w = (width - paddingLeft - paddingRight) * 0.5
        val h = (height - paddingTop - paddingBottom) * 0.5
        val radius = sqrt(w * w + h * h).toFloat()
        val newAnimator = ViewAnimationUtils.createCircularReveal(
            view,
            w.toInt() + paddingLeft,
            h.toInt() + paddingTop,
            if (isOpen) {
                0F
            } else {
                radius
            },
            if (isOpen) {
                radius
            } else {
                0F
            }
        )
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
        return newAnimator
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