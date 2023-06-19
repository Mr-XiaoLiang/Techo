package com.lollipop.lqrdemo.creator.bridge

import com.lollipop.qr.writer.LBitMatrix

fun interface OnCodeMatrixChangedListener {
    fun onCodeMatrixChanged(matrix: LBitMatrix?)
}