package com.lollipop.bigboom

/**
 * 大爆炸的辅助工具类
 * 它会绑定一个展示的View
 * 然后将炸药交给他后，将会自动把碎片交给展示的View
 */
class BigBoomManager(
    val boomView: BigBoomViewProvider
) : Basket {

    var valueArray = emptyArray<String>()
        private set
    private var onErrorListener: OnErrorListener? = null
    private var onValuesChangedCallback: OnValuesChangedCallback? = null

    private var currentExplosive: Explosive? = null

    fun startFlow(): BoomFlow {
        return BoomFlow(this)
    }

    fun boom(e: Explosive) {
        currentExplosive = e
        e.pilot(this)
    }

    fun onError(listener: OnErrorListener?) {
        onErrorListener = listener
    }

    fun onValuesChanged(callback: OnValuesChangedCallback?) {
        onValuesChangedCallback = callback
    }

    override fun goodPatches(value: Array<String>) {
        valueArray = value
        val defaultSelected = onValuesChangedCallback?.onValuesChanged(currentExplosive, value)
        boomView.getBoomView()?.setPatches(value, defaultSelected ?: emptyArray())
    }

    override fun failedDetonate(code: Int, msg: String, throwable: Throwable?) {
        onErrorListener?.failedDetonate(code, msg, throwable)
    }

    fun interface BigBoomViewProvider {
        fun getBoomView(): BigBoomView?
    }

    fun interface OnErrorListener {
        fun failedDetonate(code: Int, msg: String, throwable: Throwable?)
    }

    fun interface OnValuesChangedCallback {
        fun onValuesChanged(e: Explosive?, value: Array<String>): Array<Int>
    }

}