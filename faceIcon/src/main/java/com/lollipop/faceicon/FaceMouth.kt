package com.lollipop.faceicon

class FaceMouth(
    val center: FacePoint,
    val leftOffset: FacePoint,
    val middleOffset: FacePoint,
    val rightOffset: FacePoint,
) : FaceIcon.Mouth {
    override val left: FacePoint = center + leftOffset
    override val middle: FacePoint = center + middleOffset
    override val right: FacePoint = center + rightOffset

    companion object {
        fun happy(center: FacePoint, width: Float, depth: Float): FaceMouth {
            return FaceMouth(
                center = center,
                leftOffset = FacePoint(width * -0.5F, depth * -0.5F),
                middleOffset = FacePoint(0F, depth * 0.5F),
                rightOffset = FacePoint(width * 0.5F, depth * -0.5F)
            )
        }

        fun sadness(center: FacePoint, width: Float, depth: Float): FaceMouth {
            return FaceMouth(
                center = center,
                leftOffset = FacePoint(width * -0.5F, depth * 0.5F),
                middleOffset = FacePoint(0F, depth * -0.5F),
                rightOffset = FacePoint(width * 0.5F, depth * 0.5F)
            )
        }

        fun calm(center: FacePoint, width: Float, depth: Float): FaceMouth {
            return FaceMouth(
                center = center,
                leftOffset = FacePoint(width * -0.125F, 0F),
                middleOffset = FacePoint(0F, 0F),
                rightOffset = FacePoint(width * 0.125F, 0F)
            )
        }
    }
}