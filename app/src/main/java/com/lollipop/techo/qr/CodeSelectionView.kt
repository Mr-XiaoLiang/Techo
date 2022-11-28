package com.lollipop.techo.qr

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.qr.BarcodeWrapper

class CodeSelectionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    fun onCodeResult(size: Size, list: List<BarcodeWrapper>) {
        // TODO
    }

}