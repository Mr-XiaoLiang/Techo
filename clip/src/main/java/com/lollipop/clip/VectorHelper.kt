package com.lollipop.clip

import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.graphics.PathParser

object VectorHelper {

    /**
     * @param viewportWidth 窗口的大小，相对于画布数据而言
     * @param viewportHeight 窗口的大小，相对于画布数据而言
     * @param pathData 绘制的数据，路径的数据
     */
    fun parse(
        viewportWidth: Int,
        viewportHeight: Int,
        pathData: String
    ): VectorInfo {
        return parse(VectorData(viewportWidth, viewportHeight, pathData))
    }

    /**
     * @param data 矢量图的描述信息
     */
    fun parse(
        data: VectorData
    ): VectorInfo {
        return VectorInfo(
            data.viewportWidth,
            data.viewportHeight,
            PathParser.createPathFromPathData(data.pathData)
        )
    }

    fun outline(
        viewportWidth: Int,
        viewportHeight: Int,
        pathData: String
    ): OutlineBuilder {
        return OutlineBuilder(parse(viewportWidth, viewportHeight, pathData))
    }

    class OutlineBuilder(private val info: VectorInfo) {

        fun bindTo(view: View) {
            view.clipToOutline = true
            view.outlineProvider = VectorOutlineProvider(info)
        }

    }

    class VectorOutlineProvider(private val vectorInfo: VectorInfo) : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline ?: return
            vectorInfo.onBoundsChanged(
                view.width - view.paddingLeft - view.paddingRight,
                view.height - view.paddingTop - view.paddingBottom,
                view.paddingLeft,
                view.paddingTop
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(vectorInfo.path)
            } else {
                outline.setConvexPath(vectorInfo.path)
            }
        }
    }

    class VectorStack {

        private val frameList = ArrayList<VectorInfo>()
        private val path = Path()

        fun addFrame(index: Int, frame: VectorInfo): VectorStack {
            frameList.add(index, frame)
            return this
        }

        fun addFrame(frame: VectorInfo): VectorStack {
            frameList.add(frame)
            return this
        }

        fun removeFrame(frame: VectorInfo): VectorStack {
            frameList.remove(frame)
            return this
        }

        fun removeAt(index: Int): VectorStack {
            frameList.removeAt(index)
            return this
        }

        fun addFrame(data: VectorData): VectorStack {
            addFrame(parse(data))
            return this
        }

        fun onBoundsChanged(width: Int, height: Int, offsetX: Int, offsetY: Int) {
            path.reset()
            frameList.forEach {
                it.onBoundsChanged(width, height, offsetX, offsetY)
                path.addPath(it.path)
            }
        }
    }

    /**
     * 矢量图的解析数据
     */
    class VectorInfo(
        private val viewportWidth: Int,
        private val viewportHeight: Int,
        private val srcPath: Path
    ) {

        private var lastWidth = viewportWidth
        private var lastHeight = viewportHeight
        private var lastOffsetX = 0
        private var lastOffsetY = 0
        val path = Path().apply {
            addPath(srcPath)
        }

        fun onBoundsChanged(width: Int, height: Int, offsetX: Int, offsetY: Int) {
            if (width == lastWidth && height == lastHeight
                && lastOffsetX == offsetX && lastOffsetY == offsetY
            ) {
                return
            }
            if (width == 0 || height == 0) {
                path.reset()
                return
            }
            val widthWeight = width * 1F / viewportWidth
            val heightWeight = height * 1F / viewportHeight
            val matrix = Matrix()
            matrix.setScale(widthWeight, heightWeight)
            matrix.postTranslate(offsetX.toFloat(), offsetY.toFloat())
            path.reset()
            path.addPath(srcPath, matrix)
        }

    }

    /**
     * 矢量图描述信息
     */
    class VectorData(
        /**
         * 矢量图的宽度
         */
        val viewportWidth: Int,
        /**
         * 矢量图的高度
         */
        val viewportHeight: Int,
        /**
         * 矢量图的信息
         */
        val pathData: String
    )

}