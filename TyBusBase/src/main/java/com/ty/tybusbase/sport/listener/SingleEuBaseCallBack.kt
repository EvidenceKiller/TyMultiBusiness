package com.ty.tybusbase.sport.listener

import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuCallback
import com.ty.tybusbase.sport.bean.SportDetectResult

/**
 * @ProjectName: SportBS
 * @Date: 2022/8/10
 * @Desc:
 */
open class SingleEuBaseCallBack : EuCallback() {
    open fun onComplete(result: SportDetectResult) {}

    open fun onSkeleton(skeleton: AdlPoseSkeleton?){}
    open fun onCheckRope(ropeExist: Boolean) {}
}