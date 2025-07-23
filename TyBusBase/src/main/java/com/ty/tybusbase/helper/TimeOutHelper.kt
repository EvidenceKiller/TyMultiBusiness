package com.ty.tybusbase.helper

import android.os.CountDownTimer
import com.aipower.modulecommon.expand.Action

class TimeOutHelper(private var timeoutCallback: Action, private var timeOut: Long) :
    CountDownTimer(timeOut, 1000) {

    override fun onTick(millisUntilFinished: Long) {

    }

    override fun onFinish() {
        timeoutCallback.invoke()
    }
}