package com.ty.tybusbase.constants

object BusiBaseConstantsValue {
    const val VISITOR_USER_ID = "00000000000000000000000000000000" //游客ID

    const val STARTED_TIME_OUT = (5 * 60 * 1000 //赛事开始后超时时间(5分钟)
            ).toLong()



    /**
     * HK摄像头录制视屏模式
     */
    const val HTTP_CAMERA_RECORD_MODEL_DEFAULT = 1
    const val HTTP_CAMERA_RECORD_MODEL_IPC = 1 shl 1
    const val HTTP_CAMERA_RECORD_DEFAULT_MODEL = HTTP_CAMERA_RECORD_MODEL_DEFAULT

    const val HTTP_CAMERA_DEFAULT_IP = "172.16.4.64"
    const val HTTP_CAMERA_DEFAULT_ACCOUNT = "admin"
    const val HTTP_CAMERA_DEFAULT_PASSWORD = "zy123456"
    const val HTTP_CAMERA_DEFAULT_PORT = 8000

    const val MULTI_SPORT_CONFIG_KEY = "multi_sport_config_key_"

    /**
     * 设备调试模式控制参数
     */
    const val DEFAULT_SKELETON = true //骨骼开关

    const val DEFAULT_DEBUG_MODULE = true //调试模式

    const val DEFAULT_VISITOR_MODULE = true //调试模式

    const val DEFAULT_SKELETON_MIRROR = false //骨骼镜像
}