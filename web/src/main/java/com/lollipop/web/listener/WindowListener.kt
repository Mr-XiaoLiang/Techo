package com.lollipop.web.listener

import com.lollipop.web.IWeb

interface WindowListener {

    /**
     * 请求宿主应用程序创建一个新窗口。
     * 如果宿主应用程序选择接受这个请求，它应该从这个方法返回 true，
     * 创建一个新的 WebView 来承载窗口，将它插入到 View 系统中，
     * 并将新创建的IWeb对象放到返回值中。如果宿主应用程序选择不接受请求，它应该从此方法返回 false。
     * 此方法的默认实现什么都不做，因此返回 false。
     * 当 isUserGesture 标志为 false 时，应用程序通常不应允许创建窗口，因为这可能是不需要的弹出窗口。
     * 应用程序应该小心他们如何显示新窗口：不要简单地将它覆盖在现有的 WebView 上，
     * 因为这可能会误导用户他们正在查看哪个站点。
     * 如果您的应用程序显示主页的 URL，请确保也以类似的方式显示新窗口的 URL。
     * 如果您的应用程序不显示 URL，请考虑完全禁止创建新窗口。
     * 注意：没有可靠的方法来判断哪个页面请求了新窗口：请求可能来自 WebView 内的第三方 iframe。
     * @param view 当前的Web对象
     * @param isDialog 是否是对话框的形式
     * @param isUserGesture 是否是用户操作的，标志为 false 时，应用程序通常不应允许创建窗口，因为这可能是不需要的弹出窗口。
     * @param result 新创建的IWeb
     * @return 如果宿主应用程序选择接受这个请求，它应该从这个方法返回 true，
     */
    fun onCrete(
        view: IWeb,
        isDialog: Boolean,
        isUserGesture: Boolean,
        result: WindowCreateResult
    ): Boolean

    /**
     * 请求关闭一个 WebView
     */
    fun onClose(view: IWeb)

    interface WindowCreateResult {
        fun onWebCreated(iWeb: IWeb)
    }

}