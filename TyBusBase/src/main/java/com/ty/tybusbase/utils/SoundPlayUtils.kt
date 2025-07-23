package com.ty.tybusbase.utils

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.ty.tybusbase.R
import com.ty.tybusbase.manager.ThreadManager

/**
 * @description:
 * @author: chenwei
 * @date :   2022/4/2 4:28 下午
 */
object SoundPlayUtils {
    // SoundPool对象
    private var mSoundPlayer = SoundPool(
        10,
        AudioManager.STREAM_NOTIFICATION, 5
    )

    /**
     * 初始化
     */
    @JvmStatic
    fun init(mContext: Context) {
        mSoundPlayer.load(mContext, R.raw.duang, 1) // 1s 1
        mSoundPlayer.load(mContext, R.raw.complete, 1) // 1s 2
        mSoundPlayer.load(mContext, R.raw.faill, 1) // 1s 3
        mSoundPlayer.load(mContext, R.raw.interest_duang, 1) // 1s 4
        mSoundPlayer.load(mContext, R.raw.fire, 1) // 1s 发射导弹 5
        mSoundPlayer.load(mContext, R.raw.fire2, 1) // 飞机发射小子弹  6
        mSoundPlayer.load(mContext, R.raw.explore, 1) // 1s //爆炸 7
        mSoundPlayer.load(mContext, R.raw.sinister_smile, 1) // 4s 得到蛋糕 8
        mSoundPlayer.load(mContext, R.raw.sinister_smile, 1) // 1s 奸笑 9
        mSoundPlayer.load(mContext, R.raw.di, 1) // 1s 统一测试的di声音 10
        mSoundPlayer.load(mContext, R.raw.cat_mouse_fail, 1) // 猫鼠游戏挑战失败 11
        mSoundPlayer.load(mContext, R.raw.cat_mouse_dot_move, 1) // 不许动 12
        mSoundPlayer.load(mContext, R.raw.cat_mouse_luck_u, 1) // 算你运气好 13
        mSoundPlayer.load(mContext, R.raw.cat_mouse_get_u, 1) // 抓住你了 14
        mSoundPlayer.load(mContext, R.raw.cat_mouse_cat_turn, 1) // 猫咪转身 15
        mSoundPlayer.load(mContext, R.raw.metal, 1) // 金属碰撞 16
        mSoundPlayer.load(mContext, R.raw.get_big_bullet, 1) //获得道具（大子弹）17
        mSoundPlayer.load(mContext, R.raw.get_missle, 1) //获得导弹 18
        mSoundPlayer.load(mContext, R.raw.laser_shot, 1) //激光枪射击 19
        mSoundPlayer.load(mContext, R.raw.laser_big_shot, 1) //激光枪射击 20
        mSoundPlayer.load(mContext, R.raw.harder, 1) //难度提升 21
        mSoundPlayer.load(mContext, R.raw.pacman_eat, 1) //难度提升 22
        mSoundPlayer.load(mContext, R.raw.qr_ok, 1) //难度提升 23
        mSoundPlayer.load(mContext, R.raw.skip_rope_multi_finish_bgm, 1) //无人跳绳结束 24
        mSoundPlayer.load(mContext, R.raw.multi_bgm_step_1, 1) //多人运动音效1  25
        mSoundPlayer.load(mContext, R.raw.multi_bgm_step_2, 1) //多人运动音效2  26
        mSoundPlayer.load(mContext, R.raw.multi_bgm_step_3, 1) //多人运动音效3  27
        mSoundPlayer.load(mContext, R.raw.multi_bgm_step_4, 1) //多人运动音效4  28
        mSoundPlayer.load(mContext, R.raw.multi_bgm_step_5, 1) //多人运动音效5  29
    }

    /**
     * 播放声音
     *
     * @param soundID
     */
    fun play(soundID: Int) {
        ThreadManager.getInstance().postBackTask { mSoundPlayer.play(soundID, 1f, 1f, 0, 0, 1f) }
    }
}