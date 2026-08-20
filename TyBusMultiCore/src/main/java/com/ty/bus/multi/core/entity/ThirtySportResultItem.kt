package com.ty.bus.multi.core.entity

import com.ty.tybusbase.entity.DisplayType

/**
 * 30s跳绳结果页统一数据项，包含原始成绩与区域/道次/游客信息
 */
data class ThirtySportResultItem(
    val result: MultiSportResult,
    /** 所属区域 */
    val displayType: DisplayType,
    /** 区域名称，如"左区"/"中区"/"右区" */
    val areaName: String,
    /** 是否为游客 */
    val isVisitor: Boolean,
    /** 道次（从0开始，展示需+1） */
    val trackNumber: Int
) {
    /** 成绩 */
    val count: Int get() = result.count
    /** 排名 */
    val rank: Int get() = result.rank
    /** 姓名 */
    val name: String? get() = result.accountInfo.studentEntity?.accountName
    /** 头像地址 */
    val faceUrl: String? get() = result.accountInfo.studentEntity?.userFaceImgUrl
}
