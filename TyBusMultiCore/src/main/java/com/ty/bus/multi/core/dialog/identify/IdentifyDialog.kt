package com.ty.bus.multi.core.dialog.identify

import android.content.DialogInterface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.lifecycleScope
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.service.outer.UserCaller
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.DialogIdentifyBinding
import com.ty.nfc.reader.base.NfcType
import com.ty.nfc.reader.base.ReadCallback
import com.ty.nfc.reader.manager.NfcReaderManager
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.config.BusiBaseConfig.getIdentityType
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.invisible
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.SpeechWrapper.speech
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIFrameCountIdentityEngine
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.MMKVUtil.decodeInt
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IdentifyDialog : BaseDialogFragment<DialogIdentifyBinding, IdentifyViewModel>() {

    private var identifyResultListener: Action1<HashMap<Int, StudentEntity>>? = null

    private var identifiedMap: HashMap<Int, StudentEntity> = HashMap()

    //连接状态
    private var blueConnectList = HashMap<Int, Boolean>()

    private var planId: String? = null

    private val identifyEngine by lazy {
        MultiIFrameCountIdentityEngine()
    }

    private val nfcReaderManager by lazy {
        NfcReaderManager()
    }

    private val onceCacheProcessor by lazy {
        object : AdlCameraFrameProcessor {
            override fun onFrame(yuv: ByteArray?, cameraPreviewInfo: AdlCameraPreviewInfo) {
                identify(yuv, cameraPreviewInfo)
            }
        }
    }

    private var currentIndex: Int = 1

    /**
     * 人脸识别标识
     */
    private var identifyFlag = false

    override fun initViewObservable() {
    }

    override fun initData() {

    }

    override fun initView() {
        super.initView()
        mBinding.tvCancel.setOnClickListener {
            identifiedMap?.clear()
            dismiss()
        }

        mBinding.tvConfirm.setOnClickListener {
            dismiss()
            identifyResultListener?.invoke(identifiedMap ?: HashMap())
        }

        //如果添加了已识别的人，处理显示已识别的人
        identifiedMap?.forEach { (index, value) ->
            dispatchStudent(index, value)
        }

        mBinding.llAdd1.setOnClickListener {
            identifyEngine.reset()
            currentIndex = 1
            identifyFlag = true
            dispatchIdentifyingView(1)
        }

        mBinding.llAdd2.setOnClickListener {
            identifyEngine.reset()
            currentIndex = 2
            identifyFlag = true
            dispatchIdentifyingView(2)
        }

        mBinding.llAdd3.setOnClickListener {
            identifyEngine.reset()
            currentIndex = 3
            identifyFlag = true
            dispatchIdentifyingView(3)
        }
        initTip()
        handleClickEnable()
        initCamera()
        initFaceDetect()
        initCard()
    }

    private fun initTip() {
        val identityType = getIdentityType()
        when (identityType) {
            BusiBaseConstantsValue.IDENTITY_TYPE_FACE -> {
                mBinding.tvTitle.text =
                    com.ty.tybusbase.expand.getString(R.string.identify_face)
                mBinding.tvIdentifyTip.text =
                    com.ty.tybusbase.expand.getString(R.string.busi_multi_dialog_identify_tip_face)
            }

            BusiBaseConstantsValue.IDENTITY_TYPE_FACE or BusiBaseConstantsValue.IDENTITY_TYPE_CARD -> {
                mBinding.tvTitle.text =
                    com.ty.tybusbase.expand.getString(R.string.identify_face_nfc)
                mBinding.tvIdentifyTip.text =
                    com.ty.tybusbase.expand.getString(R.string.busi_multi_dialog_identify_tip_face_nfc)
            }

            BusiBaseConstantsValue.IDENTITY_TYPE_CARD -> {
                mBinding.tvTitle.text =
                    com.ty.tybusbase.expand.getString(R.string.identify_nfc)
                mBinding.tvIdentifyTip.text =
                    com.ty.tybusbase.expand.getString(R.string.busi_multi_dialog_identify_tip_nfc)
            }
        }
    }

    /**
     * 处理未连接的位置不能识别人员逻辑
     */
    private fun handleClickEnable() {
        blueConnectList.forEach { (index, value) ->
            when (index) {
                1 -> {
                    if (!value) {
                        mBinding.llAdd1.isClickable = false
                        mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd1Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd1Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }

                2 -> {
                    if (!value) {
                        mBinding.llAdd2.isClickable = false
                        mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd2Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd2Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }

                3 -> {
                    if (!value) {
                        mBinding.llAdd3.isClickable = false
                        mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd3Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd3Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }
            }
        }
    }

    private fun initCard() {
        val identityType = decodeInt(
            MMKVBusiKey.IDENTITY_TYPE,
            BusiBaseConstantsValue.IDENTITY_TYPE_FACE
        );
        if ((identityType and BusiBaseConstantsValue.IDENTITY_TYPE_CARD) != BusiBaseConstantsValue.IDENTITY_TYPE_CARD) {
            //不支持刷卡
            LogUtil.d("当前未开启刷卡功能")
            return
        }
        val readerType =
            decodeInt(MMKVBusiKey.NFC_READER_TYPE, BusiBaseConstantsValue.DEFAULT_NFC_READER_TYPE)
        var type: NfcType = NfcType.NFC_A
        if (readerType == BusiBaseConstantsValue.NFC_READER_TYPE_B) {
            type = NfcType.NFC_B
        }
        nfcReaderManager.init(requireContext(), type)
        nfcReaderManager.startRead(object : ReadCallback {
            override fun readCardBack(cardStr: String?) {
                onCardReadBack(cardStr)
            }
        })
    }

    //读到卡
    private fun onCardReadBack(cardStr: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val studentList = UserCaller.instance().queryStudentByCarNum(cardStr)
            for (baseForFace in studentList) {
                val cardNumList = baseForFace.cardNums
                if (cardNumList == null || cardNumList.isEmpty()) continue
                if (cardNumList.contains(cardStr)) {
                    if (isContainStudent(baseForFace)) {
                        //已经添加过此人
                        ToastUtil.getInstance(
                            mBinding.root.context,
                            getString(R.string.busi_multi_already_add)
                        ).show()
                        return@launch
                    }
                    viewModel.checkStudent(baseForFace.accountId, planId) {
                        if (it) {
                            dispatchCardStudent(baseForFace.apply {
                                userFaceImgUrl =
                                    AdlService.getService().facePrefix + baseForFace.userFaceImgUrl
                            })
                        } else {
                            toastWithoutPlan(baseForFace.accountName)
                        }
                    }
                    return@launch
                }
            }
            speech(mBinding.getRoot().context.getString(R.string.rec_face_fail))
            ToastUtil.getInstance(
                mBinding.getRoot().context,
                mBinding.getRoot().context.getString(R.string.rec_face_fail)
            ).show()
        }
    }

    /**
     * 刷到卡后分发学生，以此填充，1~3
     */
    private fun dispatchCardStudent(value: StudentEntity) {
        val index =
            if (mBinding.groupAdded1.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    1,
                    false
                )
            ) {
                1
            } else if (mBinding.groupAdded2.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    2,
                    false
                )
            ) {
                2
            } else if (mBinding.groupAdded3.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    3,
                    false
                )
            ) {
                3
            } else {
                return
            }
        identifiedMap?.put(index, value)
        dispatchStudent(index, value)
    }

    private fun dispatchIdentifyingView(index: Int) {
        when (index) {
            1 -> {
                mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd1Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }

            2 -> {
                mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd2Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }

            3 -> {
                mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd3Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }
        }
    }

    /**
     * 重置正在识别的View
     */
    private fun resetIdentifyingView(index: Int) {
        when (index) {
            1 -> {
                mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd1Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }

            2 -> {
                mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd2Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }

            3 -> {
                mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd3Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }
        }
    }

    fun isContainStudent(stu: StudentEntity): Boolean {
        for ((_, value1) in identifiedMap) {
            if (value1.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && value1.accountId == stu.accountId) {
                return true
            }
        }
        return false
    }

    /**
     * 分发学生到UI
     */
    private fun dispatchStudent(index: Int, value: StudentEntity?) {
        if (value == null) {
            return
        }
        when (index) {
            1 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd1,
                    mBinding.groupAdded1,
                    mBinding.ivHeader1,
                    mBinding.tvAddName1,
                    mBinding.ivDeleteUser1,
                    value
                )
            }

            2 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd2,
                    mBinding.groupAdded2,
                    mBinding.ivHeader2,
                    mBinding.tvAddName2,
                    mBinding.ivDeleteUser2,
                    value
                )
            }

            3 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd3,
                    mBinding.groupAdded3,
                    mBinding.ivHeader3,
                    mBinding.tvAddName3,
                    mBinding.ivDeleteUser3,
                    value
                )
            }
        }
    }

    private fun identify(yuv: ByteArray?, cameraPreviewInfo: AdlCameraPreviewInfo) {
        if (identifyFlag.not()) return
        //裁剪图片
        identifyEngine.identify(
            mBinding.identifyDargView.getRect(
                BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH,
                BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT
            ),
            yuv,
            cameraPreviewInfo.width,
            cameraPreviewInfo.height,
            currentIndex
        )

    }

    private fun initFaceDetect() {
        identifyEngine.openMachine(object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityTimeOn() {

            }

            override fun onIdentityOk(accountInfo: AccountInfo?) {
                handleIdentifyOk(accountInfo)
                mBinding.root.post {
                    resetIdentifyingView(currentIndex)
                }
            }

            override fun onIdentityTimeFail() {
                LogUtil.d("单次识别超时,未识别到人脸")
                mBinding.root.post {
                    resetIdentifyingView(currentIndex)
                }
            }

        })
    }

    private fun handleIdentifyOk(info: AccountInfo?) {
        identifyFlag = false
        info?.studentEntity?.let { stu ->
            if (isContainStudent(stu)) {
                //已经添加过此人
                ToastUtil.getInstance(
                    mBinding.root.context,
                    getString(R.string.busi_multi_already_add)
                ).show()
                return
            }
            viewModel.checkStudent(stu.accountId, planId) {
                if (it) {
                    identifiedMap.put(currentIndex, stu)
                    mBinding.root.post {
                        dispatchStudent(currentIndex, info.studentEntity)
                    }
                } else {
                    toastWithoutPlan(stu.accountName)
                }
            }

        }
    }

    private fun toastWithoutPlan(accountName: String) {
        ToastUtil.getInstance(mBinding.root.context, getString(R.string.busi_base_name_without_plan, accountName))
            .show()
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBinding.cameraView.addProcessor(onceCacheProcessor)
        mBinding.cameraView.onResume()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mBinding.cameraView.onPause()
        mBinding.cameraView.onRelease()
        identifyEngine.closeMachine()
        nfcReaderManager.stopRead()
        nfcReaderManager.release()
    }

    private fun handleIdentifyView(
        index: Int,
        addGroup: Group,
        identifyGroup: Group,
        ivHeader: ImageView,
        tvName: TextView,
        deleteView: ImageView,
        studentEntity: StudentEntity
    ) {
        addGroup.invisible()
        identifyGroup.visible()
        deleteView.setOnClickListener {
            identifiedMap.remove(index)
            addGroup.visible()
            identifyGroup.gone()
        }
        Glide.with(requireContext())
            .load(studentEntity.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default)
            .into(ivHeader)
        tvName.text = studentEntity.accountName
    }


    fun setIdentifiedMap(identifiedMap: HashMap<Int, StudentEntity>): IdentifyDialog {
        this.identifiedMap.putAll(identifiedMap)
        return this
    }

    fun setBlueConnectList(blue: HashMap<Int, Boolean>): IdentifyDialog {
        this.blueConnectList = blue
        return this
    }

    fun setPlanId(planId: String?): IdentifyDialog {
        this.planId = planId
        return this
    }

    fun setIdentifyResultListener(listener: Action1<HashMap<Int, StudentEntity>>): IdentifyDialog {
        identifyResultListener = listener
        return this
    }


}