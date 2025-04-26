package com.lollipop.lqrdemo.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.lollipop.base.util.findFragmentByType
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.ActivityScanResultBinding
import com.lollipop.lqrdemo.preview.BarcodePreviewFragment
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.reader.BarcodeReader
import com.lollipop.qr.reader.OnBarcodeScanResultListener
import com.lollipop.qr.view.CodeSelectionView

abstract class ScanResultActivity : ColorModeActivity(),
    OnBarcodeScanResultListener,
    CodeSelectionView.OnCodeSelectedListener,
    BarcodePreviewFragment.OpenBarcodeCallback {

    companion object {
        const val ACTION_SCAN = "com.google.zxing.client.android.SCAN"
        const val SCAN_RESULT = "SCAN_RESULT"
        const val SCAN_RESULT_FORMAT = "SCAN_RESULT_FORMAT"
        const val SCAN_RESULT_BYTES = "SCAN_RESULT_BYTES"
        const val SCAN_RESULT_ORIENTATION = "SCAN_RESULT_ORIENTATION"
        const val SCAN_RESULT_ERROR_CORRECTION_LEVEL = "SCAN_RESULT_ERROR_CORRECTION_LEVEL"
    }

    protected val fromExternal: Boolean
        get() {
            return intent.action == ACTION_SCAN
        }

    private val basicBinding: ActivityScanResultBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(basicBinding.root)
        basicBinding.contentGroup.addView(
            createContentView(),
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    protected abstract fun createContentView(): View

    protected fun bindSelectionView(selectionView: CodeSelectionView, type: ImageView.ScaleType) {
        selectionView.addOnCodeSelectedListener(this)
        selectionView.scaleType = type
    }

    protected fun bindResult(reader: BarcodeReader) {
        reader.addOnBarcodeScanResultListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    protected fun testShow() {
        val fragments = findFragmentByType<BarcodePreviewFragment>()
        fragments.firstOrNull()?.testShow()
    }

    override fun onCodeSelected(code: BarcodeWrapper) {
        findFragmentByType<BarcodePreviewFragment>().firstOrNull()?.show(code)
    }

    override fun openBarcode(info: BarcodeWrapper): Boolean {
        if (fromExternal) {
            val formatName = info.describe.format.zxing.getOrNull()?.name ?: ""
            result(info.info.rawValue, formatName)
            finish()
            return true
        }
        return false
    }

    protected fun result(resultValue: String, format: String) {
        val resultIntent = Intent()
        resultIntent.putExtra(SCAN_RESULT, resultValue)
        resultIntent.putExtra(SCAN_RESULT_FORMAT, format)
        // resultIntent.putExtra("SCAN_RESULT_BYTES",OtherUtils.Bitmap2Bytes(barcode))
        // resultIntent.putExtra("SCAN_RESULT_ORIENTATION", finderView.getRotetion())
        // resultIntent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL", "")
        setResult(Activity.RESULT_OK, resultIntent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK && fromExternal) {
            val resultValue = data.getStringExtra(SCAN_RESULT) ?: ""
            if (resultValue.isNotEmpty()) {
                resultByScan(data)
            }
        }
    }

    protected open fun resultByScan(data: Intent) {
        setResult(Activity.RESULT_OK, data)
        finish()
    }

}