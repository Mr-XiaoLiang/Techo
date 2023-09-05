package com.lollipop.lqrdemo.creator.subpage

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.FragmentQrCornerBinding

/**
 * 圆角设置的页面，它和背景设置其实是一起的，但是业务上，放不到同一个页面里面
 */
class QrCornerFragment : QrBaseSubpageFragment() {

    private val binding: FragmentQrCornerBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private class CutModeDrawable: ModeDrawable() {
        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

    }

    private class RoundModeDrawable: ModeDrawable() {
        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

    }

    private class SquircleModeDrawable: ModeDrawable() {
        override fun draw(canvas: Canvas) {
            TODO("Not yet implemented")
        }

    }


    private abstract class ModeDrawable: Drawable() {

        protected val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(filter: ColorFilter?) {
            paint.colorFilter = filter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }


    }


}