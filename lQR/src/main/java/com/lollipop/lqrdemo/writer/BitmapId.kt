package com.lollipop.lqrdemo.writer

import android.graphics.Bitmap

class BitmapId {

    private var id = 0
    private var hash = 0

    fun isChanged(bitmap: Bitmap?, remember: Boolean = true): Boolean {
        // 如果为空或者被回收了，那么清空记录
        if (bitmap == null || bitmap.isRecycled) {
            id = 0
            hash = 0
            return true
        }
        val generationId = bitmap.generationId
        val hashCode = System.identityHashCode(bitmap)
        // 通过比对generationId来确定修改次数，通过hash来确定对象是否是同一个
        if (generationId != id || hashCode != hash) {
            id = generationId
            hash = hashCode
            return true
        }
        return false
    }

}