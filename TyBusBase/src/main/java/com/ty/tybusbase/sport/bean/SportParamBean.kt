package com.ty.tybusbase.sport.bean

/**
 * @author zhouwenhua
 * @date 2023/9/25
 * @describe 运动参数存储类
 */

abstract class CommonParamBean {
    var enableBreakRuleVoice = true//语音播报
}

class JumpParamBean : CommonParamBean() {
    var enableOverLine = false//起跳踩线
    var enableFootBack = true//脚后退
    var enableHandDown = true//手部触地
    var enableHipDown = true//臀部部触地
    var overLineDx = 6//踩线阈值(0~20)
}

class SiteUpParamBean : CommonParamBean() {
    var enableHandOk = true//双手抱头
    var enableKneeAngleOk = true//屈膝90°
    var enableHipUp = false//垫臀
    var hipUpOverLineDx = 0//垫臀阈值
}

class PullUpParamBean : CommonParamBean() {
    var enableMandibleCrossover = true//下颌过杠
    var enableStandStill = false//身体保持静止开始
    var enableOverhang = false//直臂垂悬
    var enableCountDown = true//动作间隔10秒
}