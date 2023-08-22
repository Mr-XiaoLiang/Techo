package com.lollipop.lqrdemo.creator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.creator.background.BackgroundGravity
import com.lollipop.lqrdemo.creator.background.BackgroundInfo
import com.lollipop.lqrdemo.creator.background.BackgroundStore
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.writer.QrWriter
import com.lollipop.lqrdemo.writer.QrWriterDistributor
import com.lollipop.lqrdemo.writer.QrWriterGroup
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.BitmapBackgroundWriterLayer
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.reader.OnBarcodeScanResultListener
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix
import java.io.File
import java.io.OutputStream
import kotlin.math.max
import kotlin.random.Random

class QrCreatorHelper(
    lifecycleOwner: LifecycleOwner,
    private val onQrChangedCallback: () -> Unit,
    private val qrCheckResult: (CheckResult) -> Unit
) : OnBarcodeScanResultListener {


    companion object {

        private fun getQrBackgroundDir(context: Context): File {
            return File(context.filesDir, "qr_background")
        }

        fun getQrBackgroundFile(context: Context): File {
            return File(getQrBackgroundDir(context), getFileName())
        }

        fun clearQrBackground(context: Context) {
            try {
                val dir = getQrBackgroundDir(context)
                dir.listFiles()?.forEach {
                    it.delete()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

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
        barcodeReader.addOnBarcodeScanResultListener(this)
    }

    fun setBackground(backgroundInfo: BackgroundInfo) {
        BackgroundStore.set(backgroundInfo, true)
        onBackgroundChanged()
    }

    fun setContextProvider(provider: QrWriter.ContextProvider?) {
        this.writerGroup.setContextProvider(provider)
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

    private fun getQrWidthByVersion(): Int {
        val matrix = bitMatrix ?: return 0
        if (matrix is LQrBitMatrix) {
            return matrix.qrWidthByVersion()
        }
        return 0
    }

    fun createShareQrBitmap(weight: Int = 10, callback: (Result<Bitmap>) -> Unit) {
        return createQrBitmap(getQrWidthByVersion() * weight, callback)
    }

    private fun createQrBitmap(width: Int = -1, callback: (Result<Bitmap>) -> Unit) {
        doAsync({
            onUI { callback(Result.failure(it)) }
        }) {
            val matrix = bitMatrix
            if (matrix == null) {
                onUI { callback(Result.failure(RuntimeException("BitMatrix is null"))) }
                return@doAsync
            }
            val widthByVersion = getQrWidthByVersion()

            var qrWidth = max(widthByVersion, width)
            if (qrWidth < 21) {
                qrWidth = matrix.width
            }
            saveWriterDistributor.setBounds(0, 0, qrWidth, qrWidth)
            saveWriterDistributor.updateLayerResource {
                onSaveWriterResourceReady(qrWidth, callback)
            }
        }
    }

    private fun onSaveWriterResourceReady(qrWidth: Int, callback: (Result<Bitmap>) -> Unit) {
        doAsync({
            onUI { callback(Result.failure(it)) }
        }) {
            val outBitmap = Bitmap.createBitmap(qrWidth, qrWidth, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outBitmap)
            saveWriterDistributor.draw(canvas)
            callback(Result.success(outBitmap))
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
        val qrSize = getQrWidthByVersion() * 4
        createQrBitmap(qrSize) { result ->
            val bitmap = result.getOrNull()
            if (bitmap == null) {
                onUI {
                    setCheckResult(currentMode, CheckResult.ERROR)
                }
            } else {
                doAsync({
                    it.printStackTrace()
                    onUI {
                        setCheckResult(currentMode, CheckResult.ERROR)
                    }
                }) {
                    val bitmapSize = qrSize + 100
                    val scanBitmap = Bitmap.createBitmap(
                        bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(scanBitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.translate(50F, 50F)
                    canvas.drawBitmap(bitmap, 0F, 0F, null)
                    bitmap.recycle()
                    onUI {
                        barcodeReader.read(scanBitmap, 0, modeKey)
                    }
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

    fun setBackground(layer: Class<out BackgroundWriterLayer>?) {
        writerGroup.setBackground(layer)
        // 这样其实是不对的, 但是先这样写吧。背景内容的方式应该再改改，不能各弄各的
        if (layer == BitmapBackgroundWriterLayer::class.java) {
            writerGroup.setQrPointColor(Color.BLACK, Color.TRANSPARENT)
        } else {
            writerGroup.setQrPointColor(Color.BLACK, Color.WHITE)
        }
        onBackgroundChanged()
    }

    fun onBackgroundChanged() {
        writerGroup.notifyBackgroundChanged()
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
            val value = barcode?.info?.rawValue ?: ""
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