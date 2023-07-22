package com.lollipop.qr.writer

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.encoder.QRCode


/**
 * Created by lollipop on 2018/3/16.
 * @author Lollipop
 * 一个包装的矩阵，因为在二维码缩放模式下，
 * 需要标记清除允许透明的部分，黑色部分，白色部分
 */
class LQrBitMatrix(private val qrCode: QRCode, width: Int, height: Int = width): LBitMatrix(width, height) {

    companion object {
        fun copyOf(bitMatrix: LQrBitMatrix): LQrBitMatrix {
            val lqrMatrix = LQrBitMatrix(bitMatrix.qrCode, bitMatrix.width, bitMatrix.height)
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

    fun qrVersion(): Int {
        return qrCode.version?.versionNumber ?: 0
    }

    fun qrWidthByVersion(): Int {
        val version = qrVersion()
        if (version < 1) {
            return 0
        }
        return LQRCodeWriter.getMinWidth(version)
    }

}
