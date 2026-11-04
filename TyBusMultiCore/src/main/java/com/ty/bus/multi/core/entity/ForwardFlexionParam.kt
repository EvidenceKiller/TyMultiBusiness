package com.ty.bus.multi.core.entity

class ForwardFlexionParam {
    private var minAngle = 110
    private var maxAngle = 170

    private val maxSsdqTh = 80 //双手对齐最大值

    private val minSsdqTh = 40 //双手对齐最大值

    var checkLeg = true //双膝伸直

    var szth = 0 //伸直阈值

    var checkFoot = true//双脚并拢

    var checkHand = false//双手平行

    var checkInertia = true//惯性前冲

    var ssdqTh = 8 //双手对齐阈值

    fun getAngle() = minAngle + ((szth * (maxAngle - minAngle)) / 100.0).toInt()

    fun getSsdqThVaule() = maxSsdqTh - ((ssdqTh * (maxSsdqTh - minSsdqTh)) / 10.0).toInt()
}