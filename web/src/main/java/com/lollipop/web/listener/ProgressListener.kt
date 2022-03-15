package com.lollipop.web.listener

import com.lollipop.web.IWeb

interface ProgressListener {

    /**
     * 加载进度
     * @param iWeb 浏览器实体
     * @param progress 加载进度，0～100
     */
    fun onProgressChanged(iWeb: IWeb, progress: Int)

}