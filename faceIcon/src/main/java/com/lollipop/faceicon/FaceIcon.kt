package com.lollipop.faceicon

interface FaceIcon {

    val left: Eyes
    val right: Eyes
    val mouth: Mouth

    interface Eyes {

        val left: Point
        val top: Point
        val right: Point
        val bottom: Point

    }

    interface Mouth {
        val left: Point
        val middle: Point
        val right: Point
    }

    interface Point {
        val x: Float
        val y: Float
    }

}