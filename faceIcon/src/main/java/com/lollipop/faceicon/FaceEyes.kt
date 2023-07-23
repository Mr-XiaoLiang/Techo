package com.lollipop.faceicon

class FaceEyes(
    val center: FacePoint,
    val leftOffset: FacePoint,
    val topOffset: FacePoint,
    val rightOffset: FacePoint,
    val bottomOffset: FacePoint
) : FaceIcon.Eyes {
    override val left: FacePoint = center + leftOffset
    override val top: FacePoint = center + topOffset
    override val right: FacePoint = center + rightOffset
    override val bottom: FacePoint = center + bottomOffset

    companion object {

        fun pop(center: FacePoint, radius: Float): FaceEyes {
            return FaceEyes(
                center = center,
                leftOffset = FacePoint(-radius, 0F),
                topOffset = FacePoint(0F, -radius * 1.5F),
                rightOffset = FacePoint(radius, 0F),
                bottomOffset = FacePoint(0F, radius * 1.5F)
            )
        }

        fun open(center: FacePoint, radius: Float): FaceEyes {
            return FaceEyes(
                center = center,
                leftOffset = FacePoint(-radius, 0F),
                topOffset = FacePoint(0F, -radius),
                rightOffset = FacePoint(radius, 0F),
                bottomOffset = FacePoint(0F, radius)
            )
        }

        fun cute(center: FacePoint, radius: Float): FaceEyes {
            return FaceEyes(
                center = center,
                leftOffset = FacePoint(-radius, radius * 0.9F),
                topOffset = FacePoint(0F, -radius * 1.2F),
                rightOffset = FacePoint(radius, radius * 0.9F),
                bottomOffset = FacePoint(0F, radius * 1.2F)
            )
        }

        fun close(center: FacePoint, radius: Float): FaceEyes {
            return FaceEyes(
                center = center,
                leftOffset = FacePoint(radius * -1.5F, 0F),
                topOffset = FacePoint(0F, -radius * 0.5F),
                rightOffset = FacePoint(radius * 1.5F, 0F),
                bottomOffset = FacePoint(0F, radius * 0.5F)
            )
        }
    }

}