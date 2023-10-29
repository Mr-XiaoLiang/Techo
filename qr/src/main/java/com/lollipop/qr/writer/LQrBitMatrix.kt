package com.lollipop.qr.writer

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

//    /**
//     * 左上角定位点
//     */
//    fun inLeftTop(x: Int, y: Int): Boolean {
//        return ((x - quietZone) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE && (y - quietZone) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE)
//    }
//
//    /**
//     * 右上角定位点
//     */
//    fun inRightTop(x: Int, y: Int): Boolean {
//        return ((width - x - quietZone) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1 && (y - quietZone) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE)
//    }
//
//    /**
//     * 左下角定位点
//     */
//    fun inLeftBottom(x: Int, y: Int): Boolean {
//        return (x < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE && (height - y) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1)
//    }
//
//    /**
//     * Timing Pattern基准线
//     */
//    fun inTimingPattern(x: Int, y: Int): Boolean {
//        return x == BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1 || y == BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1
//    }
//
//    /**
//     * 是否位于格式化数据分区
//     */
//    fun inFormatInformation(width: Int, x: Int, y: Int): Boolean {
//        if (version < 7) {
//            return false
//        }
//        return ((width - x) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + BarcodeWriter.VERSION_INFORMATION_HEIGHT + 1 && y < BarcodeWriter.VERSION_INFORMATION_WIDTH)
//                || (x < BarcodeWriter.VERSION_INFORMATION_WIDTH && (width - y) < BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1 + BarcodeWriter.VERSION_INFORMATION_HEIGHT)
//
//    }
//
//    /**
//     * 判断是否在辅助定位点上
//     */
//    fun isAlignmentPattern(version: Version, width: Int, x: Int, y: Int): Boolean {
//        val apcCenterArray = version.alignmentPatternCenters
//        if (apcCenterArray.isEmpty()) {
//            return false
//        }
//        for (i in apcCenterArray) {
//            for (j in apcCenterArray) {
//                //如果这个点刚好在左上角
//                if (inLeftTop(i, j) || inLeftTop(j, i)) {
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
