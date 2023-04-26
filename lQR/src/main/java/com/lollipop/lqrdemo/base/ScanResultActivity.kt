package com.lollipop.lqrdemo.base

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.lqrdemo.BarcodeDetailDialog
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.reader.BarcodeReader
import com.lollipop.qr.reader.OnBarcodeScanResultListener
import com.lollipop.qr.view.CodeSelectionView

abstract class ScanResultActivity : ColorModeActivity(), OnBarcodeScanResultListener,
    CodeSelectionView.OnCodeSelectedListener {

    protected fun bindSelectionView(selectionView: CodeSelectionView, type: ImageView.ScaleType) {
        selectionView.addOnCodeSelectedListener(this)
        selectionView.scaleType = type
    }

    protected fun bindResult(reader: BarcodeReader) {
        reader.addOnBarcodeScanResultListener(this)
    }

    override fun onCodeSelected(code: BarcodeWrapper) {
        BarcodeDetailDialog.show(this, code)
    }

}