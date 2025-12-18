package com.ty.tybusbase.identify

import com.ty.tybusbase.entity.AccountInfo

/**
 * @author :WEI_CP
 * Time:2022/3/7 9:24
 * Description: this is IdentifyMachineListener
 */
interface DoubleIdentifyMachineListener {
    fun onIdentityMachineInit(initResult: Boolean)

    fun onIdentityOk(leftAccountInfo: AccountInfo, rightAccountInfo: AccountInfo)

    fun onIdentityTimeFail()
}
