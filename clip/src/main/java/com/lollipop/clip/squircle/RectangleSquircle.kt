package com.lollipop.clip.squircle

import android.graphics.Path
import android.graphics.Rect
import kotlin.math.min

class RectangleSquircle {

    var leftTop: SquircleCorner = SquircleCorner(0F, 0F)
    var rightTop: SquircleCorner = SquircleCorner(0F, 0F)
    var rightBottom: SquircleCorner = SquircleCorner(0F, 0F)
    var leftBottom: SquircleCorner = SquircleCorner(0F, 0F)

    fun setCorner(
        leftTop: SquircleCorner,
        rightTop: SquircleCorner,
        rightBottom: SquircleCorner,
        leftBottom: SquircleCorner
    ) {
        this.leftTop = leftTop
        this.rightTop = rightTop
        this.rightBottom = rightBottom
        this.leftBottom = leftBottom
    }

    fun build(boundsLeft: Int, boundsTop: Int, boundsRight: Int, boundsBottom: Int, outPath: Path) {

        val edgeYLength = (boundsBottom - boundsTop).toFloat()
        val edgeXLength = (boundsRight - boundsLeft).toFloat()
        val left = boundsLeft.toFloat()
        val top = boundsTop.toFloat()
        val right = boundsRight.toFloat()
        val bottom = boundsBottom.toFloat()

        val ltIn = getRadius(leftTop.inEdgeRadius, edgeYLength)
        val ltOut = getRadius(leftTop.outEdgeRadius, edgeXLength)
        outPath.moveTo(left, top + ltIn)
        outPath.cubicTo(
            left,
            top + ltIn - (ltIn * leftTop.inEdgeWeight),
            left + ltOut - (ltOut * leftTop.outEdgeWeight),
            top,
            left + ltOut,
            top
        )

        val rtIn = getRadius(rightTop.inEdgeRadius, edgeXLength)
        val rtOut = getRadius(rightTop.outEdgeRadius, edgeYLength)
        outPath.lineTo(right - rtIn, top)
        outPath.cubicTo(
            right - rtIn + (rtIn * rightTop.inEdgeWeight),
            top,
            right,
            top + rtOut - (rtOut * rightTop.outEdgeWeight),
            right,
            top + rtOut
        )

        val rbIn = getRadius(rightBottom.inEdgeRadius, edgeYLength)
        val rbOut = getRadius(rightBottom.outEdgeRadius, edgeXLength)
        outPath.lineTo(right, bottom - rbIn)
        outPath.cubicTo(
            right,
            bottom - rbIn + (rbIn * rightBottom.inEdgeWeight),
            right - rbOut + (rbOut * rightBottom.outEdgeWeight),
            bottom,
            right - rbOut,
            bottom
        )

        val lbIn = getRadius(leftBottom.inEdgeRadius, edgeXLength)
        val lbOut = getRadius(leftBottom.outEdgeRadius, edgeYLength)
        outPath.lineTo(left + lbIn, bottom)
        outPath.cubicTo(
            left + lbIn - (lbIn * leftBottom.inEdgeWeight),
            bottom,
            left,
            bottom - lbOut + (lbOut * leftBottom.outEdgeWeight),
            left,
            bottom - lbOut
        )
        outPath.close()
    }

    private fun getRadius(edgeRadius: Float, edgeLength: Float): Float {
        return min(edgeRadius, edgeLength * 0.5F)
    }

}