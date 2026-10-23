package com.ty.bus.multi.core.base

import com.ty.tybusbase.entity.AccountInfo

interface ISportCardView {
    /**
     * 设置卡片Number
     */
    fun setNumber(number: String)

    fun setDebug(debug: Boolean)

    /**
     * 切换状态
     */
    fun changeState(state: MultiSportCardViewState)

    /**
     * 设置用户信息
     */
    fun setAccountInfo(accountInfo: AccountInfo)

    /**
     * 设置用户信息
     */
    fun setUserInfo(name: String, header: String?) {}

    fun setPrecastAccountInfo(accountInfo: AccountInfo)

    /**
     * 更新成绩
     */
    fun updateScore(score: Int)

    /**
     * 设置时间
     */
    fun setTime(time: String)


    fun setTimeBackground(res: Int)

    fun reset()
    fun retest() {
    }
}