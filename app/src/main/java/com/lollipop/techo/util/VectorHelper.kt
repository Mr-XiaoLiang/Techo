package com.lollipop.techo.util

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
        private val cachePath = Path().apply {
            addPath(srcPath)
        }

        val path: Path
            get() {
                return cachePath
            }

        fun onBoundsChanged(width: Int, height: Int, offsetX: Int, offsetY: Int) {
            if (width == lastWidth && height == lastHeight
                && lastOffsetX == offsetX && lastOffsetY == offsetY
            ) {
                return
            }
            val widthWeight = width * 1F / viewportWidth
            val heightWeight = height * 1F / viewportHeight
            val matrix = Matrix()
            matrix.setScale(widthWeight, heightWeight)
            matrix.postTranslate(offsetX.toFloat(), offsetY.toFloat())
            cachePath.reset()
            cachePath.addPath(srcPath, matrix)
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