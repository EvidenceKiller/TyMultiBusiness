package com.ty.tybusbase.config

import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil

object BusiBaseConfig {

    /**
     * 更新游客模式
     */
    fun updateVisitorEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.VISITOR_ABLE, enable)
    }

    /**
     * 获取游客模式
     */
    fun getVisitorEnable(): Boolean {
        return MMKVUtil.decodeBoolean(MMKVBusiKey.VISITOR_ABLE, false)
    }
}