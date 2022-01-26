package com.lollipop.bigboom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.lollipop.bigboom.PatchesStatus.*
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2022/1/7 21:59
 * 大爆炸的View
 */
class BigBoomView(
    context: Context, attr: AttributeSet?, style: Int
) : ViewGroup(context, attr, style){

    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context) : this(context, null)

    private val patchList = ArrayList<Patches>()

    private val itemGroup = RecyclerView(context).apply {
        layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }
    }

//    private val quickSelectionHelper = QuickSelectionHelper(context).apply {
//        onSelectedRangeAdd {
//            notifySelectedAdd(it)
//        }
//        onSelectedRangeReduce {
//            notifySelectedReduce(it)
//        }
//        getPositionByLocation { x, y ->
//            findSelectPosition(x, y)
//        }
//        notifyListScroll { x, y ->
//            itemGroup.scrollBy(x, y)
//        }
//        getGroupHeight {
//            height
//        }
//    }

    init {
        addView(itemGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private fun notifySelectedAdd(intRange: IntRange) {
        intRange.forEach {
            patchList[it].let { patches ->
                if (!patches.status.isDisable) {
                    patches.status = SELECTED
                }
            }
        }
        itemGroup.adapter?.notifyItemRangeChanged(
            intRange.first,
            intRange.last - intRange.first
        )
    }

    private fun notifySelectedReduce(intRange: IntRange) {
        intRange.forEach {
            patchList[it].let { patches ->
                if (!patches.status.isDisable) {
                    patches.status = DEFAULT
                }
            }
        }
        itemGroup.adapter?.notifyItemRangeChanged(
            intRange.first,
            intRange.last - intRange.first
        )
    }

    fun bindItemProvider(itemProvider: PatchesItemProvider) {
        itemGroup.adapter = BigBoomAdapter(patchList, itemProvider, ::onItemClick)
    }

    /**
     * 获取被选中的字符串的集合
     */
    fun getSelectedValues(): List<String> {
        val array = ArrayList<String>()
        patchList.forEach {
            if (it.status.isSelected) {
                array.add(it.value)
            }
        }
        return array
    }

    /**
     * 获取选中范围
     */
    fun getSelectedRange(): List<IntRange> {
        val array = ArrayList<IntRange>()
        var lastIndex = -1
        for (index in patchList.indices) {
            if (patchList[index].status.isSelected) {
                if (lastIndex < 0) {
                    lastIndex = index
                }
            } else {
                if (lastIndex >= 0 && index != 0) {
                    array.add(IntRange(lastIndex, index - 1))
                    lastIndex = -1
                }
            }
        }
        return array
    }

    private fun onItemClick(position: Int) {
        if (position < 0 || position >= patchList.size) {
            return
        }
        val patches = patchList[position]
        if (patches.status.isDisable) {
            return
        }
        when (patches.status) {
            SELECTED -> {
                patches.status = DEFAULT
            }
            DEFAULT -> {
                patches.status = SELECTED
            }
            DISABLE -> {}
        }
        itemGroup.adapter?.notifyItemChanged(position)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childRight = width - paddingRight
        val childBottom = height - paddingBottom
        for (index in 0 until childCount) {
            getChildAt(index)?.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    fun setPatches(value: Array<String>, defaultSelected: Array<Int> = emptyArray()) {
        patchList.clear()
        value.forEach {
            patchList.add(Patches(it))
        }
        defaultSelected.forEach {
            patchList[it].status = SELECTED
        }
        notifyPatchesChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyPatchesChanged() {
//        quickSelectionHelper.reset()
        itemGroup.adapter?.notifyDataSetChanged()
    }

//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return quickSelectionHelper.onTouchEvent(event) || super.onTouchEvent(event)
//    }
//
//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        return quickSelectionHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev)
//    }

    private fun findSelectPosition(x: Float, y: Float): Int {
        val itemView = itemGroup.findChildViewUnder(x, y) ?: return -1
        return itemGroup.findContainingViewHolder(itemView)?.adapterPosition ?: -1
    }

    private class QuickSelectionHelper(private val context: Context) {

        companion object {
            private const val INVALID_TIME = 0L
            private const val INVALID_POSITION = -1
        }

        private var dragStartPosition = INVALID_POSITION
        private var nowSelectedPosition = INVALID_POSITION
        private var lastSelectedPosition = INVALID_POSITION

        private var touchDownTime = INVALID_TIME
        private var activeTouchId = -1
        private val lastTouchLocation = Point()

        private var status = Status.CANCEL

        private val longPressTimeout = ViewConfiguration.getLongPressTimeout()
        private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        private var scrollUpTime = INVALID_TIME
        private var scrollDownTime = INVALID_TIME
        var scrollUpWeight = 0.1F
        var scrollDownWeight = 0.1F
        var scrollSpeed = 1000L

        private var onSelectedRangeAddListener: ((IntRange) -> Unit)? = null
        private var onSelectedRangeReduceListener: ((IntRange) -> Unit)? = null
        private var positionProvider: ((x: Float, y: Float) -> Int)? = null
        private var notifyListScrollCallback: ((x: Int, y: Int) -> Unit)? = null
        private var groupHeightProvider: (() -> Int)? = null

        fun onTouchEvent(event: MotionEvent?): Boolean {
            if (status.isCancel) {
                return false
            }
            event ?: return false
            return onTouch(event)
        }

        fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
            if (status.isCancel) {
                return false
            }
            event ?: return false
            return onTouch(event)
        }

        private fun onTouch(event: MotionEvent): Boolean {
            positionProvider ?: return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onTouchDown(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    onTouchMove(event)
                }
                MotionEvent.ACTION_UP -> {
                    reset()
                }
                MotionEvent.ACTION_CANCEL -> {
                    reset()
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    getFocusTouchPosition(event)
                }
            }
            return status.isEnable
        }

        private fun onTouchDown(event: MotionEvent) {
            touchDownTime = System.currentTimeMillis()
            activeTouchId = event.getPointerId(0)
            val focus = focusXY(event)
            if (focus == null) {
                reset()
                return
            }
            notifyTouchMoved(focus[0], focus[1])
            status = Status.WAIT
        }

        private fun onTouchMove(event: MotionEvent) {
            val getPosition = positionProvider
            if (getPosition == null) {
                reset()
                return
            }
            val focus = focusXY(event)
            if (focus == null) {
                reset()
                return
            }
            val x = focus[0]
            val y = focus[1]
            if (status.isWait) {
                // 位置偏移是否符合，如果超过范围，那么认为是滑动，放弃事件
                if (abs(x - lastTouchLocation.x) > scaledTouchSlop
                    || abs(y - lastTouchLocation.y) > scaledTouchSlop
                ) {
                    status = Status.CANCEL
                    return
                }
                val now = System.currentTimeMillis()
                // 是否达到长按时间，如果没有达到，那么就接着等待
                if ((now - touchDownTime) < longPressTimeout) {
                    return
                }
                // 达到长按时长，那么可以开始滑动操作了
                status = Status.ENABLE
                notifyTouchMoved(x, y)
                val position = getPosition(x, y)
                dragStartPosition = position
                nowSelectedPosition = position
                lastSelectedPosition = position
                if (position >= 0) {
                    notifyPositionAdd(position..position)
                }
                callEnable()
                return
            }
            notifyScroll(x, y)
            // 以下逻辑都认为是激活状态
            if (!isTouchMoved(x, y)) {
                // 位置不变就不更新
                return
            }
            val position = getPosition(x, y)
            // 没有找到序号，不更新
            if (position < 0) {
                return
            }
            // 如果开始时候没有找到序号，那么更新一下序号
            if (dragStartPosition < 0) {
                dragStartPosition = position
                nowSelectedPosition = position
                lastSelectedPosition = position
                if (position >= 0) {
                    notifyPositionAdd(position..position)
                }
                return
            }
            // 最新选中的序号发生了变化
            if (nowSelectedPosition != position) {
                lastSelectedPosition = nowSelectedPosition
                nowSelectedPosition = position
                notifyPositionChanged()
            }
        }

        private fun notifyPositionChanged() {
            val last = lastSelectedPosition
            val start = dragStartPosition
            val now = nowSelectedPosition
            if (last - start > 0) {
                // 原本是向后选择的
                if (now - start > 0) {
                    // 现在也是向后选择
                    if (now > last) {
                        // 选择的更多了
                        notifyPositionAdd((last + 1)..now)
                    } else {
                        // 选的更少了
                        notifyPositionReduce((now + 1)..last)
                    }
                } else {
                    // 现在变成了向前选择
                    // 移除后面的
                    notifyPositionReduce((start + 1)..last)
                    // 增加前面的
                    notifyPositionAdd(now..start)
                }
            } else {
                // 原本是向前选择的
                if (now - start > 0) {
                    // 现在变成了向后选择
                    // 移除前面的
                    notifyPositionReduce(last until start)
                    // 增加后面的
                    notifyPositionAdd(start..now)
                } else {
                    // 现在也是向前选择
                    if (now > last) {
                        // 选的更少了
                        notifyPositionReduce(last until now)
                    } else {
                        // 选的更多了
                        notifyPositionAdd(now until last)
                    }
                }
            }
        }

        private fun notifyScroll(x: Float, y: Float) {
            val scrollCallback = notifyListScrollCallback ?: return
            val groupHeight = groupHeightProvider?.invoke() ?: return
            val upThreshold = groupHeight * scrollUpWeight
            val downThreshold = groupHeight * (1 - scrollDownWeight)
            if (y in upThreshold..downThreshold) {
                scrollUpTime = INVALID_TIME
                scrollDownTime = INVALID_TIME
                return
            }
            val now = System.currentTimeMillis()
            if (y < upThreshold) {
                scrollDownTime = INVALID_TIME
                if (scrollUpTime == INVALID_TIME) {
                    scrollUpTime = now
                    return
                }
                val time = (now - scrollUpTime) * 1F / scrollSpeed
                val distance = upThreshold - y
                val offset = (distance * time).toInt()
                if (abs(offset) > 0) {
                    scrollUpTime = now
                    scrollCallback.invoke(0, offset * -1)
                }
            } else if (y > downThreshold) {
                scrollUpTime = INVALID_TIME
                if (scrollDownTime == INVALID_TIME) {
                    scrollDownTime = now
                }
                val time = (now - scrollDownTime) * 1F / scrollSpeed
                val distance = y - downThreshold
                val offset = (distance * time).toInt()
                if (abs(offset) > 0) {
                    scrollDownTime = now
                    scrollCallback.invoke(0, offset)
                }
            }
        }

        private fun callEnable() {
            val vibratorService = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibratorService ?: return
            if (!vibratorService.hasVibrator()) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibratorService.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                vibratorService.vibrate(100L)
            }
        }

        private fun notifyPositionAdd(intRange: IntRange) {
            onSelectedRangeAddListener?.invoke(intRange)
        }

        private fun notifyPositionReduce(intRange: IntRange) {
            onSelectedRangeReduceListener?.invoke(intRange)
        }

        private fun focusXY(event: MotionEvent): Array<Float>? {
            val focusTouchPosition = getFocusTouchPosition(event)
            if (focusTouchPosition < 0) {
                return null
            }
            return arrayOf(
                event.getX(focusTouchPosition),
                event.getY(focusTouchPosition)
            )
        }

        private fun getFocusTouchPosition(event: MotionEvent): Int {
            val index = event.findPointerIndex(activeTouchId)
            if (index < 0) {
                activeTouchId = event.getPointerId(0)
                return 0
            }
            return index
        }

        private fun isTouchMoved(x: Float, y: Float): Boolean {
            return lastTouchLocation.x != x.toInt() || lastTouchLocation.y != y.toInt()
        }

        private fun notifyTouchMoved(x: Float, y: Float) {
            lastTouchLocation.set(x.toInt(), y.toInt())
        }

        fun reset() {
            dragStartPosition = INVALID_POSITION
            nowSelectedPosition = INVALID_POSITION
            lastSelectedPosition = INVALID_POSITION
            touchDownTime = INVALID_TIME
            status = Status.CANCEL
            scrollUpTime = INVALID_TIME
            scrollDownTime = INVALID_TIME
        }

        fun onSelectedRangeAdd(callback: ((IntRange) -> Unit)) {
            onSelectedRangeAddListener = callback
        }

        fun onSelectedRangeReduce(callback: ((IntRange) -> Unit)) {
            onSelectedRangeReduceListener = callback
        }

        fun getPositionByLocation(callback: (x: Float, y: Float) -> Int) {
            positionProvider = callback
        }

        fun notifyListScroll(callback: (x: Int, y: Int) -> Unit) {
            notifyListScrollCallback = callback
        }

        fun getGroupHeight(callback: () -> Int) {
            groupHeightProvider = callback
        }

        private enum class Status {
            WAIT,
            ENABLE,
            CANCEL;

            val isCancel: Boolean
                get() {
                    return this == CANCEL
                }
            val isEnable: Boolean
                get() {
                    return this == ENABLE
                }

            val isWait: Boolean
                get() {
                    return this == WAIT
                }
        }

    }

    private class BigBoomAdapter(
        private val valueList: List<Patches>,
        private val itemProvider: PatchesItemProvider,
        private val itemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<PatchesHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatchesHolder {
            return itemProvider.createHolder(parent).apply {
                itemView.setOnClickListener(OnHolderClickListener(this, itemClickListener))
            }
        }

        override fun onBindViewHolder(holder: PatchesHolder, position: Int) {
            val patches = valueList[position]
            holder.bind(patches.value, patches.status)
        }

        override fun getItemCount(): Int {
            return valueList.size
        }

        private class OnHolderClickListener(
            private val holder: PatchesHolder,
            private val callback: OnItemClickListener
        ) : OnClickListener {
            override fun onClick(v: View?) {
                if (v != holder.itemView) {
                    return
                }
                callback.onItemClick(holder.adapterPosition)
            }
        }

        fun interface OnItemClickListener {
            fun onItemClick(position: Int)
        }

    }

    private class Patches(
        val value: String,
        var status: PatchesStatus = DEFAULT
    )

}