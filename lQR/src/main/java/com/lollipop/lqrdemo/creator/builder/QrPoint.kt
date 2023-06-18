package com.lollipop.lqrdemo.creator.builder

class QrPoint(
    val width: Int,
    val height: Int,
    private val data: Array<IntArray>
) {

    companion object {
        const val POINT_NONE = 0
        const val POINT_DATA = 1
        const val POINT_EMPTY = 2
    }

    fun get(x: Int, y: Int): Type {
        if (x >= width || x < 0) {
            return Type.NONE
        }
        if (y >= height || y < 0) {
            return Type.NONE
        }
        return when (data[y][x]) {
            POINT_DATA -> {
                Type.DATA
            }

            POINT_EMPTY -> {
                Type.EMPTY
            }

            else -> {
                Type.NONE
            }
        }
    }

    enum class Type {
        DATA, EMPTY, NONE
    }

}