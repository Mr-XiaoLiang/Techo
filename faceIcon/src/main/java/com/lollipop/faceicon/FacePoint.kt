package com.lollipop.faceicon

class FacePoint(override val x: Float, override val y: Float) : FaceIcon.Point {

    operator fun plus(point: FacePoint): FacePoint {
        return FacePoint(this.x + point.x, this.y + point.y)
    }

}