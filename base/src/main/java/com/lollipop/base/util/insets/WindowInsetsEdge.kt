package com.lollipop.base.util.insets

class WindowInsetsEdge(
    val left: WindowInsetsEdgeStrategy,
    val top: WindowInsetsEdgeStrategy,
    val right: WindowInsetsEdgeStrategy,
    val bottom: WindowInsetsEdgeStrategy
) {
    companion object {
        val ALL = WindowInsetsEdge(
            left = WindowInsetsEdgeStrategy.COMPARE,
            top = WindowInsetsEdgeStrategy.COMPARE,
            right = WindowInsetsEdgeStrategy.COMPARE,
            bottom = WindowInsetsEdgeStrategy.COMPARE
        )

        val HEADER = WindowInsetsEdge(
            left = WindowInsetsEdgeStrategy.COMPARE,
            top = WindowInsetsEdgeStrategy.COMPARE,
            right = WindowInsetsEdgeStrategy.COMPARE,
            bottom = WindowInsetsEdgeStrategy.ORIGINAL
        )

        val CONTENT = WindowInsetsEdge(
            left = WindowInsetsEdgeStrategy.COMPARE,
            top = WindowInsetsEdgeStrategy.ORIGINAL,
            right = WindowInsetsEdgeStrategy.COMPARE,
            bottom = WindowInsetsEdgeStrategy.COMPARE
        )

        val BOTTOM = WindowInsetsEdge(
            left = WindowInsetsEdgeStrategy.ORIGINAL,
            top = WindowInsetsEdgeStrategy.ORIGINAL,
            right = WindowInsetsEdgeStrategy.ORIGINAL,
            bottom = WindowInsetsEdgeStrategy.COMPARE
        )

        fun build(callback: Builder.() -> Unit): WindowInsetsEdge {
            val builder = Builder()
            callback(builder)
            return WindowInsetsEdge(
                left = builder.left,
                right = builder.right,
                top = builder.top,
                bottom = builder.bottom
            )
        }
    }

    fun baseTo(
        left: WindowInsetsEdgeStrategy = this.left,
        top: WindowInsetsEdgeStrategy = this.top,
        right: WindowInsetsEdgeStrategy = this.right,
        bottom: WindowInsetsEdgeStrategy = this.bottom
    ): WindowInsetsEdge {
        return WindowInsetsEdge(left, top, right, bottom)
    }

    class Builder {
        var left: WindowInsetsEdgeStrategy = WindowInsetsEdgeStrategy.ORIGINAL
        var top: WindowInsetsEdgeStrategy = WindowInsetsEdgeStrategy.ORIGINAL
        var right: WindowInsetsEdgeStrategy = WindowInsetsEdgeStrategy.ORIGINAL
        var bottom: WindowInsetsEdgeStrategy = WindowInsetsEdgeStrategy.ORIGINAL
    }

}