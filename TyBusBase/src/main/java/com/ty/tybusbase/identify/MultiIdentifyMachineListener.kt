package com.ty.tybusbase.identify

import com.ty.tybusbase.entity.AccountInfo

/**
 * 多人人脸识别回调，增加索引(位置)
 */
interface MultiIdentifyMachineListener {
    fun onIdentityMachineInit(initResult: Boolean) {}

    fun onIdentityTimeOn() {}

    fun onIdentityOk(index: Int, accountInfo: AccountInfo)

    fun onIdentityTimeFail(index: Int)
}
