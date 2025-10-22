package com.ty.bus.multi.core.entity

class ForwardFlexionParam {
    var checkLeg = true//双膝伸直
    var szth = 75//伸直阈值
    var checkFoot = true//双脚并拢
    var checkHand = false//双手平行

    var baseAngle = 120

    fun getAngle(): Int {
        return baseAngle + ((szth * (180 - baseAngle)) / 100.0).toInt()
    }
}