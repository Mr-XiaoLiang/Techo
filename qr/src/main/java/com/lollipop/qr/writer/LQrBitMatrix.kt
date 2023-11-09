package com.lollipop.qr.writer

import android.graphics.Rect
import com.google.zxing.qrcode.decoder.Version
import com.google.zxing.qrcode.encoder.QRCode


/**
 * Created by lollipop on 2018/3/16.
 * @author Lollipop
 * 一个包装的矩阵，因为在二维码缩放模式下，
 * 需要标记清除允许透明的部分，黑色部分，白色部分
 */
class LQrBitMatrix(
    private val qrCode: QRCode,
    val quietZone: Int,
    width: Int,
    height: Int = width
) : LBitMatrix(width, height) {

    companion object {
        fun copyOf(bitMatrix: LQrBitMatrix): LQrBitMatrix {
            val lqrMatrix = LQrBitMatrix(
                bitMatrix.qrCode,
                bitMatrix.quietZone,
                bitMatrix.width,
                bitMatrix.height
            )
            val blackMatrix = bitMatrix.blackMatrix
            val nullableMatrix = bitMatrix.nullableMatrix
            for (width in 0 until bitMatrix.width) {
                for (height in 0 until bitMatrix.height) {
                    if (blackMatrix.get(width, height)) {
                        lqrMatrix.setBlack(width, height)
                    }
                    if (nullableMatrix.get(width, height)) {
                        lqrMatrix.setNullable(width, height)
                    }
                }
            }
            return lqrMatrix
        }

        /**
         * 获取二维码的Version
         * 通过宽度来计算宽度
         */
        fun getVersion(width: Int) = LQRCodeWriter.getVersion(width)

        /**
         * getVersion 的反向操作，计算最小可用宽度
         */
        fun getMinWidth(version: Int) = LQRCodeWriter.getMinWidth(version)

        /**
         * 左上角定位点
         */
        fun inLeftTop(x: Int, y: Int) = LQRCodeWriter.inLeftTop(x, y)

        /**
         * 右上角定位点
         */
        fun inRightTop(width: Int, x: Int, y: Int) = LQRCodeWriter.inRightTop(width, x, y)

        /**
         * 左下角定位点
         */
        fun inLeftBottom(height: Int, x: Int, y: Int) = LQRCodeWriter.inLeftBottom(height, x, y)

        /**
         * Timing Pattern基准线
         */
        fun inTimingPattern(x: Int, y: Int) = LQRCodeWriter.inTimingPattern(x, y)

        /**
         * 是否位于格式化数据分区
         */
        fun inFormatInformation(
            width: Int, x: Int, y: Int
        ) = LQRCodeWriter.inFormatInformation(width, x, y)

        /**
         * 判断是否在辅助定位点上
         */
        fun isAlignmentPattern(
            version: Version, width: Int, x: Int, y: Int
        ) = LQRCodeWriter.isAlignmentPattern(version, width, x, y)

    }

    val version: Version
        get() {
            return qrCode.version
        }

    val versionNumber: Int by lazy {
        version.versionNumber
    }

    private val leftTopPatternBounds by lazy {
        Rect().apply {
            val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
            set(0, 0, size - 1, size - 1)
            offset(quietZone, quietZone)
        }
    }

    private val rightTopPatternBounds by lazy {
        Rect().apply {
            val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
            val left = width - size
            set(left, 0, left + size - 1, size - 1)
            offset(-quietZone, quietZone)
        }
    }

    private val leftBottomPatternBounds by lazy {
        Rect().apply {
            val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
            val top = height - size
            set(0, top, size - 1, top + size - 1)
            offset(quietZone, -quietZone)
        }
    }

    private val horizontalTimingPatternBounds by lazy {
        Rect().apply {
            val patternSize = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1
            set(
                patternSize,
                patternSize,
                width - patternSize - quietZone - quietZone,
                patternSize
            )
            offset(quietZone, quietZone)
        }
    }

    private val verticalTimingPatternBounds by lazy {
        Rect().apply {
            val patternSize = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1
            set(
                patternSize,
                patternSize,
                patternSize,
                height - patternSize - quietZone - quietZone,
            )
            offset(quietZone, quietZone)
        }
    }

    private val alignmentPatternBounds by lazy {
        createAlignmentPattern()
    }

    fun qrWidthByVersion(): Int {
        val version = versionNumber
        if (version < 1) {
            return 0
        }
        return LQRCodeWriter.getMinWidth(version)
    }

    /**
     * 左上角定位点
     */
    fun getLeftTopPattern(out: Rect) {
        out.set(leftTopPatternBounds)
    }

    /**
     * 右上角定位点
     */
    fun getRightTopPattern(out: Rect) {
        out.set(rightTopPatternBounds)
    }

    /**
     * 左下角定位点
     */
    fun getLeftBottomPattern(out: Rect) {
        out.set(leftBottomPatternBounds)
    }


    /**
     * Timing Pattern基准线
     */
    fun getTimingPattern(
        horizontal: Rect,
        vertical: Rect
    ) {
        horizontal.set(horizontalTimingPatternBounds)
        vertical.set(verticalTimingPatternBounds)
    }


    /**
     * 判断是否在辅助定位点上
     */
    private fun createAlignmentPattern(): List<Rect> {
        val apcCenterArray = version.alignmentPatternCenters
        if (apcCenterArray.isEmpty()) {
            return emptyList()
        }
        val leftTop = Rect()
        val leftBottom = Rect()
        val rightTop = Rect()
        getLeftTopPattern(leftTop)
        getLeftBottomPattern(leftBottom)
        getRightTopPattern(rightTop)

        val checkArray = arrayOf(leftTop, leftBottom, rightTop)

        val resultList = ArrayList<Rect>()

        for (i in apcCenterArray) {
            for (j in apcCenterArray) {
                val left1 = i - 2
                val top1 = j - 2
                val left2 = j - 2
                val top2 = i - 2
                // 辅助定位点的边长是5，定位点的位置是中心点，
                // 起点向左移动2格，右边就需要向右移动4格，加上本格，总共5格
                val rect1 = Rect(left1, top1, left1 + 4, top1 + 4).apply {
                    offset(quietZone, quietZone)
                }
                val rect2 = Rect(left2, top2, left2 + 4, top2 + 4).apply {
                    offset(quietZone, quietZone)
                }
                if (safe(rect1, checkArray)) {
                    resultList.add(rect1)
                }
                if (safe(rect2, checkArray)) {
                    resultList.add(rect2)
                }
            }
        }
        return resultList
    }

    private fun safe(target: Rect, anchorArray: Array<Rect>): Boolean {
        anchorArray.forEach { rect ->
            if (rect.contains(target)) {
                return false
            }
            if (target.contains(rect)) {
                return false
            }
            if (rect.intersects(
                    target.left,
                    target.top,
                    target.right,
                    target.bottom
                )
            ) {
                return false
            }
        }
        return true
    }

    fun getAlignmentPattern(): List<Rect> {
        return alignmentPatternBounds.map { it.clone() }
    }

    private fun Rect.clone(): Rect {
        val src = this
        val rect = Rect()
        rect.set(src)
        return rect
    }

    fun forEach(callback: (x: Int, y: Int, type: Type) -> Unit) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val type = getType(x, y)
                callback(x, y, type)
            }
        }
    }

}
