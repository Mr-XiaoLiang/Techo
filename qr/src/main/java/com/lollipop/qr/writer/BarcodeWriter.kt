package com.lollipop.qr.writer

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.encoder.QRCode
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.comm.BarcodeExecutor
import kotlin.math.max
import kotlin.math.min

open class BarcodeWriter(
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

    }

    fun encode(content: String): Builder {
        return Builder(this, content)
    }

    private fun loadBitmap(builder: Builder, base: Bitmap?, callback: (Result<Bitmap>) -> Unit) {
        doAsync {
            val result = builder.drawBitmap(base)
            onUI {
                callback(result)
            }
        }
    }

    private fun loadBitMatrix(builder: Builder, callback: (Result<LBitMatrix>) -> Unit) {
        doAsync {
            val result = builder.build()
            onUI {
                callback(result)
            }
        }
    }

    private fun encode(
        info: String,
        format: com.google.zxing.BarcodeFormat,
        miniQR: Boolean,
        width: Int,
        height: Int,
        hints: Map<com.google.zxing.EncodeHintType, Any>
    ): LBitMatrix {
        return if (format == com.google.zxing.BarcodeFormat.QR_CODE) {
            LQRCodeWriter(
                if (miniQR) {
                    LQRCodeWriter.WriterType.MINI
                } else {
                    LQRCodeWriter.WriterType.DEFAULT
                }
            ).encode2(info, format, width, height, hints)
        } else {
            LBitMatrix.copyOf(
                MultiFormatWriter().encode(
                    info,
                    format,
                    // 其他的码不确定尺寸，就固定最小为144吧
                    max(width, 144),
                    height,
                    hints
                )
            )
        }
    }

    private fun drawBitmap(
        bitMatrix: LBitMatrix,
        darkColor: Int,
        lightColor: Int,
        outBitmap: Bitmap,
        src: Bitmap?
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        //创建一个空的像素数组
        val pixelArray = IntArray(width * height) { Color.TRANSPARENT }
        //如果有原始图片，那么就将它复制到现有像素数组
        src?.getPixels(pixelArray, 0, width, 0, 0, width, height)
        //将二维码赋值到现有像素数组
        getPixelArray(bitMatrix, pixelArray, darkColor, lightColor)
        //将像素数组赋值到图片中
        outBitmap.setPixels(pixelArray, 0, width, 0, 0, width, height)
        return outBitmap
    }

    private fun getPixelArray(
        bitMatrix: LBitMatrix,
        pixelArray: IntArray,
        darkColor: Int,
        lightColor: Int
    ): IntArray {
        val width = bitMatrix.width
        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                if (bitMatrix.isNotNull(x, y)) {
                    pixelArray[y * width + x] = if (bitMatrix.isBlack(x, y)) {
                        darkColor
                    } else {
                        lightColor
                    }
                }
            }
        }
        return pixelArray
    }

    class Builder(
        private val writer: BarcodeWriter,
        private val content: String
    ) {

        private var width = 0
        private var height = 0
        private var format = BarcodeFormat.QR_CODE
        private var miniDataPoint = false
        private var hints = HashMap<com.google.zxing.EncodeHintType, Any>()
        private var darkColor = Color.BLACK
        private var lightColor = Color.WHITE

        init {
            characterSet("utf-8")
            // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            errorCorrection(ErrorCorrectionLevel.H)
        }

        fun build(): Result<LBitMatrix> {
            try {
                val formatZxing = format.zxing.getOrNull()
                    ?: return Result.failure(
                        java.lang.IllegalArgumentException("format is $format")
                    )
                return Result.success(
                    writer.encode(content, formatZxing, miniDataPoint, width, height, hints)
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                return Result.failure(e)
            }
        }

        fun drawBitmap(base: Bitmap?): Result<Bitmap> {
            try {
                val matrixResult = build()
                val matrix = matrixResult.getOrNull() ?: return Result.failure(
                    matrixResult.exceptionOrNull()
                        ?: java.lang.IllegalArgumentException("encode error: $matrixResult")
                )
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                writer.drawBitmap(matrix, darkColor, lightColor, bitmap, base)
                return Result.success(bitmap)
            } catch (e: Throwable) {
                e.printStackTrace()
                return Result.failure(e)
            }
        }

        fun loadBitMatrix(callback: (Result<LBitMatrix>) -> Unit) {
            writer.loadBitMatrix(this, callback)
        }

        fun loadBitmap(base: Bitmap?, callback: (Result<Bitmap>) -> Unit) {
            writer.loadBitmap(this, base, callback)
        }

        fun size(width: Int, height: Int = width): Builder {
            this.width = width
            this.height = height
            return this
        }

        fun format(format: BarcodeFormat): Builder {
            this.format = format
            return this
        }

        fun miniPointMode(mode: Boolean = true): Builder {
            this.miniDataPoint = mode
            return this
        }

        fun color(dark: Int, light: Int): Builder {
            this.darkColor = dark
            this.lightColor = light
            return this
        }

        /**
         * Specifies what degree of error correction to use, for example in QR Codes.
         * Type depends on the encoder. For example for QR codes it's type
         * [ErrorCorrectionLevel][com.google.zxing.qrcode.decoder.ErrorCorrectionLevel].
         * For Aztec it is of type [Integer], representing the minimal percentage of error correction words.
         * For PDF417 it is of type [Integer], valid values being 0 to 8.
         * In all cases, it can also be a [String] representation of the desired value as well.
         * Note: an Aztec symbol should have a minimum of 25% EC words.
         */
        fun errorCorrection(level: ErrorCorrectionLevel): Builder {
            return putHints(EncodeHintType.ERROR_CORRECTION, level)
        }

        /**
         * Specifies what character encoding to use where applicable (type [String])
         */
        fun characterSet(charSet: String): Builder {
            return putHints(EncodeHintType.CHARACTER_SET, charSet)
        }

        /**
         * Specifies the matrix shape for Data Matrix (type [com.google.zxing.datamatrix.encoder.SymbolShapeHint])
         */
        fun dataMatrixShape(shapeHint: SymbolShapeHint): Builder {
            return putHints(EncodeHintType.DATA_MATRIX_SHAPE, shapeHint)
        }

        /**
         * Specifies whether to use compact mode for Data Matrix (type [Boolean], or "true" or "false"
         * [String] value).
         * The compact encoding mode also supports the encoding of characters that are not in the ISO-8859-1
         * character set via ECIs.
         * Please note that in that case, the most compact character encoding is chosen for characters in
         * the input that are not in the ISO-8859-1 character set. Based on experience, some scanners do not
         * support encodings like cp-1256 (Arabic). In such cases the encoding can be forced to UTF-8 by
         * means of the [.CHARACTER_SET] encoding hint.
         * Compact encoding also provides GS1-FNC1 support when [.GS1_FORMAT] is selected. In this case
         * group-separator character (ASCII 29 decimal) can be used to encode the positions of FNC1 codewords
         * for the purpose of delimiting AIs.
         * This option and [.FORCE_C40] are mutually exclusive.
         */
        fun dataMatrixCompact(compact: Boolean): Builder {
            return putHints(EncodeHintType.DATA_MATRIX_COMPACT, compact)
        }

        /**
         * Specifies margin, in pixels, to use when generating the barcode. The meaning can vary
         * by format; for example it controls margin before and after the barcode horizontally for
         * most 1D formats. (Type [Integer], or [String] representation of the integer value).
         */
        fun margin(px: Int): Builder {
            return putHints(EncodeHintType.MARGIN, px)
        }

        /**
         * Specifies whether to use compact mode for PDF417 (type [Boolean], or "true" or "false"
         * [String] value).
         */
        fun pdf417Compact(compact: Boolean): Builder {
            return putHints(EncodeHintType.PDF417_COMPACT, compact)
        }

        /**
         * Specifies what compaction mode to use for PDF417 (type
         * [Compaction][com.google.zxing.pdf417.encoder.Compaction] or [String] value of one of its
         * enum values).
         */
        fun pdf417Compaction(compaction: Pdf417Compaction): Builder {
            return putHints(EncodeHintType.PDF417_COMPACTION, compaction)
        }

        /**
         * Specifies the minimum and maximum number of rows and columns for PDF417 (type
         * [Dimensions][com.google.zxing.pdf417.encoder.Dimensions]).
         */
        fun pdf417Dimensions(minCols: Int, maxCols: Int, minRows: Int, maxRows: Int): Builder {
            return putHints(
                EncodeHintType.PDF417_DIMENSIONS,
                com.google.zxing.pdf417.encoder.Dimensions(minCols, maxCols, minRows, maxRows)
            )
        }

        /**
         * Specifies whether to automatically insert ECIs when encoding PDF417 (type [Boolean], or "true" or "false"
         * [String] value).
         * Please note that in that case, the most compact character encoding is chosen for characters in
         * the input that are not in the ISO-8859-1 character set. Based on experience, some scanners do not
         * support encodings like cp-1256 (Arabic). In such cases the encoding can be forced to UTF-8 by
         * means of the [.CHARACTER_SET] encoding hint.
         */
        fun pdf417AutoEci(enable: Boolean): Builder {
            return putHints(EncodeHintType.PDF417_AUTO_ECI, enable)
        }

        /**
         * Specifies the required number of layers for an Aztec code.
         * A negative number (-1, -2, -3, -4) specifies a compact Aztec code.
         * 0 indicates to use the minimum number of layers (the default).
         * A positive number (1, 2, .. 32) specifies a normal (non-compact) Aztec code.
         * (Type [Integer], or [String] representation of the integer value).
         */
        fun aztecLayers(layers: Int): Builder {
            return putHints(EncodeHintType.AZTEC_LAYERS, max(min(layers, 32), -4))
        }

        /**
         * Specifies the exact version of QR code to be encoded.
         * (Type [Integer], or [String] representation of the integer value).
         */
        fun qrVersion(version: Int): Builder {
            return putHints(EncodeHintType.QR_VERSION, version)
        }

        /**
         * Specifies the QR code mask pattern to be used. Allowed values are
         * 0..QRCode.NUM_MASK_PATTERNS-1. By default the code will automatically select
         * the optimal mask pattern.
         * * (Type [Integer], or [String] representation of the integer value).
         */
        fun qrMaskPattern(pattern: Int): Builder {
            return putHints(
                EncodeHintType.QR_MASK_PATTERN,
                max(min(pattern, QRCode.NUM_MASK_PATTERNS - 1), 0)
            )
        }

        /**
         * Specifies whether to use compact mode for QR code (type [Boolean], or "true" or "false"
         * [String] value).
         * Please note that when compaction is performed, the most compact character encoding is chosen
         * for characters in the input that are not in the ISO-8859-1 character set. Based on experience,
         * some scanners do not support encodings like cp-1256 (Arabic). In such cases the encoding can
         * be forced to UTF-8 by means of the [.CHARACTER_SET] encoding hint.
         */
        fun qrCompact(compact: Boolean): Builder {
            return putHints(EncodeHintType.QR_COMPACT, compact)
        }

        /**
         * Specifies whether the data should be encoded to the GS1 standard (type [Boolean], or "true" or "false"
         * [String] value).
         */
        fun gs1Format(format: Boolean): Builder {
            return putHints(EncodeHintType.GS1_FORMAT, format)
        }

        /**
         * Forces which encoding will be used. Currently only used for Code-128 code sets (Type [String]).
         * Valid values are "A", "B", "C".
         * This option and [.CODE128_COMPACT] are mutually exclusive.
         */
        fun forceCodeSet(value: ForceCodeSet): Builder {
            return putHints(EncodeHintType.FORCE_CODE_SET, value)
        }

        /**
         * Forces C40 encoding for data-matrix (type [Boolean], or "true" or "false") [String] value). This
         * option and [.DATA_MATRIX_COMPACT] are mutually exclusive.
         */
        fun forceC40(value: Boolean): Builder {
            return putHints(EncodeHintType.FORCE_C40, value)
        }

        /**
         * Specifies whether to use compact mode for Code-128 code (type [Boolean], or "true" or "false"
         * [String] value).
         * This can yield slightly smaller bar codes. This option and [.FORCE_CODE_SET] are mutually
         * exclusive.
         */
        fun code128Compact(compact: Boolean): Builder {
            return putHints(EncodeHintType.CODE128_COMPACT, compact)
        }

        private fun putHints(type: EncodeHintType, value: Any): Builder {
            hints[type.zxing] = if (value is ZxingWrapper<*>) {
                value.zxing
            } else {
                value
            }
            return this
        }

    }

}