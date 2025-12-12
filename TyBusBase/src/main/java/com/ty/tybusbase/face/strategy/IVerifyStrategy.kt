package com.ty.tybusbase.face.strategy

interface IVerifyStrategy {
    fun verifyUser(faceId: String, score: Float): Boolean
    fun reset()
}