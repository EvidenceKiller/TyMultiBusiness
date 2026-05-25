package com.ty.identifty.base

enum class FailType {
    /**
     * 无错
     */
    NONE,

    /**
     * 裁剪区域错误
     */
    AREA_ERROR,

    /**
     * 数据错误
     */
    DATA_ERROR,

    /**
     * 正在识别中
     */
    DETECTING,

    /**
     * 异常
     */
    EXCEPTION,

    /**
     * 没有初始化
     */
    NOT_INIT,

    /**
     * 质量检测不过
     */
    NO_QUALITY,

    /**
     * 没有人脸
     */
    NO_FACE,

    /**
     * 多张人脸
     */
    MORE_FACE,

    /**
     * 没有特征值
     */
    NO_FEATURE,

    /**
     * 基础数据库中没有匹配到人
     */
    NO_MATCH,

    /**
     * 人脸SDK数据库，没有搜索到人
     */
    NO_SEARCH,

    /**
     * 没有校验通过
     */
    NO_VERIFY,
}