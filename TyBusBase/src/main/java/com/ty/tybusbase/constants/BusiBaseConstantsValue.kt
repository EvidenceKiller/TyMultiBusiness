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

    //  百度人脸识别KEY 正式Key，谨慎使用
    const val FACE_LICENSE_KEY = "ty-integrate-gx-face-offline-app"

    /**
     * HK摄像头录制视屏模式
     */
    const val HTTP_CAMERA_RECORD_MODEL_DEFAULT = 1
    const val HTTP_CAMERA_RECORD_MODEL_IPC = 1 shl 1
    const val HTTP_CAMERA_RECORD_DEFAULT_MODEL = HTTP_CAMERA_RECORD_MODEL_DEFAULT

    const val HTTP_CAMERA_DEFAULT_IP = "172.16.30.200"
    const val HTTP_CAMERA_DEFAULT_ACCOUNT = "admin"
    const val HTTP_CAMERA_DEFAULT_PASSWORD = "zy132456"
    const val HTTP_CAMERA_DEFAULT_PORT = 8000

    const val MULTI_SPORT_CONFIG_KEY = "multi_sport_config_key_"

    /**
     * 设备调试模式控制参数
     */
    const val DEFAULT_SKELETON = false //骨骼开关

    const val DEFAULT_DEBUG_MODULE = false //调试模式

    const val DEFAULT_VISITOR_MODULE = true //游客模式

    const val DEFAULT_SKELETON_MIRROR = false //骨骼镜像

    const val FINISH_TIME_OUT = (3 * 1000 //超时关闭页面时间(3秒)
            ).toLong()

    const val IDENTITY_TIME_OUT = (60 * 1000 //识别超时时间(1分钟)
            ).toLong()

    const val HTTP_CAMERA_PREVIEW_WIDTH = 1920
    const val HTTP_CAMERA_PREVIEW_HEIGHT = 1080

    // 摄像头预览帧数
    const val PREVIEW_FPS = 25
    const val CAMERA_PREVIEW_WIDTH = 1920
    const val CAMERA_PREVIEW_HEIGHT = 1080

    const val CAMERA_PREVIEW_4K_WIDTH = 3840
    const val CAMERA_PREVIEW_4K_HEIGHT = 2160


    /**
     * 身份识别类型
     */
    const val IDENTITY_TYPE_CARD = 1
    const val IDENTITY_TYPE_FACE = 1 shl 1 //原=0，既默认值为0时表示人脸，新版需要兼容，读取到0时，转换为人脸

    const val IDENTITY_TYPE_QR = 1 shl 2 //二维码识别

    const val NFC_READER_TYPE_A = 1 //读卡设备1（旧设备）
    const val NFC_READER_TYPE_B = 2 //读卡设备2（新设备）
    const val DEFAULT_NFC_READER_TYPE = NFC_READER_TYPE_B //NFC 读卡设备默认使用2（新设备）


    /********************
     * 0:前置，1：后置，2：USB（预留）
     */
    const val CAMERA_FRONT = 0 //前置相机

    const val CAMERA_BACK = 1 //后置相机

    const val CAMERA_USB = 2 //USB相机


    /***********************
     * 1:camera方式；2：cameraX 方式
     */
    const val CAMERA_TYPE_CAMERA = 1
    const val CAMERA_TYPE_CAMERAX = 2
    const val CAMERA_TYPE_CAMERA_UVC = 3
    const val CAMERA_TYPE_V4L2 = 4
    const val CAMERA_TYPE_DEFAULT: Int = CAMERA_TYPE_CAMERAX

    const val DEFAULT_CAMERA_MIRROR = false //骨骼镜像


    /***********************
     * UVC摄像头画质设置
     * 1:普通；2：强光环境；3：弱光环境,4:逆光环境
     */
    const val UVC_CAMERA_DISPLAY_PARAM_GENERAL = 1
    const val UVC_CAMERA_DISPLAY_PARAM_QG = 2
    const val UVC_CAMERA_DISPLAY_PARAM_RG = 3
    const val UVC_CAMERA_DISPLAY_PARAM_CUSTOM = 4
    const val UVC_CAMERA_DISPLAY_PARAM_DEFAULT: Int = UVC_CAMERA_DISPLAY_PARAM_GENERAL

    /**
     * 默认UVC摄像头光线强度
     */
    const val UVC_CAMERA_DEFAULT_LIGHT = 60
    const val UVC_CAMERA_DEFAULT_CONTRAST = 60

    const val MODEL_GROUP_NAME_GB = "model_group_name_gb" // 国标跳绳模型

    const val FACE_DETECT_TYPE_STUDENT = 0 //人脸识别类型：学生
    const val FACE_DETECT_TYPE_TEACHER = 1 //人脸识别类型：老师

    const val THREE_SPORT_CONFIG_KEY = "overlay_three_sport_config_key"
    const val THREE_SPORT_CONFIG_DEFAULT_REGION =
        "[{\"x\":203,\"y\":376},{\"x\":809,\"y\":379},{\"x\":157,\"y\":624},{\"x\":818,\"y\":639},{\"x\":146,\"y\":760},{\"x\":823,\"y\":766},{\"x\":91,\"y\":1015},{\"x\":815,\"y\":1018},{\"x\":967,\"y\":766},{\"x\":1633,\"y\":781},{\"x\":968,\"y\":1018},{\"x\":1703,\"y\":1027}]"

    const val DEFAULT_SINGLE_FACE_THRESHOLD = 0.75f //默认单人人脸阈值
    const val DEFAULT_MULTI_FACE_THRESHOLD = 0.4f //默认多人人脸阈值

    const val CHINA_MOBILE_CARD_TYPE = "1" //移动卡类型

    const val AUTO_BACK_TIME = 15_000L //运动完自动退出的时间

    /***********************
     * 投篮运动摄像头选择
     * 1:USB；2：IPC
     */
    const val SHOTBALL_CAMERA_USB: Int = 0
    const val SHOTBALL_CAMERA_IPC: Int = 1

    const val SHOTBALL_DEFAULT_COUNT_DOWN: Int = 3

    const val SHOTBALL_DEFAULT_MULTI_PERSON_DETECTION: Boolean = false

    const val SHOTBALL_SPORT_TYPE: String = "shotball_sport_type"
    const val SHOTBALL_SPORT_DURATION: String = "shotball_sport_duration"

    const val SHOTBALL_FREE_POINT: Int = 0
    const val SHOTBALL_SET_POINT_L1: Int = 1
    const val SHOTBALL_SET_POINT_L2: Int = 2
    const val SHOTBALL_SET_POINT_L3: Int = 3

    const val SHOTBALL_SET_POINT_L1_NAME: String = "L1"
    const val SHOTBALL_SET_POINT_L2_NAME: String = "L2"
    const val SHOTBALL_SET_POINT_L3_NAME: String = "L3"

    const val GRPC_CONNECT_PORT = 3588 //grpc端口
}