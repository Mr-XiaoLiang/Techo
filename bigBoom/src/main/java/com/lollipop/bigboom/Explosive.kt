package com.lollipop.bigboom

/**
 * @author lollipop
 * @date 2022/1/7 22:31
 * 用来发生爆炸的炸药
 */
interface Explosive {

    /**
     * 点燃炸药
     * @param basket 需要一个篮子来接收炸出的碎片
     */
    fun pilot(basket: Basket)

    /**
     * 放置燃料
     * @param any 任何资源都可以作为燃料，
     * 但是不同的炸药可能对于燃料会有不同的效果
     */
    fun putFuel(any: Any)

}