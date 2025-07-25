package com.ty.bus.multi.core.base

import com.aipower.modulepk.ui.fivesport.MultiSportCardViewState
import com.ty.tybusbase.entity.AccountInfo

interface ISportCardView {
    /**
     * 设置卡片Number
     */
    fun setNumber(number: String)

    /**
     * 切换状态
     */
    fun changeState(state: MultiSportCardViewState)

    /**
     * 切换到没人状态
     * 可从，standby，identifying,prepared,切换过来
     */
    fun toNobody()

    /**
     * 切换到举手准备状态
     */
    fun toStandby()

    /**
     * 切换到识别中，由准备 ==》识别中
     */
    fun toIdentifying()

    /**
     * 切换到，已准备状态，由识别中 ==》已准备（游客或已识别到人）
     */
    fun toPrepared(accountInfo: AccountInfo)

    /**
     * 切换到识别已准备状态
     */
    fun toPrepared()

    fun toPrecastPrepare()

    fun toPrecastPerson()

    /**
     * 设置用户信息
     */
    fun setAccountInfo(accountInfo: AccountInfo)

    /**
     * 设置用户信息
     */
    fun setUserInfo(name: String, header: String) {}
    fun setPrecastAccountInfo(accountInfo: AccountInfo)

    /**
     * 开始运动状态
     */
    fun toSporting()

    /**
     * 切换到比赛开始，但是该位置无人状态，由Nobody ==> NobodySport
     */
    fun toNobodySport()


    /**
     * 运动中切换到离开状态
     */
    fun sportingToLeave()

    /**
     * 离开状态切换回运动中状态
     */
    fun leaveToSporting()

    /**
     * 切换到干扰模式
     */
    fun sportingToInterference()

    fun interferenceToSporting()

    /**
     * 切换到识别失败
     */
    fun toIdentifyFail()

    /**
     * 比赛开始，如果已识别到人 ==> toSporting,如果未识别到人 ==>toNobodySport
     */
    fun toStarted()

    /**
     * 更新成绩
     */
    fun updateScore(score: Int)

    /**
     * 设置时间
     */
    fun setTime(time: String)


    fun setTimeBackground(res: Int)
}