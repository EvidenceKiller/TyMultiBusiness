package com.ty.tybusbase.entity

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class IpcCameraConfig(
    val ip: String,
    val account: String,
    val psw: String,
    val port: Int = 8000
) : Parcelable