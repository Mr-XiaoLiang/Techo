package liang.lollipop.ltabview

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.InflateException
import android.view.View
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @date: 2019/04/17 19:43
 * @author: lollipop
 * TabView主体
 */
class LTabView(context: Context, attr: AttributeSet?,
               defStyleAttr: Int, defStyleRes: Int): FrameLayout(context, attr, defStyleAttr, defStyleRes),
    ValueAnimator.AnimatorUpdateListener{

    constructor(context: Context, attr: AttributeSet?,
                defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    companion object {
        private const val MIN_PROGRESS = 0F
        private const val MAX_PROGRESS = 1F
        private const val TOLERANCE_SCOPE = 0.0001F

        private const val DEF_DURATION = 300L
    }

    /**
     * 排版样式，默认为自适应模式
     */
    var style = Style.Fit
        set(value) {
            field = value
            updateLocationByAnimator()
        }

    /**
     * 限制长度，保证View尺寸
     */
    var isLimit = true
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 是否统一宽度
     */
    var isUniform = false
        set(value) {
            field = value
            requestLayout()
        }

    private var selectedIndex = 0

    private val childTargetLocation = ArrayList<Int>()
    private val childFromLocation = ArrayList<Int>()
    private val childWidth = ArrayList<ItemSize>()

    private val locationAnimator = ValueAnimator().apply {
        addUpdateListener(this@LTabView)
    }

    private val itemAnimators = HashMap<LTabItem, ItemAnimatorHelper>()

    var onSelectedListener: OnSelectedListener? = null

    var animationDuration = DEF_DURATION
        set(value) {
            field = value
            locationAnimator.duration = value
            itemAnimators.values.forEach { it.duration(value) }
        }

    var space = 0
        set(value) {
            field = value
            requestLayout()
        }

    private fun reLayout() {
        updateChildLocation()
        val top = paddingTop
        val bottom = height - paddingBottom
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val loc = childTargetLocation[index]
//            log("reLayout: $index: [${child.left}, ${child.top}, ${child.right}, ${child.bottom}]")
            val childWidth = getChildWidthAt(index)
            child.layout(loc, top, loc + childWidth, bottom)
//            log("reLayout: $index: [${child.left}, ${child.top}, ${child.right}, ${child.bottom}]")
            child.translationX = 0F
            val item = child as LTabItem
            val animHelper = itemAnimators[item]
            if (index == selectedIndex) {
                animHelper?.progress = 1F
            } else {
                animHelper?.progress = 0F
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        var left = paddingLeft
        val usedHeight = height - paddingBottom - paddingTop
        var maxItem = 0
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            child.measure(widthMeasureSpec, heightMeasureSpec)
            val item = child as LTabItem
            val childExpend = child.measuredWidth - item.miniSize
            if (maxItem < childExpend) {
                maxItem = childExpend
            }
            left += max(usedHeight, item.miniSize)
            left += space
        }
        left += maxItem
        left -= space
        left += paddingRight

        val width = if (widthMode == MeasureSpec.AT_MOST) {
            min(MeasureSpec.getSize(widthMeasureSpec), left)
        } else {
            style = Style.Start
            left
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        limit()
        reLayout()
    }

    private fun updateChildLocation() {
        when (style) {
            Style.Start -> layoutByStart()
            Style.End -> layoutByEnd()
            Style.Fit -> layoutByFit()
            Style.Center -> layoutByCenter()
        }
    }

    private fun limit() {
        // 如果不做限制，那么直接返回
        if (!isLimit) {
            childWidth.forEach {
                it.max = -1
                it.min = -1
            }
            return
        }
        // 测定每一个item的最大可用宽度
        for (index in 0 until childCount) {
            limitByIndex(index)
            childWidth[index].min = -1
        }
        // 如果要求均一宽度，那么需要再次进行一次遍历，得到最大宽度中的最小值
        if (isUniform) {
            var uniformSize = 0
            childWidth.forEach {
                if (it.max > uniformSize) {
                    uniformSize = it.max
                }
            }
            val width = this.width - paddingLeft - paddingRight
            for (index in 0 until childCount) {
                if (uniformSize > width - childWidth[index].unavailable) {
                    uniformSize = width - childWidth[index].unavailable
                }
            }
            childWidth.forEach {
                it.max = uniformSize
                it.min = uniformSize
            }
        }
    }

    private fun limitByIndex(index: Int) {
        var selectedSize = 0
        var unselectedSize = 0
        val width = this.width - paddingLeft - paddingRight
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            if (i == index) {
                selectedSize = child.measuredWidth
            } else {
                unselectedSize += (child as LTabItem).miniSize
            }
        }
        if (width - unselectedSize < selectedSize) {
            selectedSize = width - unselectedSize
        }
        childWidth[index].max = selectedSize
        childWidth[index].unavailable = unselectedSize
    }

    private fun layoutByStart() {
        var left = paddingLeft
        val height = this.height - paddingBottom - paddingTop

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            val item = child as LTabItem
            var itemHorizontal = ((height - item.miniSize) * 0.5F).toInt()
            if (itemHorizontal < 0) {
                itemHorizontal = 0
            }
            val itemLeft = left + itemHorizontal
            childTargetLocation[index] = itemLeft
            if (index == selectedIndex) {
                left += getChildWidthAt(index)
                left -= item.miniSize
            }
            left += max(height, item.miniSize)
            left += space
        }
    }

    private fun layoutByEnd() {
        var right = this.right - paddingRight
        val height = this.height - paddingBottom - paddingTop
        for (index in (childCount - 1) downTo 0) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            val item = child as LTabItem
            var itemHorizontal = ((height - item.miniSize) * 0.5F).toInt()
            if (itemHorizontal < 0) {
                itemHorizontal = 0
            }
            val childWidth = getChildWidthAt(index)
            val itemRight = if (index == selectedIndex) {
                right - itemHorizontal
            } else {
                right - itemHorizontal - item.miniSize + childWidth
            }
            childTargetLocation[index] = itemRight - childWidth
            if (index == selectedIndex) {
                right -= childWidth
                right += item.miniSize
            }
            right -= max(height, item.miniSize)
            right -= space
        }
    }

    private fun layoutByFit() {
        var left = paddingLeft
        val right = this.right - this.left - paddingRight
        val width = right - left

        var effectiveWidth = 0
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            effectiveWidth += if (index == selectedIndex) {
                getChildWidthAt(index)
            } else {
                (child as LTabItem).miniSize
            }
        }

        val itemHorizontal = (width - effectiveWidth) * 1F / (childCount + 1)
        left = (left + itemHorizontal).toInt()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            childTargetLocation[index] = left
            left += if (index == selectedIndex) {
                getChildWidthAt(index)
            } else {
                (child as LTabItem).miniSize
            }
            left = (left + itemHorizontal).toInt()
        }

    }

    private fun layoutByCenter() {
        var left = paddingLeft
        val right = this.right - this.left - paddingRight
        val width = right - left

        var effectiveWidth = 0
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            effectiveWidth += if (index == selectedIndex) {
                getChildWidthAt(index) + (height - (child as LTabItem).miniSize)
            } else {
                height
            }
        }

        left += (width - effectiveWidth) / 2
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            val item = child as LTabItem
            var itemHorizontal = ((height - item.miniSize) * 0.5F).toInt()
            if (itemHorizontal < 0) {
                itemHorizontal = 0
            }
            childTargetLocation[index] = left + itemHorizontal
            if (index == selectedIndex) {
                left += getChildWidthAt(index)
                left -= item.miniSize
            }
            left += height
        }

    }

    private fun getChildWidthAt(index: Int): Int {
        return childWidth[index].limit(getChildAt(index).measuredWidth)
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        if (child == null) {
            return
        }
        val item = child as LTabItem
        itemAnimators.remove(item)
        childTargetLocation.removeAt(0)
        childFromLocation.removeAt(0)
        childWidth.removeAt(0)
        requestLayout()
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child == null) {
            return
        }
        if (child !is LTabItem) {
            throw InflateException("LTabView does not allow adding views other than LTabItem")
        }
        val item = child as LTabItem
        itemAnimators[item] = ItemAnimatorHelper(item, animationDuration)
        childTargetLocation.add(0)
        childFromLocation.add(0)
        childWidth.add(ItemSize())
        item.onTabClick {
            for (i in 0 until childCount) {
                if (getChildAt(i) == it) {
                    selected(i)
                    break
                }
            }
        }
        requestLayout()
    }

    fun selected(index: Int) {
        if (selectedIndex == index) {
            return
        }
        selectedIndex = index
        updateLocationByAnimator()
        onSelectedListener?.onTabSelected(index)
    }

    private fun updateLocationByAnimator() {
        if (locationAnimator.isRunning) {
            locationAnimator.cancel()
        }
        updateLocation()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val helper = itemAnimators[child as LTabItem]?:continue
            if (i == selectedIndex) {
                helper.open()
            } else {
                helper.close()
            }
        }
        locationAnimator.setFloatValues(MIN_PROGRESS, MAX_PROGRESS)
        locationAnimator.start()
    }

    private fun updateLocation() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            childFromLocation[i] = child.x.toInt()
            log("selected: $i: from = ${childFromLocation[i]}, to = ${childTargetLocation[i]}")
        }
        updateChildLocation()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == locationAnimator) {
            val value = locationAnimator.animatedValue as Float
            moveChildByOffset(value)
        }
    }

    private fun moveChildByOffset(value: Float) {
        for (i in 0 until childCount) {
            val from = childFromLocation[i]
            val to = childTargetLocation[i]
            val child = getChildAt(i)
            val offset = (to - from) * value + from - child.x
            child.offsetLeftAndRight(offset.toInt())
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
    }

    interface OnSelectedListener {
        fun onTabSelected(index: Int)
    }

    private class ItemAnimatorHelper(private val item: LTabItem, animationDuration: Long): ValueAnimator.AnimatorUpdateListener {

        private val tabAnimator = ValueAnimator().apply {
            addUpdateListener(this@ItemAnimatorHelper)
        }

        private var duration = animationDuration

        var progress = 0F
            set(value) {
                field = value
                update()
            }

        fun close() {
            tabAnimator.cancel()
            if (abs(progress - MIN_PROGRESS) < TOLERANCE_SCOPE) {
                progress = MIN_PROGRESS
                return
            }
            tabAnimator.setFloatValues(progress, MIN_PROGRESS)
            tabAnimator.duration = ((progress - MIN_PROGRESS) / (MAX_PROGRESS - MIN_PROGRESS) * duration).toLong()
            tabAnimator.start()
        }

        fun open() {
            tabAnimator.cancel()
            if (abs(progress - MAX_PROGRESS) < TOLERANCE_SCOPE) {
                progress = MAX_PROGRESS
                return
            }
            tabAnimator.setFloatValues(progress, MAX_PROGRESS)
            tabAnimator.duration = ((MAX_PROGRESS - progress) / (MAX_PROGRESS - MIN_PROGRESS) * duration).toLong()
            tabAnimator.start()
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            if (animation == tabAnimator) {
                progress = animation.animatedValue as Float
            }
        }

        private fun update() {
            item.schedule(progress)
        }

        fun duration(value: Long) {
            duration = value
        }

        fun cancel() {
            tabAnimator.cancel()
        }
    }

    enum class Style(val value: Int) {
        /**
         * 从头排列
         */
        Start(1),
        /**
         * 从尾排列
         */
        End(2),
        /**
         * 按照权重排列
         */
        Fit(3),
        /**
         * 居中排列
         */
        Center(4),
    }

    class ItemSize(var max: Int = -1, var min: Int = -1, var unavailable: Int = 0) {

        fun limit(value: Int): Int {
            if (max in 1 until value) {
                return max
            }
            if (min > 0 && value < min) {
                return min
            }
            return value
        }

    }

    private fun log(value: String) {
        Log.d("Lollipop", "LTabView: $value")
    }
}