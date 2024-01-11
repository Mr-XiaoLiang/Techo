package com.lollipop.qr.writer

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.decoder.Version
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.max
import kotlin.math.min

class LQRCodeWriter(private val writerType: WriterType = WriterType.DEFAULT) : Writer {

    companion object {
        private const val QUIET_ZONE_SIZE = 4

        /**
         * 获取二维码的Version
         * 通过宽度来计算宽度
         */
        fun getVersion(width: Int) = (width - 21) / 4 + 1

        /**
         * getVersion 的反向操作，计算最小可用宽度
         */
        fun getMinWidth(version: Int) = ((version - 1) * 4) + 21

        /**
         * 左上角定位点
         */
        fun inLeftTop(x: Int, y: Int): Boolean {
            return (x <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE && y <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE)
        }

        /**
         * 右上角定位点
         */
        fun inRightTop(width: Int, x: Int, y: Int): Boolean {
            return ((width - x) <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1 && y <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE)
        }

        /**
         * 左下角定位点
         */
        fun inLeftBottom(height: Int, x: Int, y: Int): Boolean {
            return (x <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE && (height - y) <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1)
        }

        /**
         * Timing Pattern基准线
         */
        fun inTimingPattern(x: Int, y: Int): Boolean {
            return x == BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1 || y == BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE - 1
        }

        /**
         * 是否位于格式化数据分区
         */
        fun inFormatInformation(width: Int, x: Int, y: Int): Boolean {
            if (getVersion(width) < 7) {
                return false
            }
            return ((width - x) <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + BarcodeWriter.VERSION_INFORMATION_HEIGHT + 1 && y <= BarcodeWriter.VERSION_INFORMATION_WIDTH)
                    || (x <= BarcodeWriter.VERSION_INFORMATION_WIDTH && (width - y) <= BarcodeWriter.POSITION_DETECTION_PATTERN_SIZE + 1 + BarcodeWriter.VERSION_INFORMATION_HEIGHT)

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
                    //判断是否是在范围内
                    if ((x <= j + 2 && x >= j - 2) && (y <= i + 2 && y >= i - 2)) {
                        return true
                    }
                }
            }
            return false
        }
    }

    enum class WriterType {
        DEFAULT,
        MINI
    }

    override fun encode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int
    ): BitMatrix {
        return encode(contents, format, width, height, null)
    }

    override fun encode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        hints: Map<EncodeHintType, *>?
    ): BitMatrix {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Requested dimensions are too small: $width x $height")
        }
        val bean = encodeTo(contents, format, width, height, hints)
        return renderResult(bean.qrCode, bean.width, bean.height, bean.quietZone)
    }

    fun encode2(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        hints: Map<EncodeHintType, *>? = null
    ): LQrBitMatrix {
        val bean = encodeTo(contents, format, width, height, hints)
        return renderResultLQR(bean.qrCode, bean.width, bean.height, bean.quietZone)
    }

    /**
     * 构建原始的二维码的数据
     * 它的目的是为了更自由的定制二维码
     */
    fun encodeOriginal(
        contents: String,
        hints: Map<EncodeHintType, *>? = null
    ): QRCode {
        var errorCorrectionLevel = ErrorCorrectionLevel.L
        if (hints != null) {
            if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                errorCorrectionLevel =
                    ErrorCorrectionLevel.valueOf(hints[EncodeHintType.ERROR_CORRECTION].toString())
            }
        }
        return Encoder.encode(contents, errorCorrectionLevel, hints)
    }

    private fun encodeTo(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        hints: Map<EncodeHintType, *>? = null
    ): CodeBean {

        if (contents.isEmpty()) {
            throw IllegalArgumentException("Found empty contents")
        }

        if (format != BarcodeFormat.QR_CODE) {
            throw IllegalArgumentException("Can only encode QR_CODE, but got $format")
        }

        var quietZone = QUIET_ZONE_SIZE
        if (hints != null) {
            if (hints.containsKey(EncodeHintType.MARGIN)) {
                quietZone = Integer.parseInt(hints[EncodeHintType.MARGIN].toString())
            }
        }
        val code = encodeOriginal(contents, hints)
        val minWidth = getMinWidth(code.version.versionNumber)
        val codeWidth = max(minWidth, width)
        val codeHeight = max(minWidth, height)
        return CodeBean(code, codeWidth, codeHeight, quietZone)
    }

    private class CodeBean(val qrCode: QRCode, val width: Int, val height: Int, val quietZone: Int)

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private fun renderResult(code: QRCode, width: Int, height: Int, quietZone: Int): BitMatrix {
        val input = code.matrix ?: throw IllegalStateException()
        val inputWidth = input.width
        val inputHeight = input.height
        val qrWidth = inputWidth + quietZone * 2
        val qrHeight = inputHeight + quietZone * 2
        val outputWidth = max(width, qrWidth)
        val outputHeight = max(height, qrHeight)

        val multiple = min(outputWidth / qrWidth, outputHeight / qrHeight)
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        val leftPadding = (outputWidth - inputWidth * multiple) / 2
        val topPadding = (outputHeight - inputHeight * multiple) / 2

        val output = BitMatrix(outputWidth, outputHeight)

        var inputY = 0
        var outputY = topPadding

        while (inputY < inputHeight) {
            // Write the contents of this row of the barcode
            var inputX = 0
            var outputX = leftPadding
            while (inputX < inputWidth) {
                if (input.get(inputX, inputY).toInt() == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple)
                }
                inputX++
                outputX += multiple
            }
            inputY++
            outputY += multiple
        }
        return output
    }

    private fun renderResultLQR(code: QRCode, w: Int, h: Int, quietZone: Int): LQrBitMatrix {
        val input = code.matrix ?: throw IllegalStateException()
        val version = code.version
        val inputWidth = input.width
        val inputHeight = input.height
        var width = w
        var height = h

        val qrWidth = inputWidth + quietZone * 2
        val qrHeight = inputHeight + quietZone * 2

        if (writerType == WriterType.MINI) {
            if (width < 0) {
                width = inputWidth * 3 + quietZone * 2
            }
            if (height < 0) {
                height = inputHeight * 3 + quietZone * 2
            }
        } else {
            if (width < 0) {
                width = qrWidth
            }
            if (height < 0) {
                height = qrHeight
            }
        }

        val outputWidth = max(width, qrWidth)
        val outputHeight = max(height, qrHeight)

        val multiple = min(outputWidth / qrWidth, outputHeight / qrHeight)
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        val leftPadding = (outputWidth - inputWidth * multiple) / 2
        val topPadding = (outputHeight - inputHeight * multiple) / 2

        val output = LQrBitMatrix(code, quietZone, outputWidth, outputHeight)

        var outputY = topPadding

        for (inputY in 0 until inputHeight) {
            // Write the contents of this row of the barcode
            var outputX = leftPadding
            for (inputX in 0 until inputWidth) {

                val type = if (input.get(inputX, inputY).toInt() == 1) {
                    LBitMatrix.Type.BLACK
                } else {
                    LBitMatrix.Type.WHITE
                }

                when (writerType) {

                    WriterType.DEFAULT -> {
                        output.setRegion(outputX, outputY, multiple, multiple, type)
                    }

                    WriterType.MINI -> {
                        if (isBody(version, inputWidth, inputX, inputY)) {

                            output.setRegionOneInNine(outputX, outputY, multiple, multiple, type)

                        } else {

                            output.setRegion(outputX, outputY, multiple, multiple, type)

                        }
                    }

                }

                outputX += multiple
            }
            outputY += multiple
        }
        return output
    }

    private fun isBody(version: Version, width: Int, x: Int, y: Int): Boolean {
        //左上角定位点、定位点分离层、格式化数据
        if (inLeftTop(x, y)) {
            return false
        }
        //右上角定位点、定位点分离层、格式化数据
        if (inRightTop(width, x, y)) {
            return false
        }
        //左下角定位点、定位点分离层、格式化数据
        if (inLeftBottom(width, x, y)) {
            return false
        }

        //Timing Pattern基准线
        if (inTimingPattern(x, y)) {
            return false
        }

        //辅助定位点
        if (isAlignmentPattern(version, width, x, y)) {
            return false
        }

        return true
    }
}