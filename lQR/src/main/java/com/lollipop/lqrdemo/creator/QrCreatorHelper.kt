package com.lollipop.lqrdemo.creator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.qrcode.QRCodeReader
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.writer.QrWriter
import com.lollipop.lqrdemo.writer.QrWriterDistributor
import com.lollipop.lqrdemo.writer.QrWriterGroup
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.reader.OnBarcodeScanResultListener
import com.lollipop.qr.writer.LBitMatrix
import java.io.OutputStream
import kotlin.random.Random

class QrCreatorHelper(
    lifecycleOwner: LifecycleOwner,
    private val onQrChangedCallback: () -> Unit,
    private val qrCheckResult: (CheckResult) -> Unit
): OnBarcodeScanResultListener {


    companion object {

        private fun getFileName(): String {
            val fileCode = Random.nextInt(0xF0000, 0xFFFFF).toString(16).uppercase()
            return "LQR_$fileCode.png"
        }

        fun saveToMediaStore(context: Context, bitmap: Bitmap): Uri? {
            val contentResolver = context.applicationContext.contentResolver
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageInQ(bitmap, contentResolver)
            } else {
                val uri = MediaStore.Images.Media.insertImage(
                    contentResolver, bitmap,
                    getFileName(),
                    ""
                )
                Uri.parse(uri)
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

    var contentValue: String = ""
        set(value) {
            field = value
            onContentChanged()
        }

    private var bitMatrix: LBitMatrix? = null

    private val codeContentChangedListener = ListenerManager<OnCodeContentChangedListener>()
    private val loadStatusChangedListener = ListenerManager<OnLoadStatusChangedListener>()


    private val previewWriterDistributor = QrWriterDistributor()
    private val saveWriterDistributor = QrWriterDistributor()
    private val writerGroup = QrWriterGroup()

    private val barcodeWriter = BarcodeHelper.createWriter(lifecycleOwner)
    private val barcodeReader = BarcodeHelper.createLocalReader(lifecycleOwner)

    private var qrCheckMode = 0

    val previewWriter: QrWriter
        get() {
            return previewWriterDistributor
        }

    init {
        writerGroup.addWriter(saveWriterDistributor)
        writerGroup.addWriter(previewWriterDistributor)
    }

    fun addLoadStatusChangedListener(listener: OnLoadStatusChangedListener) {
        this.loadStatusChangedListener.addListener(listener)
    }

    fun removeLoadStatusChangedListener(listener: OnLoadStatusChangedListener) {
        this.loadStatusChangedListener.removeListener(listener)
    }

    fun addContentChangedListener(listener: OnCodeContentChangedListener) {
        this.codeContentChangedListener.addListener(listener)
    }

    fun removeContentChangedListener(listener: OnCodeContentChangedListener) {
        this.codeContentChangedListener.removeListener(listener)
    }

    fun createQrBitmap(width: Int = 512): Result<Bitmap> {
        return try {
            saveWriterDistributor.setBounds(0, 0, width, width)
            val outBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outBitmap)
            saveWriterDistributor.draw(canvas)
            Result.success(outBitmap)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun checkQr() {
        if (contentValue.isEmpty()) {
            qrCheckResult(CheckResult.EMPTY)
            return
        }
        qrCheckMode++
        val currentMode = qrCheckMode
        val modeKey = currentMode.toString()
        doAsync({
            onUI {
                setCheckResult(currentMode, CheckResult.ERROR)
            }
        }) {
            val bitmap = createQrBitmap().getOrNull()
            if (bitmap != null) {
                barcodeReader.read(bitmap, 0, modeKey)
            } else {
                onUI {
                    setCheckResult(currentMode, CheckResult.ERROR)
                }
            }
        }
    }

    private fun setCheckResult(modeKey: Int, result: CheckResult) {
        if (qrCheckMode == modeKey) {
            qrCheckResult(result)
        }
    }

    private fun setCheckResult(modeKey: String, result: CheckResult) {
        if (qrCheckMode.toString() == modeKey) {
            qrCheckResult(result)
        }
    }

    private fun onContentChanged() {
        val content = contentValue
        codeContentChangedListener.invoke { it.onCodeContentChanged(content) }
        notifyLoadingStart()
        doAsync {
            val matrix = createBitMatrix()
            writerGroup.setBitMatrix(matrix)
            onUI {
                notifyLoadingEnd()
                onChanged()
            }
        }
    }

    fun setBackground(layer: Class<BackgroundWriterLayer>) {
        writerGroup.setBackground(layer)
        onChanged()
    }

    private fun notifyLoadingStart() {
        loadStatusChangedListener.invoke { it.onLoadStatusChanged(true) }
    }

    private fun notifyLoadingEnd() {
        loadStatusChangedListener.invoke { it.onLoadStatusChanged(false) }
    }

    private fun createBitMatrix(): LBitMatrix? {
        val result = barcodeWriter.encode(contentValue).build()
        val matrix = result.getOrNull()
        bitMatrix = matrix
        return matrix
    }

    private fun onChanged() {
        onQrChangedCallback()
        checkQr()
    }

    override fun onBarcodeScanResult(result: BarcodeResult) {
       val type = if (result.isEmpty) {
           CheckResult.ERROR
        } else {
           val barcode = result.list.getOrNull(0)
           val value = barcode?.info?.rawValue?:""
           if (value != contentValue) {
               CheckResult.ERROR
           } else {
               CheckResult.SUCCESSFUL
           }
       }
        setCheckResult(result.tag, type)
    }

    fun interface OnLoadStatusChangedListener {

        fun onLoadStatusChanged(isLading: Boolean)

    }

    enum class CheckResult {
        SUCCESSFUL,
        ERROR,
        EMPTY
    }


}