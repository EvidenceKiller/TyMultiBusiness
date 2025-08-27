package com.ty.tybusbase.constants

object BusiBaseConstantsValue {
    const val VISITOR_USER_ID = "00000000000000000000000000000000" //游客ID

    /**
     * 游客模式
     */
    const val VISITOR_ABLE = "VISITOR_ABLE"
    const val VISITOR_OPEN = true //默认打开游客模式


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

    const val FINISH_TIME_OUT = (3 * 1000 //超时关闭页面时间(3秒)
            ).toLong()

    const val IDENTITY_TIME_OUT = (60 * 1000 //识别超时时间(1分钟)
            ).toLong()

    const val HTTP_CAMERA_PREVIEW_WIDTH = 1920
    const val HTTP_CAMERA_PREVIEW_HEIGHT = 1080

    /**
     * 身份识别类型
     */
    const val IDENTITY_TYPE_CARD = 1
    const val IDENTITY_TYPE_FACE = 1 shl 1 //原=0，既默认值为0时表示人脸，新版需要兼容，读取到0时，转换为人脸

    const val IDENTITY_TYPE_QR = 1 shl 2 //二维码识别

}