package com.lollipop.lqrdemo.base

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import com.lollipop.lqrdemo.BarcodeDetailDialog
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.reader.BarcodeReader
import com.lollipop.qr.reader.OnBarcodeScanResultListener
import com.lollipop.qr.view.CodeSelectionView

abstract class ScanResultActivity : ColorModeActivity(),
    OnBarcodeScanResultListener,
    CodeSelectionView.OnCodeSelectedListener,
    BarcodeDetailDialog.OpenBarcodeCallback {

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


    override fun onCodeSelected(code: BarcodeWrapper) {
        BarcodeDetailDialog.show(
            this,
            code,
            if (fromExternal) {
                this
            } else {
                null
            }
        )
    }

    override fun openBarcode(info: BarcodeWrapper) {
        val formatName = info.describe.format.zxing.getOrNull()?.name ?: ""
        result(info.info.rawValue, formatName)
        finish()
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
        finish()
    }

}