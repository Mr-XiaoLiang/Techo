package com.lollipop.faceicon

import kotlin.math.sqrt

class FaceMouth(
    val center: FacePoint,
    val leftTopOffset: FacePoint,
    val leftBottomOffset: FacePoint,
    val middleTopOffset: FacePoint,
    val middleBottomOffset: FacePoint,
    val rightTopOffset: FacePoint,
    val rightBottomOffset: FacePoint,
) : FaceIcon.Mouth {

    override val leftTop = center + leftTopOffset
    override val leftBottom = center + leftBottomOffset
    override val middleTop = center + middleTopOffset
    override val middleBottom = center + middleBottomOffset
    override val rightTop = center + rightTopOffset
    override val rightBottom = center + rightBottomOffset

    companion object {
        fun happy(center: FacePoint, width: Float, stroke: Float, depth: Float): FaceMouth {
            val halfStroke = stroke * 0.5F
            return FaceMouth(
                center = center,
                leftTopOffset = FacePoint(
                    width * -0.5F + halfStroke,
                    depth * -0.5F
                ),
                leftBottomOffset = FacePoint(
                    width * -0.5F - halfStroke,
                    depth * -0.5F
                ),
                middleTopOffset = FacePoint(0F, depth * 0.5F - halfStroke),
                middleBottomOffset = FacePoint(0F, depth * 0.5F + halfStroke),
                rightTopOffset = FacePoint(
                    width * 0.5F - halfStroke,
                    depth * -0.5F
                ),
                rightBottomOffset = FacePoint(
                    width * 0.5F + halfStroke,
                    depth * -0.5F
                )
            )
        }

        fun sadness(center: FacePoint, width: Float, stroke: Float, depth: Float): FaceMouth {
            val halfStroke = stroke * 0.5F
            return FaceMouth(
                center = center,
                leftTopOffset = FacePoint(
                    width * -0.5F - halfStroke,
                    depth * 0.5F
                ),
                leftBottomOffset = FacePoint(
                    width * -0.5F + halfStroke,
                    depth * 0.5F
                ),
                middleTopOffset = FacePoint(0F, depth * -0.5F - halfStroke),
                middleBottomOffset = FacePoint(0F, depth * -0.5F + halfStroke),
                rightTopOffset = FacePoint(
                    width * 0.5F + halfStroke,
                    depth * 0.5F
                ),
                rightBottomOffset = FacePoint(
                    width * 0.5F - halfStroke,
                    depth * 0.5F
                )
            )
        }

        fun calm(center: FacePoint, width: Float, stroke: Float, depth: Float): FaceMouth {
            val halfStroke = stroke * 0.5F
            val top = 0 - halfStroke
            val bottom = 0 + halfStroke
            val left = width * -0.3F
            val right = width * 0.3F
            return FaceMouth(
                center = center,
                leftTopOffset = FacePoint(left, top),
                leftBottomOffset = FacePoint(left, bottom),
                middleTopOffset = FacePoint(0F, top),
                middleBottomOffset = FacePoint(0F, bottom),
                rightTopOffset = FacePoint(right, top),
                rightBottomOffset = FacePoint(right, bottom)
            )
        }
    }

}