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

    }

    val version: Version
        get() {
            return qrCode.version
        }

    val versionNumber: Int by lazy {
        version.versionNumber
    }

    fun qrWidthByVersion(): Int {
        val version = versionNumber
        if (version < 1) {
            return 0
        }
        return LQRCodeWriter.getMinWidth(version)
    }

    // TODO 需要在这里做一些方法，来判断和区分不同定位点

    /**
     * 左上角定位点
     */
    fun getLeftTopPattern(out: Rect) {
        val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
        out.set(0, 0, size - 1, size - 1)
        out.offset(quietZone, quietZone)
    }

    /**
     * 右上角定位点
     */
    fun getRightTopPattern(out: Rect) {
        val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
        val left = width - size
        out.set(left, 0, left + size - 1, size - 1)
        out.offset(-quietZone, quietZone)
    }

    /**
     * 左下角定位点
     */
    fun getLeftBottomPattern(out: Rect) {
        val size = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE
        val top = height - size
        out.set(0, top, size - 1, top - size - 1)
        out.offset(quietZone, -quietZone)
    }


    /**
     * Timing Pattern基准线
     */
    fun getTimingPattern(
        horizontal: Rect,
        vertical: Rect
    ) {
        val patternSize = BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1
        horizontal.set(
            patternSize,
            patternSize,
            width - patternSize - quietZone - quietZone,
            patternSize
        )
        horizontal.offset(quietZone, quietZone)

        vertical.set(
            patternSize,
            patternSize,
            patternSize,
            height - patternSize - quietZone - quietZone,
        )
        vertical.offset(quietZone, quietZone)
    }


    /**
     * 判断是否在辅助定位点上
     * TODO 应该跑一下代码，确定一下这个辅助定位点的参数协议
     */
//    fun getAlignmentPattern(): List<Rect> {
//        val apcCenterArray = version.alignmentPatternCenters
//        if (apcCenterArray.isEmpty()) {
//            return emptyList()
//        }
//        val leftTop = Rect()
//        val leftBottom = Rect()
//        val rightTop = Rect()
//        getLeftTopPattern(leftTop)
//        getLeftBottomPattern(leftBottom)
//        getRightTopPattern(rightTop)
//
//        for (i in apcCenterArray) {
//            for (j in apcCenterArray) {
//                //如果这个点刚好在左上角
//                if (leftTop.contains(i, j) || leftTop.contains(j, i)) {
//                    continue
//                }
//                //如果这个点刚好在右上角
//                if (inRightTop(width, i, j) || inRightTop(width, j, i)) {
//                    continue
//                }
//                //如果这个点刚好在左下角
//                if (inLeftBottom(width, i, j) || inLeftBottom(width, j, i)) {
//                    continue
//                }
//                //判断是否是在范围内
//                if ((x <= i + 2 && x >= i - 2) && (y <= j + 2 && y >= j - 2)) {
//                    return true
//                }
//            }
//        }
//        return false
//    }

}
