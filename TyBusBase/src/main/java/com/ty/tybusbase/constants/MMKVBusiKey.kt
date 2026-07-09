package com.ty.tybusbase.constants

object MMKVBusiKey {
    const val ORG_CODE = "orgCode" //组织机构代码

    const val ACCESS_TOKEN = "access_token"

    const val REGISTER_CODE = "register_code"

    const val PRODUCT_CODE = "product_code"

    const val APP_CODE = "app_code"
    const val APP_LOGO = "app_logo"
    const val DEVICE_NAME = "device_name"


    /*布尔——SKELETON_ABLE(骨骼开关)
    布尔——SKELETON_MIRROR(骨骼镜像)
    布尔——SURFACE_MIRROR(相机镜像)*/

    /*布尔——SKELETON_ABLE(骨骼开关)
    布尔——SKELETON_MIRROR(骨骼镜像)
    布尔——SURFACE_MIRROR(相机镜像)*/
    const val SKELETON_ABLE = "skeleton_able"
    const val SKELETON_MIRROR = "skeleton_mirror"
    const val SURFACE_MIRROR = "surface_mirror"
    const val MODEL_DEBUG = "model_debug"

    const val AUDIO_LIGHT_ENABLE = "audio_light_enable"

    // 0, 180
    const val SKELETON_ROTATE = "skeleton_rotate"


    //是否自动识别统一测试跳远垫
    const val AUTO_MANU_UNIFY_LONGJUMP_KEY = "auto_mark_long_jump"

    const val AUTO_MANU_UNIFY_START_LINE_KEY = "start_line"

    /**
     * 自动更新数据
     */
    const val AUTO_UPDATE = "auto_update"

    /**
     * 相机镜像
     */
    const val CAMERA_MIRROR = "camera_mirror"

    /**
     * 相机选择
     * 0:前置，1：后置，2：USB（预留）
     */
    const val CAMERA_CHOOSE = "camera_choose"

    /**
     * 身份识别
     */
    const val IDENTITY_TYPE = "IDENTITY_TYPE_N"

    const val NFC_READER_TYPE = "nfc_reader_type"

    /**
     * 相机打开方式选择
     * 1：camera方式；2：cameraX方式
     */
    const val CAMERA_TYPE_CHOOSE = "camera_type_choose"
    const val SKIP_TYPE_CHOOSE = "skip_type_choose"

    const val UVC_CAMERA_DISPLAY_PARAM = "uvc_camera_display_param"

    const val UVC_CAMERA_LIGHT = "uvc_camera_light"

    const val UVC_CAMERA_CONTRAST = "uvc_camera_contrast"
    const val UVC_CAMERA_EXPOSURE = "uvc_camera_exposure"

    //五人跳绳设置
    const val IF_SHOW_SKIP = "if_show_skip" //是否显示五人跳绳
    const val IF_SHOW_SPORT_THREE = "if_show_sport_three" //是否显示三人跳绳
    const val IF_SHOW_SPORT_TEN = "if_show_sport_ten" //是否显示十人跳绳
    const val IF_ENTER_INTEREST = "if_enter_interest" //是否能进入趣味运动

    const val HTTP_CAMERA_IP = "http_camera_ip" //http摄像头IP

    const val HTTP_CAMERA_ACCOUNT = "http_camera_account" //http摄像头账号

    const val HTTP_CAMERA_PSW = "http_camera_psw" //http摄像头密码

    const val HTTP_CAMERA_LIGHT_MODEL = "http_camera_model" //http摄像头模式

    const val HTTP_CAMERA_RECORD_MODEL = "http_camera_record_model" //http摄像头模式


    const val MULTI_ROPE_SKIP_COMPENSATION = "multi_rope_skip_compensation" //多人跳绳补偿值

    const val VISITOR_ABLE = "VISITOR_ABLE"  //游客模式

    const val MULTI_ROPE_SKIP_THRESHOLD = "multi_rope_skip_threshold" //多人跳绳游客使能

    const val IS_UPDATE = "is_update" //是否执行了升级；1=执行了升级

    const val UPDATE_REPORT_INFO = "update_report_info"  //

    const val SIT_UP_CONFIG = "SIT_UP_CONFIG_1"
    const val PULL_UP_CONFIG = "PULL_UP_CONFIG"
    const val ORGANIZE_SITE_UP_CONFIG = "ORGANIZE_SITE_UP_CONFIG"
    const val ORGANIZE_PULL_UP_CONFIG = "ORGANIZE_PULL_UP_CONFIG"

    const val PARAM_FREE_SPORT_SITE_UP = "base_param_free_site_up" //自主测试仰卧起坐参数

    const val PARAM_SKIP_TSJC = "PARAM_FREE_ROPE" //跳绳抬手检测

    const val SHOTBALL_IPC_CAMERA_CONFIG_KEY = "shotball_ipc_camera_config_key" //篮球IPC摄像头参数KEY
    const val SHOTBALL_LED_CONFIG_KEY = "shotball_led_config_key" //篮球LED参数KEY
    const val PINGPANG_IPC_CAMERA_CONFIG_KEY = "pingpang_ipc_camera_config_key" //兵乓球IPC摄像头参数KEY
    const val PINGPANG_BLUETOOTH_ADDRESS_CONFIG_KEY = "pingpang_bluetooth_address_config_key" //兵乓球IPC摄像头参数KEY

    const val SHOTBALL_CAMERA_CHOOSE: String = "shotball_camera_choose"
    const val SHOTBALL_COUNT_DOWN: String = "shotball_count_down"
}