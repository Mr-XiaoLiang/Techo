package com.lollipop.lqrdemo.creator.subpage.adjust

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.dp2px
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.DefaultWriterLayer
import com.lollipop.lqrdemo.view.CheckedButtonBackgroundDrawable
import com.lollipop.pigment.Pigment
import com.lollipop.widget.RoundBackgroundView

open class StyleAdjustContentFragment : BaseFragment() {

    companion object {

        const val RADIUS_MAX = 1F
        const val RADIUS_MIN = 0F

    }

    private val checkedMap = HashMap<Int, Boolean>()

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    protected fun changeRadius(
        info: Radius,
        value: Float,
        leftTop: Boolean,
        rightTop: Boolean,
        rightBottom: Boolean,
        leftBottom: Boolean,
    ) {
        if (leftTop) {
            info.leftTopX = value
            info.leftTopY = value
        }
        if (rightTop) {
            info.rightTopX = value
            info.rightTopY = value
        }
        if (rightBottom) {
            info.rightBottomX = value
            info.rightBottomY = value
        }
        if (leftBottom) {
            info.leftBottomX = value
            info.leftBottomY = value
        }
    }


    protected fun checkOrUpdateButton(btnView: View, iconView: ImageView, color: Int) {
        iconView.imageTintList = ColorStateList.valueOf(color)
        val background = btnView.background
        if (background is CheckedButtonBackgroundDrawable) {
            background.color = color
        } else {
            val drawable = CheckedButtonBackgroundDrawable()
            drawable.color = color
            btnView.background = drawable
        }
    }

    protected fun View.checkedToggle() {
        // 读取状态并反转
        val view = this
        view.isChecked = !view.isChecked
    }

    protected fun View.updateCheckedState() {
        // 走一遍方法，让状态更新
        val view = this
        view.isChecked = view.isChecked
    }


    protected var View.isChecked: Boolean
        get() {
            val view = this
            if (view.id == View.NO_ID) {
                return false
            }
            return checkedMap[view.id] ?: false
        }
        set(value) {
            val view = this
            if (view.id == View.NO_ID) {
                view.alpha = 0.5F
                return
            }
            checkedMap[view.id] = value
            view.alpha = if (value) {
                1F
            } else {
                0.5F
            }
        }

    protected fun notifyContentChanged() {
        callback?.notifyContentChanged()
    }

    open fun getWriterLayer(): Class<out BitMatrixWriterLayer> {
        return DefaultWriterLayer::class.java
    }

    override fun onResume() {
        super.onResume()
        notifyLayerChanged(getWriterLayer())
    }

    protected fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        callback?.notifyLayerChanged(layer)
    }

    protected fun createHistoryColorAdapter(
        onColorClick: (Int) -> Unit,
        onPaletteClick: () -> Unit,
        getPigment: () -> Pigment?,
    ): HistoryColorAdapter {
        return HistoryColorAdapter(onColorClick, onPaletteClick, getPigment)
    }

    protected class HistoryColorAdapter(
        private val onColorClick: (Int) -> Unit,
        private val onPaletteClick: () -> Unit,
        private val getPigment: () -> Pigment?,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_COLOR = 0
            private const val TYPE_PALETTE = 1
        }

        private val colorList = ArrayList<Int>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_PALETTE) {
                return PaletteHolder.create(parent, onPaletteClick)
            }
            return ColorHolder.crate(parent, ::onItemClick)
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= colorList.size) {
                return
            }
            val color = colorList[position]
            onColorClick(color)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun onColorListChanged(list: List<Int>) {
            colorList.clear()
            colorList.addAll(list)
            notifyDataSetChanged()
        }

        fun findFirstColor(): Int {
            if (colorList.isNotEmpty()) {
                return colorList[0]
            }
            return Color.RED
        }

        override fun getItemCount(): Int {
            return colorList.size + 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ColorHolder) {
                holder.bind(colorList[position])
            }
            if (holder is PaletteHolder) {
                holder.bind(getPigment())
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position >= 0 && position < colorList.size) {
                return TYPE_COLOR
            }
            return TYPE_PALETTE
        }

    }

    private class ColorHolder(
        view: View,
        private val colorView: RoundBackgroundView,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        companion object {
            fun crate(parent: ViewGroup, onClickCallback: (Int) -> Unit): ColorHolder {
                val frameLayout = FrameLayout(parent.context)
                frameLayout.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val colorView = RoundBackgroundView(parent.context)
                colorView.type = RoundBackgroundView.RoundType.SMALLER
                colorView.value = 0.5F
                val margin = 6.dp2px
                colorView.layoutParams = FrameLayout.LayoutParams(
                    36.dp2px,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = margin
                    marginEnd = margin
                }
                frameLayout.addView(colorView)
                return ColorHolder(frameLayout, colorView, onClickCallback)
            }
        }

        init {
            colorView.onClick {
                onItemViewClick()
            }
        }

        private fun onItemViewClick() {
            onClickCallback(adapterPosition)
        }

        fun bind(color: Int) {
            colorView.color = color
        }

    }

    private class PaletteHolder(
        view: View,
        private val iconView: ImageView,
        private val onClickCallback: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        companion object {
            fun create(parent: ViewGroup, onClickCallback: () -> Unit): PaletteHolder {
                val frameLayout = FrameLayout(parent.context)
                frameLayout.layoutParams = ViewGroup.LayoutParams(
                    48.dp2px,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val iconView = ImageView(parent.context)
                val size = 24.dp2px
                iconView.layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                }
                iconView.setImageResource(R.drawable.ic_baseline_palette_24)
                frameLayout.addView(iconView)
                return PaletteHolder(frameLayout, iconView, onClickCallback)
            }
        }

        init {
            itemView.onClick {
                onClickCallback()
            }
        }

        fun bind(pigment: Pigment?) {
            if (pigment == null) {
                iconView.imageTintList = null
            } else {
                iconView.imageTintList
                ColorStateList.valueOf(pigment.onBackgroundTitle)
            }
        }

    }

    interface Callback {

        fun notifyContentChanged()

        fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>)

    }

    protected class Radius(
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomY: Float = 0F,
    ) {

        fun array(): FloatArray {
            return floatArrayOf(
                leftTopX,
                leftTopY,
                rightTopX,
                rightTopY,
                rightBottomX,
                rightBottomY,
                leftBottomX,
                leftBottomY,
            )
        }

        fun pixelSize(width: Float, height: Float): FloatArray {
            return floatArrayOf(
                leftTopX * width,
                leftTopY * height,
                rightTopX * width,
                rightTopY * height,
                rightBottomX * width,
                rightBottomY * height,
                leftBottomX * width,
                leftBottomY * height,
            )
        }

        fun copyFrom(info: Radius) {
            this.leftTopX = info.leftTopX
            this.leftTopY = info.leftTopY
            this.rightTopX = info.rightTopX
            this.rightTopY = info.rightTopY
            this.rightBottomX = info.rightBottomX
            this.rightBottomY = info.rightBottomY
            this.leftBottomX = info.leftBottomX
            this.leftBottomY = info.leftBottomY
        }

        fun isSame(info: Radius): Boolean {
            return (this.leftTopX == info.leftTopX &&
                    this.leftTopY == info.leftTopY &&
                    this.rightTopX == info.rightTopX &&
                    this.rightTopY == info.rightTopY &&
                    this.rightBottomX == info.rightBottomX &&
                    this.rightBottomY == info.rightBottomY &&
                    this.leftBottomX == info.leftBottomX &&
                    this.leftBottomY == info.leftBottomY
                    )
        }

    }

}