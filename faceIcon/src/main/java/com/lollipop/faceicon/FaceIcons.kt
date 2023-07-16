package com.lollipop.faceicon

sealed class FaceIcons(
    override val left: FaceIcon.Eyes,
    override val right: FaceIcon.Eyes,
    override val mouth: FaceIcon.Mouth
) : FaceIcon {

    companion object {
        const val EYES_BASE_Y = 0.29F
    }

    object HAPPY: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.happy(center = FacePoint(0.5F, 0.75F), width = 0.6F, depth = 0.2F)
    )
    object SADNESS: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.sadness(center = FacePoint(0.5F, 0.75F), width = 0.6F, depth = 0.2F)
    )
    object CALM: FaceIcons(
        FaceEyes.open(center = FacePoint(0.25F, EYES_BASE_Y), radius = 0.065F),
        FaceEyes.open(center = FacePoint(0.75F, EYES_BASE_Y), radius = 0.065F),
        FaceMouth.calm(center = FacePoint(0.5F, 0.75F), width = 0.6F, depth = 0.2F)
    )

}