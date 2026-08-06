package com.ty.tybusbase.constants

import androidx.annotation.IntDef
import androidx.annotation.StringDef


/**
 * @author :WEI_CP
 * Time:2022/3/2 19:24
 * Description: this is SportContact
 */
object SportConstant {

    //赛事
    const val MATCH: String = "MATCH"

    //特训
    const val TRAIN: String = "TRAIN"

    const val SPORT_PERFECT: Int = 5
    const val SPORT_TERRIFIC: Int = 3
    const val SPORT_COME_ON: Int = 1

    //1计分，2计数，3计时
    /**
     * 结束方式
     * 计时	1	Number 成绩就是个数
     * 计数	2	Number 成绩就是时间
     * 其他	99	Number
     */
    const val END_TYPE_TIME: String = "1"
    const val END_TYPE_COUNT: String = "2"
    const val END_TYPE_OTHER: String = "99"

    /**
     * 测量方式
     * 1	记时长	1	Number
     * 2	记次数	2	Number
     * 3	测距离	3	Number
     * 4	计重量	4	Number
     * 5	记体积	5	Number
     * 6	计分	6	Number
     */
    const val MEASURE_CODE_TIME: String = "1"
    const val MEASURE_CODE_COUNT: String = "2"
    const val MEASURE_CODE_DISTANCE: String = "3"
    const val MEASURE_CODE_WEIGHT: String = "4"
    const val MEASURE_CODE_V: String = "5"
    const val MEASURE_CODE_SCORE: String = "6"

    /**
     * 定义优秀
     */
    const val GOOD: Int = 'A'.code

    /**
     * 定义良好
     */
    const val FINE: Int = 'B'.code

    /**
     * 成绩高低优
     * 高优	1	Number
     * 低优	2	Number
     * 其它	9	Number
     */
    const val SPORT_PRIORITY_HIGH: String = "1"
    const val SPORT_PRIORITY_LOW: String = "2"
    const val SPORT_PRIORITY_OTHER: String = "9"

    const val SPORT_LOCATION_LEFT: String = "left"
    const val SPORT_LOCATION_RIGHT: String = "right"
    const val SPORT_LOCATION_DEFAULT: String = "default"


    /**
     * 限定时间的运动模式
     */
    const val PATTERN_LIMIT_TIME: String = "1"

    /**
     * 限定运动数量模式
     */
    const val PATTERN_LIMIT_COUNT: String = "2"

    //表示仰卧起坐
    const val SPORT_SIT_UP: String = "060101"

    //表示开合跳 一期
    const val SPORT_OPEN_CLOSE_JUMP: String = "080101"

    //下蹲 一期
    const val SPORT_SQUATS: String = "450101"

    //单脚跳绳 一期
    const val SPORT_ONE_LEG_SKIP_ROPE: String = "190101"

    //前后跳绳 一期
    const val SPORT_FRONT_BACK_SKIP_ROPE: String = "180101"

    //左右跳 一期
    const val SPORT_LEFT_RIGHT_ROPE: String = "170101"

    //表示俯卧撑
    const val SPORT_PUSH_UP: String = "130101"

    //跳绳 一期
    const val SPORT_SKIP_ROPE: String = "020101"

    //交叉跳绳
    const val SPORT_CROSS_SKIP_ROPE: String = "020201"

    //交替跳绳
    const val SPORT_ALTERNATE_SKIP_ROPE: String = "020301"

    //开合跳绳
    const val SPORT_JUMPING_JACK_SKIP_ROPE: String = "020401"

    //双摇跳绳
    const val SPORT_DOUBLE_SHAKE_JUMP: String = "020501"

    //八字跳绳
    const val SPORT_SKIP_EIGHT_JUMP: String = "901408"


    //跳绳sportSkuId
    const val SPORT_SKIP_ROPE_SKU_ID: String = "0201010001"

    //表示高抬腿  一期
    const val SPORT_HIGH_KNEE: String = "090101"

    //立定跳远
    const val SPORT_LONG_JUMP: String = "030101"

    //平板支撑
    const val SPORT_PLANK: String = "1009"

    //体前屈
    const val SPORT_UTTHANASANA: String = "160201"

    //坐位体前屈
    const val SPORT_FORWARD_FLEXION: String = "160101"

    //引体向上
    const val SPORT_PULL_UP: String = "040101"

    //原地前踢
    const val SPORT_KICH_FORWARD: String = "430101"

    // 侧弓步 SideBow
    const val SPORT_SIDE_BOW: String = "230101"

    //表示红旗小游戏
    const val SPORT_RED_FLAG_GAME: String = "2001"

    //成语填空
    const val SPORT_FILL_BLANK_GAME: String = "990106"

    //单词填空
    const val SPORT_FILL_BLANK_EN_GAME: String = "990117"

    //敏捷摸球
    const val SPORT_TOUCH_THE_BALL: String = "990105"

    //石头剪刀不
    const val SPORT_STONE_SCISSORS_CLOTH: String = "990110"

    //我不是吃货
    const val SPORT_NOT_FOODIE: String = "990118"

    //顺序摸球
    const val SPORT_ORDER_TOUCH_BALL: String = "990116"

    //滑雪大作战
    const val SPORT_SKIING: String = "990103"

    //全身入框
    const val SPORT_BODY_IN_FRAME: String = "990111"

    //5育闯关
    const val SPORT_FIVE_EDUCATE_EXAM: String = "990119"


    //振臂运动
    const val SPORT_WITH_ARM: String = "220101"

    //扩胸运动
    const val SPORT_ENLARGE_BOSOM: String = "210101"

    //小蜜蜂
    const val SPORT_GAME_BEE: String = "990102"

    //猫鼠游戏
    const val SPORT_CAT_RAT: String = "990104"

    //吃豆人游戏
    const val SPORT_GAME_PACMAN: String = "990108"

    //飞机大战
    const val SPORT_PLANE_WAR: String = "990101"

    //左右手单手摇绳
    const val SPORT_SWING_ONE_HAND: String = "400101"

    //平举环绕
    const val SPORT_ARM_AROUND: String = "420101"

    //小跑
    const val SPORT_TROT: String = "320101"

    //慢速摇绳跳
    const val SPORT_SLOW_SKIP_ROPE: String = "410101"

    //纵跳摸高
    const val SPORT_SQUAT_JUMP_HIGH: String = "250101"

    //  肺活量
    const val LUNG_CAPACITY: String = "000401"

    // BMI
    const val BODY_HEIGHT_WEIGHT: String = "000301"

    //身高
    const val SPORT_HEIGHT: String = "000101"

    //体重
    const val SPORT_WEIGHT: String = "000201"

    // 篮球对抗
    const val SPORT_BASKET_BALL_BATTLE = "100701"

    // 自由投篮
    const val SPORT_SHOT_BALL_FREE = "100403"

    // 定点投篮
    const val SPORT_SHOT_BALL_L1 = "900601"
    const val SPORT_SHOT_BALL_L2 = "900603"
    const val SPORT_SHOT_BALL_L3 = "900605"

    /*
    乒乓球正手攻球
    乒乓球1分钟左推右攻
    乒乓球正手攻反手推挡
    4801010001
    4802010001
    4803010001
  */
    const val SPORT_PING_PONG_FOREHAND_ATTACK = "480101"
    const val SPORT_PING_PONG_PUSH_LEFT_ATTACK_RIGHT = "480201"
    const val SPORT_PING_PONG_FOREHAND_ATTACK_BACKHAND_BLOCK = "480301"

    /**
     * @hide
     */
    @IntDef(SPORT_PERFECT, SPORT_TERRIFIC, SPORT_COME_ON)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SingleSportLevel


    //1028原地小跑
    //1037单手摇绳
    //1038慢速摇绳跳
    //1039平举环绕
    @StringDef(
        SPORT_SIT_UP,
        SPORT_OPEN_CLOSE_JUMP,
        SPORT_SQUATS,
        SPORT_SKIP_ROPE,
        SPORT_PUSH_UP,
        SPORT_HIGH_KNEE,
        SPORT_LONG_JUMP,
        SPORT_PLANK,
        SPORT_UTTHANASANA,
        SPORT_LEFT_RIGHT_ROPE,
        SPORT_FRONT_BACK_SKIP_ROPE,
        SPORT_ONE_LEG_SKIP_ROPE,
        SPORT_RED_FLAG_GAME,
        SPORT_STONE_SCISSORS_CLOTH,
        SPORT_SWING_ONE_HAND,
        SPORT_ARM_AROUND,
        SPORT_TROT,
        SPORT_SLOW_SKIP_ROPE,
        SPORT_SQUAT_JUMP_HIGH
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SportAiCode

    @StringDef(SPORT_LOCATION_LEFT, SPORT_LOCATION_RIGHT, SPORT_LOCATION_DEFAULT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SportLocation
}
