package com.lollipop.web.listener

import android.graphics.Bitmap
import com.lollipop.web.IWeb

interface TitleListener {

    /**
     * 标题发生变化时
     * @param iWeb 浏览器实体
     * @param title 网页标题
     */
    fun onTitleChanged(iWeb: IWeb, title: String)

    /**
     * 标题的Icon发生变化时
     * @param iWeb 浏览器实体
     * @param icon 网页标题的icon
     */
    fun onIconChanged(iWeb: IWeb, icon: Bitmap?)

}