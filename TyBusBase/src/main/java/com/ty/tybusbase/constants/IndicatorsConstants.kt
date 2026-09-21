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
    const val SKIP_VALID_COUNT = "02010012" //有效个数
    const val SKIP_VIOLATION_COUNT = "02010013" //违规个数
    /**************************跳绳指标参数*******************************/

    /**************************引体向上指标参数*******************************/
    const val PULL_UP_CODE = "0401" // 引体向上CODE

    // 整体过程性指标
    const val PULL_UP_SPEED_TIME_DISTRIBUTION = "04010002" // 速度时间分布
    const val PULL_UP_TOTAL_TIME = "04010003" // 总时长
    const val PULL_UP_HAND_GRIP_DISTANCE_PASS_RATE = "04010006" // 双手握距合格率
    const val PULL_UP_LEFT_RIGHT_BALANCE_PASS_RATE = "04010007" // 左右均衡合格率
    const val PULL_UP_MOTION_PROCESS_PASS_RATE = "04010008" // 动作行程合格率
    const val PULL_UP_FAIL_COUNT_CHIN_NOT_OVER_BAR = "04010011" // 违规次数（下颌未过杆）
    const val PULL_UP_FAIL_COUNT_ARM_NOT_STRAIGHT = "04010012" // 违规次数（手臂未伸直）
    //    运动单个时间分布	04010010	一级指标		List<Object>	[{"time":2.9（秒）,    "invalidFlag": 0/1(1：无效状态 0：有效) }]  //时间数组 秒
    const val PULL_UP_SINGLE_TIME_DISTRIBUTION = "04010010" // 运动单个时间分布
    /**************************引体向上指标参数*******************************/

    /**************************仰卧起坐指标参数*******************************/
    const val SITE_UP_CODE = "0601" // 仰卧起坐CODE

    // 整体过程性指标
    const val SITE_UP_SINGLE_TIME_DISTRIBUTION = "06010020" // 运动单个时间分布
    const val SITE_UP_SPEED_TIME_DISTRIBUTION = "06010002" // 速度时间分布
    const val SITE_UP_TOTAL_TIME = "06010003" // 总时长
    const val SITE_UP_FAIL_COUNT_NOT_HOLDING_HEAD = "06010015" // 违规次数（双手未护耳）
    const val SITE_UP_FAIL_COUNT_KNEE_NOT_90_DEGREE = "06010016" // 违规次数（未屈膝 90°）
    const val SITE_UP_FAIL_COUNT_ELBOW_NOT_TOUCHING_KNEE = "06010017" // 违规次数（双肘未触膝）
    const val SITE_UP_FAIL_COUNT_SHOULDER_NOT_TOUCHING_GROUND = "06010018" // 违规次数（肩背未触底）
    const val SITE_UP_FAIL_COUNT_HIPS_LIFTING = "06010019" // 违规次数（垫臀）
    const val SITE_UP_UPPER_LIMB_POSTURE_PASS_RATE = "06010010" // 上肢姿势达标率
    const val SITE_UP_CORE_STRENGTH_PASS_RATE = "06010011" // 核心发力达标率
    const val SITE_UP_KNEE_ANGLE_PASS_RATE = "06010012" // 屈膝角度达标率
    const val SITE_UP_HIP_STABILITY_PASS_RATE = "06010013" // 髋部稳定达标率
    const val SITE_UP_SUPINE_POSTURE_PASS_RATE = "06010014" // 仰卧姿势达标率
    /**************************仰卧起坐指标参数*******************************/

    /**************************************实心球**********************************/
    /**
     *  整体过程性指标	01	距离成绩	07010001	一级指标
     * 	整体过程性指标	01	违规类型	07010002	一级指标
     * 	整体过程性指标	01	得分	07010003	一级指标
     * 	整体过程性指标	01	等级	07010004	一级指标
     * 	整体过程性指标	01	成绩违规	07010005	一级指标
     * 	专项技能指标	02	预摆姿势	07010006	一级指标
     * 	专项技能指标	02	站姿间距	07010007	一级指标
     * 	专项技能指标	02	引球手肘	07010008	一级指标
     * 	专项技能指标	02	引球手臂	07010009	一级指标
     * 	专项技能指标	02	反弓伸髋	07010010	一级指标
     * 	专项技能指标	02	反弓屈膝	07010011	一级指标
     * 	专项技能指标	02	出手姿势	07010012	一级指标
     * 	专项技能指标	02	出手角度	07010013	一级指标
     * 	专项技能指标	02	出手时机	07010014	一级指标
     * 	整体过程性指标	01	运动轨迹	07010015	一级指标
     * 	整体过程性指标	01	动作分析	07010016	一级指标
     * 	整体过程性指标	01	偏移情况	07010020	一级指标
     */
    const val MEDICINE_BALL_CODE = "0701" //实心球CODE
    const val MEDICINE_BALL_DISTANCE_SCORE = "07010001" //距离成绩
    const val MEDICINE_BALL_VIOLATION_TYPE = "07010002" //违规类型
    const val MEDICINE_BALL_SCORE = "07010003" //得分
    const val MEDICINE_BALL_LEVEL = "07010004" //等级
    const val MEDICINE_BALL_VIOLATION = "07010005" //成绩违规
    const val MEDICINE_BALL_PRE_STANCE_POSITION = "07010006" //预摆姿势
    const val MEDICINE_BALL_PRE_STANCE_POSITION_S = "0701000601" //预摆姿势
    const val MEDICINE_BALL_STAND_SPACE = "07010007" //站姿间距
    const val MEDICINE_BALL_STAND_SPACE_S = "0701000701" //站姿间距
    const val MEDICINE_BALL_THROW_ELBOW = "07010008" //引球手肘
    const val MEDICINE_BALL_THROW_ELBOW_S = "0701000801" //引球手肘
    const val MEDICINE_BALL_THROW_ARM = "07010009" //引球手臂
    const val MEDICINE_BALL_THROW_ARM_S = "0701000901" //引球手臂
    const val MEDICINE_BALL_BACK_BEND_KNEE = "07010010" //反弓伸髋
    const val MEDICINE_BALL_BACK_BEND_KNEE_S = "0701001001" //反弓伸髋
    const val MEDICINE_BALL_BACK_BEND_HIP = "07010011" //反弓屈膝
    const val MEDICINE_BALL_BACK_BEND_HIP_S = "0701001101" //反弓屈膝
    const val MEDICINE_BALL_THROW_POSITION = "07010012" //出手姿势
    const val MEDICINE_BALL_THROW_POSITION_S = "0701001201" //出手姿势
    const val MEDICINE_BALL_THROW_ANGLE = "07010013" //出手角度
    const val MEDICINE_BALL_THROW_ANGLE_S = "0701001301" //出手角度
    const val MEDICINE_BALL_THROW_TIME = "07010014" //出手时机
    const val MEDICINE_BALL_THROW_TIME_S = "0701001401" //出手时机
    const val MEDICINE_BALL_MOVE_TRACK = "07010015" //运动轨迹
    const val MEDICINE_BALL_MOTION_ANALYSIS = "07010016" //动作分析
    const val MEDICINE_BALL_OFFSET = "07010020" //偏移情况
    const val MEDICINE_BALL_OFFSET_S = "0701002001" //偏移情况
    /**************************************实心球**********************************/

    /**************************乒乓球正手攻反手推挡指标参数*******************************/
    const val PING_PONG_HC_BALL_LANDING_POINT_DISTRIBUTION = "0130001" // 回球落点分布
    const val PING_PONG_HC_SINGLE_BALL_RETURN_TIME_DISTRIBUTION = "0130003" // 单个回球时间分布
    const val PING_PONG_HC_INTERRUPTED_TIME_AREA_DISTRIBUTION = "0130004" // 中断时间区域分布
    const val PING_PONG_HC_MAXIMUM_CONSECUTIVE_HITS = "0130005" // 最大连击数
    const val PING_PONG_HC_SAFE_ZONE_SIZE = "0130006" // 安全区大小
    const val PING_PONG_HC_TABLE_SIZE = "0130002" // 台面大小
    const val PING_PONG_HC_LANDING_POINT_LEFT_ZONE_RATIO = "0130007" // 落点分布左区占比
    const val PING_PONG_HC_LANDING_POINT_RIGHT_ZONE_RATIO = "0130008" // 落点分布右区占比
    const val PING_PONG_HC_LANDING_POINT_LEFT_SAFE_ZONE_RATIO = "0130009" // 落点分布左安全区占比
    const val PING_PONG_HC_LANDING_POINT_RIGHT_SAFE_ZONE_RATIO = "0130010" // 落点分布右安全区占比
    const val PING_PONG_HC_HIGHEST_PARABOLA = "0130015" // 最高抛物线
    const val PING_PONG_HC_LOWEST_PARABOLA = "0130016" // 最低抛物线
    const val PING_PONG_HC_FASTEST_BALL_SPEED_PARABOLA = "0130017" // 球速最快抛物线
    const val PING_PONG_HC_SLOWEST_BALL_SPEED_PARABOLA = "0130018" // 球速最慢抛物线
    /**************************乒乓球正手攻反手推挡指标参数*******************************/



    /**************************足球运球绕杆射门指标参数*******************************/
    const val FOOTBALL_DRIBBLE_SHOOT_PROCEDURAL_TIME = "110102002" // 过程性时间指标
    const val FOOTBALL_DRIBBLE_SHOOT_SHOT_TIME = "110102003" // 射门耗时
    const val FOOTBALL_DRIBBLE_SHOOT_SHOT_RESULT = "110102005" // 射门结果
    const val FOOTBALL_DRIBBLE_SHOOT_PROCEDURAL_VIOLATION = "110102004" // 过程性违规指标
    const val FOOTBALL_DRIBBLE_SHOOT_DRIBBLE_TURN_TIMING = "110102101" // 专项绕杆变向时机
    const val FOOTBALL_DRIBBLE_SHOOT_MOTION_TRACK_COORDINATES = "110102102" // 运动轨迹坐标
    /**************************足球运球绕杆射门指标参数*******************************/

    /**************************篮球运球绕杆上篮指标参数*******************************/
    const val BASKETBALL_DRIBBLE_SHOOT_PROCEDURAL_TIME = "100102003" // 过程性时间指标（去程）
    const val BASKETBALL_DRIBBLE_SHOOT_LAYUP_TIME = "100102006" // 上篮耗时指标
    const val BASKETBALL_DRIBBLE_SHOOT_PROCEDURAL_VIOLATION = "100102008" // 过程性违规指标（去程）
    const val BASKETBALL_DRIBBLE_SHOOT_LAYUP_RESULT = "100102009" // 上篮结果
    const val BASKETBALL_DRIBBLE_SHOOT_DRIBBLE_TURN_TIMING = "100102101" // 专项绕杆变向时机（去程）
    const val BASKETBALL_DRIBBLE_SHOOT_MOTION_TRACK_COORDINATES = "100102103" // 运动轨迹坐标
    /**************************篮球运球绕杆上篮指标参数*******************************/

    /**************************足球运球绕标志杆指标参数*******************************/
    const val FOOTBALL_DRIBBLE_PROCESS_DISTANCE = "11010001" // 过程性距离指标
    const val FOOTBALL_DRIBBLE_PROCESS_TIME = "11010002" // 过程性时间指标
    const val FOOTBALL_DRIBBLE_SERVE_REACTION_TIME = "11010003" // 发球反应时间
    const val FOOTBALL_DRIBBLE_PROCESS_VIOLATION_GOING = "11010004" // 过程性违规指标
    const val FOOTBALL_DRIBBLE_SPECIAL_CHANGE_DIRECTION_GOING = "11011001" // 专项绕杆变向时机
    const val FOOTBALL_DRIBBLE_MOVEMENT_TRAJECTORY = "11011002" // 运动轨迹
    /**************************足球运球绕标志杆指标参数*******************************/

    /**************************篮球运球绕标志杆指标参数*******************************/
    const val BASKETBALL_DRIBBLE_PROCESS_DISTANCE_GOING = "10010001" // 过程性距离指标（去程）
    const val BASKETBALL_DRIBBLE_PROCESS_DISTANCE_RETURN = "10010002" // 过程性距离指标（返程）
    const val BASKETBALL_DRIBBLE_PROCESS_TIME_GOING = "10010003" // 过程性时间指标（去程）
    const val BASKETBALL_DRIBBLE_PROCESS_TIME_RETURN = "10010004" // 过程性时间指标（返程）
    const val BASKETBALL_DRIBBLE_DRIBBLE_TURN_TIME = "10010005" // 运球转身时间指标
    const val BASKETBALL_DRIBBLE_DRIBBLE_TURN_VIOLATION = "10010011" // 运球转身违规指标
    const val BASKETBALL_DRIBBLE_SERVE_REACTION_TIME = "10010007" // 发球反应时间
    const val BASKETBALL_DRIBBLE_PROCESS_VIOLATION_GOING = "10010008" // 过程性违规指标（去程）
    const val BASKETBALL_DRIBBLE_PROCESS_VIOLATION_RETURN = "10010009" // 过程性违规指标（返程）
    const val BASKETBALL_DRIBBLE_SPECIAL_CHANGE_DIRECTION_GOING = "10011001" // 专项绕杆变向时机（去程）
    const val BASKETBALL_DRIBBLE_SPECIAL_CHANGE_DIRECTION_RETURN = "10011002" // 专项绕杆变向时机（返程）
    const val BASKETBALL_DRIBBLE_MOVEMENT_TRAJECTORY = "10011003" // 运动轨迹
    /**************************篮球运球绕标志杆指标参数*******************************/
}