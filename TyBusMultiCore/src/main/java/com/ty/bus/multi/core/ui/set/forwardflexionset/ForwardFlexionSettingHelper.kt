package com.ty.bus.multi.core.ui.set.forwardflexionset

import com.google.gson.Gson
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.ForwardFlexionParam
import com.ty.tybusbase.utils.MMKVUtil

object ForwardFlexionSettingHelper {
    fun updateSetParam(param: ForwardFlexionParam?) {
        val gson = Gson()
        MMKVUtil.encode(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM, gson.toJson(param))
    }

    fun getSetParam(): ForwardFlexionParam {
        val gson = Gson()
        val str = MMKVUtil.decodeString(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM)
        return if (str.isNullOrEmpty()) ForwardFlexionParam() else gson.fromJson<ForwardFlexionParam>(
            str,
            ForwardFlexionParam::class.java
        )
    }

}