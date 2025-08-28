package com.ty.tybusbase.sport.bean

/**
 * @author :WEI_CP
 * Time:2022/6/8 19:52
 * Description: this is DetectError
 */
class DetectError {
    var msgError: String? = null
        private set

    fun setMsgError(msgError: String?): DetectError {
        this.msgError = msgError
        return this
    }
}
