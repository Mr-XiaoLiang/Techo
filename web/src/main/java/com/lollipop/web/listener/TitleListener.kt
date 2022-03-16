package com.lollipop.web.listener

import com.lollipop.web.IWeb

fun interface TitleListener {

    /**
     * 标题发生变化时
     * @param iWeb 浏览器实体
     * @param title 网页标题
     */
    fun onTitleChanged(iWeb: IWeb, title: String)

}