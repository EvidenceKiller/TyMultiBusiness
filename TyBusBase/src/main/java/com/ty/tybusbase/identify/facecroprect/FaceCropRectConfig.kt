package com.ty.tybusbase.identify.facecroprect

/**
 * 人脸裁剪框参数。默认值与四人/十二人跳绳原逻辑一致。
 * 无有效关键点时不提供兜底框，由 [FaceCropRectHelper.getRect] 返回空 Rect，调用方跳过识别。
 */
data class FaceCropRectConfig(
    /** 裁剪框最小边长（correctRect） */
    val minSize: Int = 40,
    /** 无头顶点时，由肩线向上反推头高的系数 */
    val shoulderHeadRatio: Float = 1.5f,
    /** 眼距扩边系数（原 faceRect 中 *2） */
    val eyeSpanScale: Float = 2f,
    /** 耳距扩边系数（原 headRect 中 *1.4） */
    val earSpanScale: Float = 1.4f,
    /** 肩宽左右/下边冗余比例（原 *0.2） */
    val shoulderWidthPaddingRatio: Float = 0.2f,
)
