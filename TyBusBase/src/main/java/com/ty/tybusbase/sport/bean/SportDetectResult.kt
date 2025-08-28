package com.ty.tybusbase.sport.bean

import com.ty.tybusbase.constants.SportConstant

/**
 * Description: this is SportDetectResult
 * 单帧规格判定结果
 */
class SportDetectResult {
    private var stateMsg: String? = null

    /**
     * 当值大于0时采用意义
     */
    @SportConstant.SingleSportLevel
    var singleSportLevel = 0

    /**
     * 运动成绩
     */
    var sportAchievement = 0

    /**
     * 运动处方
     */
    var sportPrescription: Any? = null


    fun getStateMsg(): String? {
        return stateMsg
    }

    fun setMessage(stateMsg: String?) {
        this.stateMsg = stateMsg
    }

    fun setStateMsg(stateMsg: String?) {
        this.stateMsg = stateMsg
    }

}