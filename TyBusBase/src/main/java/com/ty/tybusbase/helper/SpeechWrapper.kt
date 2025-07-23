package com.ty.tybusbase.helper

import android.content.Context
import com.adl.base.speech.AdlTextSpeaker
import com.ty.tybusbase.utils.SoundPlayUtils

/**
 * @ProjectName: SportBS
 * @Date: 2023/1/17
 * @Desc:语音播放器
 */
object SpeechWrapper {
    private const val useXunfei = true //是否使用科大讯飞的语音sdk，true：使用，false：不使用

    fun init(context: Context) {
//        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=99edb9cf");
        AdlTextSpeaker.init(context)
        SoundPlayUtils.init(context)
        //        TextToSpeechUtil.init(context);
    }


    fun speech(text: String?) {
        if (useXunfei) {
            AdlTextSpeaker.speak(text)
        } else {
//            TextToSpeechUtil.speech(text);
        }
    }

    fun speech2(text: String?, afterSpeak: Runnable?) {
        if (useXunfei) {
            AdlTextSpeaker.speak(text, afterSpeak)
        } else {
//            TextToSpeechUtil.speech2(text, afterSpeak);
        }
    }

    fun speakAfterStop(text: String?) {
        if (useXunfei) {
            AdlTextSpeaker.speakAfterStop(text)
        } else {
//            TextToSpeechUtil.speech(text);
        }
    }

    fun stop() {
        AdlTextSpeaker.stop()
        //        TextToSpeechUtil.stop();
    }
}