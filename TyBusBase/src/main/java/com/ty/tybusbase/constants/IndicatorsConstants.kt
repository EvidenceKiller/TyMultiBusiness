package com.ty.tybusbase.constants

object IndicatorsConstants {
    /**************************跳远指标参数*******************************/
    const val JUMP_CODE = "0301" //跳远CODE
    const val JUMP_RESULT_CODE = "03010001" // 跳远成绩
    const val JUMP_RESULT_TYPE_CODE = "03010002" // 跳远成绩类型
    const val JUMP_RESULT_SCORE_CODE = "03010003" // 跳远得分
    const val JUMP_RESULT_LEVEL_CODE = "03010004" // 跳远等级
    const val JUMP_RESULT_VIOLATION_CODE = "03010005" // 跳远成绩违规
    const val JUMP_FRONT_SWAY_CODE = "03010006" //上摆幅度
    const val JUMP_PREPARE_BACK_SWAY_CODE = "03010007" //预蹲后摆
    const val JUMP_PREPARE_BEND_HIP_CODE = "03010008" //预蹲屈髋
    const val JUMP_PREPARE_BEND_KNEE_CODE = "03010009" //预蹲屈膝
    const val JUMP_TAKE_OFF_CODE = "03010010" //起跳角度
    const val JUMP_TAKE_OFF_FRONT_SWAY_POSTURE_CODE = "03010011" //前摆姿势
    const val JUMP_TAKE_OFF_FRONT_SWAY_CODE = "03010012" //前摆幅度
    const val JUMP_TAKE_OFF_STRETCH_HIP_CODE = "03010013" //起跳伸髋
    const val JUMP_TAKE_OFF_STRETCH_KNEE_CODE = "03010014" //起跳伸膝
    const val JUMP_TAKE_OFF_TUCK_LEG_CODE = "03010015" //起跳蹬地
    const val JUMP_SOAR_ARM_CODE = "03010016" //手臂位置
    const val JUMP_SOAR_SHRINK_ADBOMEN_CODE = "03010017" //腾空收腹
    const val JUMP_SOAR_SHRINK_LEG_CODE = "03010018" //腾空收退
    const val JUMP_SOAR_DURATION_CODE = "03010019" //腾空时间
    const val JUMP_FALL_SHRINK_ABDOMEN_CODE = "03010020" //落地收腹
    const val JUMP_FALL_EXTEND_LEG_CODE = "03010021" //落地收退
    const val JUMP_FALL_POSE_CODE = "03010022" //落地姿势
    const val JUMP_FALL_ANGLE_CODE = "03010023" //落地角度
    const val JUMP_FALL_SPOOLER_BEND_KNEE_CODE = "03010024" //缓冲屈膝
    const val JUMP_MOVEMENT_TRACK_CODE = "03010025" //运动轨迹
    const val JUMP_MOTION_ANALYSIS_CODE = "03010026" //动作分析
    const val JUMP_VIDEO_CODE = "03010028" //运动视频
    /**************************跳远指标参数*******************************/


    /**************************跳绳指标参数*******************************/
    const val SKIP_CODE = "0201"
    const val SKIP_SINGLE_TIME_DISTRIBUTION = "02010001" //运动单个时间分布
    const val SKIP_SPEED_TIME_DISTRIBUTION = "02010002" //速度时间分布
    const val SKIP_INTERRUPT_TIME_DISTRIBUTION = "02010003" //中断时间区域分布
    const val SKIP_TOTAL_TIME = "02010004" //总时长
    const val SKIP_MAST_COMBO_COUNT = "02010005" //最大连击数
    const val SKIP_POSITION_OFFSET = "02010006" //位置偏移
    const val SKIP_UPPER_ARM_ANGLE_RATE = "02010007" //摇绳大臂夹角合格率
    const val SKIP_FOREARM_ANGLE_RATE = "02010008" //摇绳小臂夹角合格率
    const val SKIP_WRIST_RATE = "02010009" //摇绳手腕对称性合格率
    const val SKIP_JUMP_HIGH_RATE = "02010010" //起跳高度合格率
    const val SKIP_FOOT_SPACE_RATE = "02010011" //双脚间距合格率
    /**************************跳绳指标参数*******************************/

    /**************************引体向上指标参数*******************************/
    const val PULL_UP_CODE = "0401" // 引体向上CODE
    
    // 整体过程性指标
    const val PULL_UP_SINGLE_TIME_DISTRIBUTION = "04010001" // 运动单个时间分布
    const val PULL_UP_SPEED_TIME_DISTRIBUTION = "04010002" // 速度时间分布
    const val PULL_UP_TOTAL_TIME = "04010003" // 总时长
    const val PULL_UP_FAIL_COUNT = "04010004" // 失败次数
    const val PULL_UP_FAIL_COUNT_ARM_NOT_STRAIGHT = "04010005" // 失败次数（手臂未伸直）
    const val PULL_UP_HAND_GRIP_DISTANCE_PASS_RATE = "04010006" // 双手握距合格率
    const val PULL_UP_LEFT_RIGHT_BALANCE_PASS_RATE = "04010007" // 左右均衡合格率
    const val PULL_UP_MOTION_PROCESS_PASS_RATE = "04010008" // 动作行程合格率
    const val PULL_UP_FAIL_COUNT_CHIN_NOT_OVER_BAR = "04010009" // 失败次数（下颌未过杆）
    /**************************引体向上指标参数*******************************/

    /**************************仰卧起坐指标参数*******************************/
    const val SITE_UP_CODE = "0601" // 仰卧起坐CODE
    
    // 整体过程性指标
    const val SITE_UP_SINGLE_TIME_DISTRIBUTION = "06010001" // 运动单个时间分布
    const val SITE_UP_SPEED_TIME_DISTRIBUTION = "06010002" // 速度时间分布
    const val SITE_UP_TOTAL_TIME = "06010003" // 总时长
    const val SITE_UP_FAIL_COUNT = "06010004" // 失败次数
    const val SITE_UP_FAIL_COUNT_NOT_HOLDING_HEAD = "06010005" // 失败次数（未双手抱头）
    const val SITE_UP_FAIL_COUNT_KNEE_NOT_90_DEGREE = "06010006" // 失败次数（未屈膝 90°）
    const val SITE_UP_FAIL_COUNT_ELBOW_NOT_TOUCHING_KNEE = "06010007" // 失败次数（双肘未触膝）
    const val SITE_UP_FAIL_COUNT_SHOULDER_NOT_TOUCHING_GROUND = "06010008" // 失败次数（肩背未触底）
    const val SITE_UP_FAIL_COUNT_HIPS_LIFTING = "06010009" // 失败次数（垫臀）
    const val SITE_UP_UPPER_LIMB_POSTURE_PASS_RATE = "06010010" // 上肢姿势达标率
    const val SITE_UP_CORE_STRENGTH_PASS_RATE = "06010011" // 核心发力达标率
    const val SITE_UP_KNEE_ANGLE_PASS_RATE = "06010012" // 屈膝角度达标率
    const val SITE_UP_HIP_STABILITY_PASS_RATE = "06010013" // 髋部稳定达标率
    const val SITE_UP_SUPINE_POSTURE_PASS_RATE = "06010014" // 仰卧姿势达标率
    /**************************仰卧起坐指标参数*******************************/
}