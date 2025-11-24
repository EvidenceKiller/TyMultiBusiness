package com.ty.bus.multi.core.config

import com.google.gson.Gson
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.ForwardFlexionParam
import com.ty.tybusbase.utils.MMKVUtil

object MultiCoreConfig {
    /**
     * 更新体前屈参数
     */
    fun updateForwardFlexionParam(param: ForwardFlexionParam?) {
        val gson = Gson()
        MMKVUtil.encode(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM, gson.toJson(param))
    }

    /**
     * 获取体前屈参数
     */
    fun getForwardFlexionParam(): ForwardFlexionParam {
        val gson = Gson()
        val str = MMKVUtil.decodeString(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM)
        return if (str.isNullOrEmpty()) ForwardFlexionParam() else gson.fromJson<ForwardFlexionParam>(
            str,
            ForwardFlexionParam::class.java
        )
    }

    /**
     * 更新多人运动性能参数
     */
    fun updateMultiSportPerformance(value: Boolean) {
        MMKVUtil.encode(MultiCoreConstant.MULTI_SKIP_PERFORMANCE, value)
    }

    /**
     * 获取多人运动性能参数
     */
    fun getMultiSportPerformance(): Boolean =
        MMKVUtil.decodeBoolean(MultiCoreConstant.MULTI_SKIP_PERFORMANCE, true)

}