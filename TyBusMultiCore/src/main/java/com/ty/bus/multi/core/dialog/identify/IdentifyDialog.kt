package com.ty.bus.multi.core.dialog.identify

import android.content.DialogInterface
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.StudentEntity
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.DialogIdentifyBinding
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.invisible
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIFrameCountIdentityEngine
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil

class IdentifyDialog : BaseDialogFragment<DialogIdentifyBinding, IdentifyViewModel>() {

    private var identifyResultListener: Action1<HashMap<Int, StudentEntity>>? = null

    private var identifiedMap: HashMap<Int, StudentEntity>? = null

    private val identifyEngine by lazy {
        MultiIFrameCountIdentityEngine()
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
        }

        mBinding.llAdd2.setOnClickListener {
            identifyEngine.reset()
            currentIndex = 2
            identifyFlag = true
        }

        mBinding.llAdd3.setOnClickListener {
            identifyEngine.reset()
            currentIndex = 3
            identifyFlag = true
        }
        initCamera()
        initFaceDetect()
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
            }

            override fun onIdentityTimeFail() {
                LogUtil.d("单次识别超时,未识别到人脸")
            }

        })
    }

    private fun handleIdentifyOk(info: AccountInfo?) {
        identifyFlag = false
        info?.studentEntity?.let {
            identifiedMap?.put(currentIndex, it)
            mBinding.root.post {
                dispatchStudent(currentIndex, info.studentEntity)
            }
        }
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
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
            identifiedMap?.remove(index)
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
        this.identifiedMap = identifiedMap
        return this
    }

    fun setIdentifyResultListener(listener: Action1<HashMap<Int, StudentEntity>>): IdentifyDialog {
        identifyResultListener = listener
        return this
    }


}