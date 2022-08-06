package com.lollipop.techo.data

enum class FontStyle {
    /**
     * 加粗
     */
    Bold,

    /**
     * 斜体
     */
    Italic,

    /**
     * 删除线
     */
    Strikethrough,

    /**
     * 下划线
     */
    Underline,

    /**
     * 字体大小
     */
    FontSize,

    /**
     * 颜色
     */
    Color,

    /**
     * 链接
     */
    Link,

    /**
     * 上标
     */
    Superscript,

    /**
     * 下标
     */
    Subscript,

    /**
     * 模糊
     */
    Blur;

    companion object {

        const val NORMAL = 0

        fun has(value: Int, vararg flag: FontStyle): Boolean {
            for (index in flag.indices) {
                val style = flag[index]
                val key = 1 shl style.flag
                if (value and key != 0) {
                    return true
                }
            }
            return false
        }

        fun hasAll(value: Int, vararg flag: FontStyle): Boolean {
            for (index in flag.indices) {
                val style = flag[index]
                val key = 1 shl style.flag
                if (value and key == 0) {
                    return false
                }
            }
            return true
        }

        fun add(base: Int = 0, vararg flag: FontStyle): Int {
            var value = base
            flag.forEach {
                value = value or it.flag
            }
            return value
        }

        fun clear(base: Int = 0, vararg flag: FontStyle): Int {
            var value = base
            flag.forEach {
                value = value and (it.flag.inv())
            }
            return value
        }

    }

    private val flag: Int
        get() {
            return 1 shl ordinal
        }

}