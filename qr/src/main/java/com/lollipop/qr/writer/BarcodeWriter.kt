package com.lollipop.qr.writer

import androidx.lifecycle.LifecycleOwner
import com.google.zxing.qrcode.decoder.Version
import com.lollipop.qr.comm.BarcodeExecutor

class BarcodeWriter(
    lifecycleOwner: LifecycleOwner
) : BarcodeExecutor(lifecycleOwner) {

    companion object {
        /** 三个方位角的定位点尺寸 **/
        const val POSITION_DETECTION_PATTERN_SIZE = 7

        /** 辅助定位点尺寸 **/
        const val ALIGNMENT_PATTERN_SIZE = 5

        /** Version Information 的高度 **/
        const val VERSION_INFORMATION_HEIGHT = 3

        /** Version Information 的宽度 **/
        const val VERSION_INFORMATION_WIDTH = 6

        /**
         * 获取二维码的Version
         * 通过宽度来计算宽度
         */
        fun getVersion(width: Int) = (width - 21) / 4 + 1

        /**
         * 左上角定位点
         */
        fun inLeftTop(x: Int, y: Int): Boolean {
            return (x < POSITION_DETECTION_PATTERN_SIZE && y < POSITION_DETECTION_PATTERN_SIZE)
        }

        /**
         * 右上角定位点
         */
        fun inRightTop(width: Int, x: Int, y: Int): Boolean {
            return ((width - x) < POSITION_DETECTION_PATTERN_SIZE + 1 && y < POSITION_DETECTION_PATTERN_SIZE)
        }

        /**
         * 左下角定位点
         */
        fun inLeftBottom(height: Int, x: Int, y: Int): Boolean {
            return (x < POSITION_DETECTION_PATTERN_SIZE && (height - y) < POSITION_DETECTION_PATTERN_SIZE + 1)
        }

        /**
         * Timing Pattern基准线
         */
        fun inTimingPattern(x: Int, y: Int): Boolean {
            return x == POSITION_DETECTION_PATTERN_SIZE - 1 || y == POSITION_DETECTION_PATTERN_SIZE - 1
        }

        /**
         * 是否位于格式化数据分区
         */
        fun inFormatInformation(width: Int, x: Int, y: Int): Boolean {
            if (getVersion(width) < 7) {
                return false
            }
            return ((width - x) < POSITION_DETECTION_PATTERN_SIZE + VERSION_INFORMATION_HEIGHT + 1 && y < VERSION_INFORMATION_WIDTH)
                    || (x < VERSION_INFORMATION_WIDTH && (width - y) < POSITION_DETECTION_PATTERN_SIZE + 1 + VERSION_INFORMATION_HEIGHT)

        }

        /**
         * 判断是否在辅助定位点上
         */
        fun isAlignmentPattern(version: Version, width: Int, x: Int, y: Int): Boolean {
            val apcCenterArray = version.alignmentPatternCenters
            if (apcCenterArray.isEmpty()) {
                return false
            }
            for (i in apcCenterArray) {
                for (j in apcCenterArray) {
                    //如果这个点刚好在左上角
                    if (inLeftTop(i, j) || inLeftTop(j, i)) {
                        continue
                    }
                    //如果这个点刚好在右上角
                    if (inRightTop(width, i, j) || inRightTop(width, j, i)) {
                        continue
                    }
                    //如果这个点刚好在左下角
                    if (inLeftBottom(width, i, j) || inLeftBottom(width, j, i)) {
                        continue
                    }
                    //判断是否是在范围内
                    if ((x <= i + 2 && x >= i - 2) && (y <= j + 2 && y >= j - 2)) {
                        return true
                    }
//                //判断是否是在范围内
//                if(( x < j+2 && x > j-2 ) && ( y < i+2 && y > i-2 )){
//                    return true
//                }
                }
            }
            return false
        }
    }

}