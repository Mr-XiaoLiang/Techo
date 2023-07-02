package com.lollipop.lqrdemo.creator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.writer.QrWriter
import com.lollipop.lqrdemo.writer.QrWriterDistributor
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.writer.LBitMatrix
import java.io.OutputStream
import kotlin.random.Random

class QrCreatorHelper(private val lifecycleOwner: LifecycleOwner) {

    var contentValue: String = ""
        set(value) {
            field = value
            onContentChanged()
        }

    private var bitMatrix: LBitMatrix? = null

    private val codeContentChangedListener = ListenerManager<OnCodeContentChangedListener>()
    private val loadStatusChangedListener = ListenerManager<OnLoadStatusChangedListener>()

    companion object {

        private fun getFileName(): String {
            val fileCode = Random.nextInt(0xF0000, 0xFFFFF).toString(16).uppercase()
            return "LQR_$fileCode.png"
        }

        fun saveToMediaStore(context: Context, bitmap: Bitmap) {
            val contentResolver = context.applicationContext.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageInQ(bitmap, contentResolver)
            } else {
                MediaStore.Images.Media.insertImage(
                    contentResolver, bitmap,
                    getFileName(),
                    ""
                )
            }
        }

        private fun saveImageInQ(bitmap: Bitmap, contentResolver: ContentResolver): Uri? {
            val filename = getFileName()
            val fos: OutputStream?
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            //use application context to get contentResolver
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let { contentResolver.openOutputStream(it) }.also { fos = it }
            fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            fos?.flush()
            fos?.close()

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.let {
                contentResolver.update(it, contentValues, null, null)
            }
            return uri
        }
    }

    private val previewWriterDistributor = QrWriterDistributor()

    val previewWriter: QrWriter
        get() {
            return previewWriterDistributor
        }


    private fun onContentChanged() {
        val content = contentValue
        codeContentChangedListener.invoke { it.onCodeContentChanged(content) }
        loadStatusChangedListener.invoke { it.onLoadStatusChanged(true) }
        doAsync {
            val matrix = createBitMatrix()
            previewWriterDistributor.setBitMatrix(matrix)
            onUI {
                loadStatusChangedListener.invoke { it.onLoadStatusChanged(false) }
            }
        }
    }

    private fun createBitMatrix(): LBitMatrix? {
        val result = BarcodeHelper.createWriter(lifecycleOwner).encode(contentValue).build()
        val matrix = result.getOrNull()
        bitMatrix = matrix
        return matrix
    }

    fun interface OnLoadStatusChangedListener {

        fun onLoadStatusChanged(isLading: Boolean)

    }

}