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

        val leftTop: Point
        val leftBottom: Point
        val middleTop: Point
        val middleBottom: Point
        val rightTop: Point
        val rightBottom: Point

    }

    interface Point {
        val x: Float
        val y: Float
    }

}