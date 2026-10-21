package com.ty.tybusbase.grpc

import com.ty.tybusbase.constants.SceneConstants

class GrpcSubModelInfo {
    var model: Int = 0
    var sceneId: String = SceneConstants.FREE
    var skuId: String? = null
    var skuName:String? = null
    var skuItemCode: String? = null
    var testCount: Int = 1
    var regionSkuMapList: List<RegionSkuMap>? = null
}

/**
 * 识别框体位置对应运动信息
 * index:框体索引位置
 * sku:运动信息
 */
class RegionSkuMap(
    var index: Int,
    var skuId: String,
    var skuName:String,
    var skuItemCode: String,
    var testCount: Int = 1
)

enum class SubScreenMode {
    /**
     * 独立运动模式
     */
    INDEPENDENT,

    /**
     * 多运动选择模式
     */
    MULTIPLE_SELECTION

    ;

    companion object {
        /**
         * 根据Grpc传递的状态int值转换为协同状态，越界返回null
         */
        fun fromCode(code: Int): SubScreenMode? =
            SubScreenMode.values().getOrNull(code)
    }
}