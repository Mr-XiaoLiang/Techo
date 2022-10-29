package com.lollipop.qr

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Size
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor

@ExperimentalGetImage class QRReaderHelper(
    private val lifecycleOwner: LifecycleOwner
) {

    private var container: FrameLayout? = null

    private var previewView: PreviewView? = null

    var previewMode = PreviewMode.PERFORMANCE
        set(value) {
            field = value
            previewView?.implementationMode = value.mode
        }

    var scaleType = ScaleType.FILL_CENTER
        set(value) {
            field = value
            previewView?.scaleType = value.type
        }

    var scanFormat = BarcodeFormat.values()

    private val mainExecutor = MainExecutor()
    private var analyzerExecutor: SingleExecutor? = null

    private val codeAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        try {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                // Pass image to an ML Kit Vision API
                scan(image)
            }
            // after done, release the ImageProxy object
            imageProxy.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun bindContainer(layout: FrameLayout) {
        // 更换容器
        if (switchContainer(layout)) {
            return
        }
        // 将Camera的生命周期和Activity绑定在一起（设定生命周期所有者），这样就不用手动控制相机的启动和关闭。
        val cameraProviderFuture = ProcessCameraProvider.getInstance(layout.context);
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, mainExecutor)
    }

    fun onStart() {

    }

    fun onPause() {

    }

    fun onDestroy() {
        analyzerExecutor?.destroy()
        // TODO
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val view = previewView ?: return
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(view.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            buildImageAnalysis(),
            preview
        )

    }

    private fun buildImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            // 保持一定的分辨率，避免过高分辨率造成的解析压力，过低的分辨率无法分析
            .setTargetResolution(Size(1280, 720))
            // 只取最后一帧
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(getAnalyzerExecutor(), codeAnalyzer)
        return imageAnalysis
    }

    private fun getAnalyzerExecutor(): Executor {
        val executor = analyzerExecutor
        if (executor != null) {
            return executor
        }
        val newExecutor = SingleExecutor("AnalyzerExecutor")
        analyzerExecutor = newExecutor
        return newExecutor
    }

    private fun switchContainer(layout: FrameLayout): Boolean {
        container?.removeAllViews()
        val oldPreview = previewView
        if (oldPreview != null) {
            oldPreview.parent?.let {
                if (it is ViewManager) {
                    it.removeView(oldPreview)
                }
            }
        }
        val view = oldPreview ?: createPreviewView(layout.context)
        previewView = view
        container = layout
        layout.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        return view == oldPreview
    }

    private fun scan(inputImage: InputImage) {
        val array = scanFormat.map { it.code }.toIntArray()
        val first = if (array.isEmpty()) {
            Barcode.FORMAT_ALL_FORMATS
        } else {
            array[0]
        }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(first, *array)
            .build()

        val scanner = BarcodeScanning.getClient(options)
        scanner.process(inputImage).addOnSuccessListener {

        }
    }

    private fun createPreviewView(context: Context): PreviewView {
        val view = PreviewView(context)
        view.implementationMode = previewMode.mode
        view.scaleType = scaleType.type
        return view
    }

    enum class PreviewMode(val mode: PreviewView.ImplementationMode) {
        PERFORMANCE(PreviewView.ImplementationMode.PERFORMANCE),
        COMPATIBLE(PreviewView.ImplementationMode.COMPATIBLE),
    }

    enum class ScaleType(val type: PreviewView.ScaleType) {
        /**
         * 剪裁保留开始位置
         */
        FILL_START(PreviewView.ScaleType.FILL_START),

        /**
         * 剪裁保留中间位置
         */
        FILL_CENTER(PreviewView.ScaleType.FILL_CENTER),

        /**
         * 剪裁保留结束位置
         */
        FILL_END(PreviewView.ScaleType.FILL_END),

        /**
         * 缩放居上
         */
        FIT_START(PreviewView.ScaleType.FIT_START),

        /**
         * 缩放居中
         */
        FIT_CENTER(PreviewView.ScaleType.FIT_CENTER),

        /**
         * 缩放居尾
         */
        FIT_END(PreviewView.ScaleType.FIT_END);
    }

    enum class BarcodeType(val code: Int) {
        TYPE_UNKNOWN(Barcode.TYPE_UNKNOWN),
        TYPE_CONTACT_INFO(Barcode.TYPE_CONTACT_INFO),
        TYPE_EMAIL(Barcode.TYPE_EMAIL),
        TYPE_ISBN(Barcode.TYPE_ISBN),
        TYPE_PHONE(Barcode.TYPE_PHONE),
        TYPE_PRODUCT(Barcode.TYPE_PRODUCT),
        TYPE_SMS(Barcode.TYPE_SMS),
        TYPE_TEXT(Barcode.TYPE_TEXT),
        TYPE_URL(Barcode.TYPE_URL),
        TYPE_WIFI(Barcode.TYPE_WIFI),
        TYPE_GEO(Barcode.TYPE_GEO),
        TYPE_CALENDAR_EVENT(Barcode.TYPE_CALENDAR_EVENT),
        TYPE_DRIVER_LICENSE(Barcode.TYPE_DRIVER_LICENSE);
    }

    enum class BarcodeFormat(val code: Int) {
        FORMAT_CODE_128(Barcode.FORMAT_CODE_128),
        FORMAT_CODE_39(Barcode.FORMAT_CODE_39),
        FORMAT_CODE_93(Barcode.FORMAT_CODE_93),
        FORMAT_CODABAR(Barcode.FORMAT_CODABAR),
        FORMAT_DATA_MATRIX(Barcode.FORMAT_DATA_MATRIX),
        FORMAT_EAN_13(Barcode.FORMAT_EAN_13),
        FORMAT_EAN_8(Barcode.FORMAT_EAN_8),
        FORMAT_ITF(Barcode.FORMAT_ITF),
        FORMAT_QR_CODE(Barcode.FORMAT_QR_CODE),
        FORMAT_UPC_A(Barcode.FORMAT_UPC_A),
        FORMAT_UPC_E(Barcode.FORMAT_UPC_E),
        FORMAT_PDF417(Barcode.FORMAT_PDF417),
        FORMAT_AZTEC(Barcode.FORMAT_AZTEC);
    }

    private class SingleExecutor(threadName: String) : Executor {

        private val thread = HandlerThread(threadName).apply {
            start()
        }

        private val handler = Handler(thread.looper)

        override fun execute(command: Runnable?) {
            command ?: return
            handler.post(command)
        }

        fun destroy() {
            thread.quitSafely()
        }
    }

    private class MainExecutor : Executor {

        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable?) {
            command ?: return
            handler.post(command)
        }

    }
}