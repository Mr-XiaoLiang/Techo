package com.lollipop.lqrdemo.creator

import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.creator.bridge.OnCodeMatrixChangedListener
import com.lollipop.qr.writer.BarcodeWriter
import com.lollipop.qr.writer.LBitMatrix

class QrCreatorHelper(private val lifecycleOwner: LifecycleOwner) {

    var contentValue: String = ""
        set(value) {
            field = value
            onContentChanged()
        }

    private var bitMatrix: LBitMatrix? = null

    private val codeMatrixChangedListener = ListenerManager<OnCodeMatrixChangedListener>()
    private val codeContentChangedListener = ListenerManager<OnCodeContentChangedListener>()

    private fun onContentChanged() {
        val content = contentValue
        codeContentChangedListener.invoke { it.onCodeContentChanged(content) }
        val matrix = createBitMatrix()
        codeMatrixChangedListener.invoke { it.onCodeMatrixChanged(matrix) }
        // TODO
    }

    private fun createBitMatrix(): LBitMatrix? {
        val result = BarcodeWriter(lifecycleOwner).encode(contentValue).build()
        val matrix = result.getOrNull()
        bitMatrix = matrix
        return matrix
    }

}