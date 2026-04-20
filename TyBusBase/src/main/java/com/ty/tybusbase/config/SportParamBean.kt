package com.ty.tybusbase.config

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

/**
 * @author zhouwenhua
 * @date 2023/9/25
 * @describe 运动参数存储类
 */

abstract class CommonParamBean : Parcelable {
    var enableBreakRuleVoice = true

    constructor()

    constructor(parcel: Parcel) {
        enableBreakRuleVoice = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enableBreakRuleVoice) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonParamBean> {
        override fun createFromParcel(parcel: Parcel): CommonParamBean {
            throw UnsupportedOperationException("CommonParamBean is abstract")
        }

        override fun newArray(size: Int): Array<CommonParamBean?> {
            return arrayOfNulls(size)
        }
    }
}

class JumpParamBean : CommonParamBean {
    var enableOverLine = false
    var enableFootBack = true
    var enableHandDown = true
    var enableHipDown = true
    var overLineDx = 6

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        enableOverLine = parcel.readByte() != 0.toByte()
        enableFootBack = parcel.readByte() != 0.toByte()
        enableHandDown = parcel.readByte() != 0.toByte()
        enableHipDown = parcel.readByte() != 0.toByte()
        overLineDx = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeByte(if (enableOverLine) 1 else 0)
        parcel.writeByte(if (enableFootBack) 1 else 0)
        parcel.writeByte(if (enableHandDown) 1 else 0)
        parcel.writeByte(if (enableHipDown) 1 else 0)
        parcel.writeInt(overLineDx)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JumpParamBean> {
        override fun createFromParcel(parcel: Parcel): JumpParamBean {
            return JumpParamBean(parcel)
        }

        override fun newArray(size: Int): Array<JumpParamBean?> {
            return arrayOfNulls(size)
        }
    }
}

@SuppressLint("ParcelCreator")
class SiteUpParamBean : CommonParamBean {
    var enableHandOk = true//双手抱头
    var enableKneeAngleOk = true//屈膝90°
    var enableHipUp = false//垫臀
    var hipUpOverLineDx = 0//垫臀阈值

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        enableHandOk = parcel.readByte() != 0.toByte()
        enableKneeAngleOk = parcel.readByte() != 0.toByte()
        enableHipUp = parcel.readByte() != 0.toByte()
        hipUpOverLineDx = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeByte(if (enableHandOk) 1 else 0)
        parcel.writeByte(if (enableKneeAngleOk) 1 else 0)
        parcel.writeByte(if (enableHipUp) 1 else 0)
        parcel.writeInt(hipUpOverLineDx)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SiteUpParamBean> {
        override fun createFromParcel(parcel: Parcel): SiteUpParamBean {
            return SiteUpParamBean(parcel)
        }

        override fun newArray(size: Int): Array<SiteUpParamBean?> {
            return arrayOfNulls(size)
        }
    }
}

class PullUpParamBean : CommonParamBean {
    var enableMandibleCrossover = true
    var enableStandStill = false
    var enableOverhang = false
    var enableCountDown = true

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        enableMandibleCrossover = parcel.readByte() != 0.toByte()
        enableStandStill = parcel.readByte() != 0.toByte()
        enableOverhang = parcel.readByte() != 0.toByte()
        enableCountDown = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeByte(if (enableMandibleCrossover) 1 else 0)
        parcel.writeByte(if (enableStandStill) 1 else 0)
        parcel.writeByte(if (enableOverhang) 1 else 0)
        parcel.writeByte(if (enableCountDown) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PullUpParamBean> {
        override fun createFromParcel(parcel: Parcel): PullUpParamBean {
            return PullUpParamBean(parcel)
        }

        override fun newArray(size: Int): Array<PullUpParamBean?> {
            return arrayOfNulls(size)
        }
    }
}

class ZwtqqParamBean : CommonParamBean {
    var sxsz = true
    var szth = 75
    var sjbl = true
    var sspx = false

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        sxsz = parcel.readByte() != 0.toByte()
        szth = parcel.readInt()
        sjbl = parcel.readByte() != 0.toByte()
        sspx = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeByte(if (sxsz) 1 else 0)
        parcel.writeInt(szth)
        parcel.writeByte(if (sjbl) 1 else 0)
        parcel.writeByte(if (sspx) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZwtqqParamBean> {
        override fun createFromParcel(parcel: Parcel): ZwtqqParamBean {
            return ZwtqqParamBean(parcel)
        }

        override fun newArray(size: Int): Array<ZwtqqParamBean?> {
            return arrayOfNulls(size)
        }
    }
}