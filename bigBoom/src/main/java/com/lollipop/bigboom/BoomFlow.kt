package com.lollipop.bigboom

/**
 * 一个爆炸的流程
 * 它主要用于简化各个组件之间的关系
 */
class BoomFlow(private val manager: BigBoomManager) {

    private var explosive: Explosive? = null

    private val pendingFuel = ArrayList<Any>(10)

    fun use(e: Explosive): BoomFlow {
        this.explosive = e
        return this
    }

    fun use(e: PresetExplosive): BoomFlow {
        return use(e.clazz.newInstance())
    }

    fun putFuel(any: Any): BoomFlow {
        pendingFuel.add(any)
        return this
    }

    fun putFuels(vararg any: Any): BoomFlow {
        pendingFuel.addAll(any)
        return this
    }

    fun fire(): BigBoomManager {
        this.explosive?.let { e ->
            pendingFuel.forEach {
                e.putFuel(it)
            }
            manager.boom(e)
        }
        return manager
    }

}