package com.lollipop.techo.data.json

/**
 * @author lollipop
 * @date 4/29/21 23:05
 * 可被转换的接口
 */
interface Convertible {
    fun parse(any: Any)
}