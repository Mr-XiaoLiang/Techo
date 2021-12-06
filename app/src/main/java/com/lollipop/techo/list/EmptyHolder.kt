package com.lollipop.techo.list

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author lollipop
 * @date 2021/12/6 22:54
 * 一个不占用空间的用于未知类型数据的Holder
 */
class EmptyHolder private constructor(view: EmptyView) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(group: ViewGroup): EmptyHolder {
            return EmptyHolder(EmptyView(group.context))
        }
    }

    private class EmptyView(context: Context) : View(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(0, 0)
        }

        override fun onDraw(canvas: Canvas?) {
        }

    }

}