package com.lollipop.faceicon

sealed class FaceIcons(
    override val left: FaceIcon.Eyes,
    override val right: FaceIcon.Eyes,
    override val mouth: FaceIcon.Mouth
) : FaceIcon {

    companion object {
        const val EYES_BASE_Y = 0.29F
        const val MOUTH_BASE_Y = 0.75F
    }

    object HAPPY: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.happy(center = FacePoint(0.5F, MOUTH_BASE_Y), width = 0.75F, depth = 0.3F)
    )
    object SADNESS: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.sadness(center = FacePoint(0.5F, MOUTH_BASE_Y), width = 0.75F, depth = 0.3F)
    )
    object CALM: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.calm(center = FacePoint(0.5F, MOUTH_BASE_Y), width = 0.75F, depth = 0.3F)
    )

}