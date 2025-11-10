package com.ai.ui.facetest

import com.ty.bus.multi.demo.databinding.ActivityFaceTestBinding
import com.ty.tybusbase.base.BaseSingleIdentifyActivity
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.utils.ToastUtil

class FaceTestActivity : BaseSingleIdentifyActivity<ActivityFaceTestBinding, BaseSportViewModel>() {

    private var type = 0

    override fun initData() {
        super.initData()
        type = intent.getIntExtra("type", 0)
    }

    override fun onStatusPrepare() {
        super.onStatusPrepare()
        viewModel.toStatusIdentifyFace()
    }


    override fun onStatusIdentifySuccess() {
        super.onStatusIdentifySuccess()

        val accountInfo = viewModel.accountInfoLD.value!!
        if (accountInfo.studentEntity?.userId == BusiBaseConstantsValue.VISITOR_USER_ID) {
            ToastUtil.showErrorHint(this, "人脸识别失败")
            SpeechWrapper.speech("人脸识别失败，请重试")
            viewModel.toStatusIdentifyFace()
            return
        }
        ToastUtil.getInstance(this, "识别到人员：${accountInfo.studentEntity?.accountName}").show()
        SpeechWrapper.speech("识别到人员：${accountInfo.studentEntity?.accountName}")
        finish()
    }

}