package com.ty.bus.multi.core.dialog

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.SettingDialogHttpCameraSettingBinding
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil

/**
 * @description: 调试模式dialog
 * @author: zhou
 * @date :   2023/7/14 09:25
 */
class HttpCameraSettingDialog :
    BaseDialogFragment<SettingDialogHttpCameraSettingBinding, BaseViewModel>(),OnClickListener {
    private var openEye = false
    private var ipIsEmpty = false
    private var accountIsEmpty = false
    private var pswIsEmpty = false
    private var thresholdIsEmpty = false

    private lateinit var preRbRecordChecked: RadioButton

    private lateinit var preTvRecordChecked: TextView

    override fun initData() {
        isCancelable = false
        mBinding.etIp.setText(MMKVUtil.decodeString(MMKVBusiKey.HTTP_CAMERA_IP,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP))
        mBinding.etAccount.setText(MMKVUtil.decodeString(MMKVBusiKey.HTTP_CAMERA_ACCOUNT, BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT))
        mBinding.etPsw.setText(MMKVUtil.decodeString(MMKVBusiKey.HTTP_CAMERA_PSW, BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD))
        mBinding.etThreshold.setText(MMKVUtil.decodeString(MMKVBusiKey.MULTI_ROPE_SKIP_THRESHOLD, "8"))
        preRbRecordChecked = mBinding.rbRecordDefault
        preTvRecordChecked = mBinding.tvRecordDefault
        textChanged()
    }

    override fun initViewObservable() {

        mBinding.debugDialogClose.setOnClickListener(this)
        mBinding.debugDialogCancel.setOnClickListener(this)
        mBinding.debugDialogConfirm.setOnClickListener(this)
        mBinding.debugDialogEye.setOnClickListener(this)

        //初始化录制选择
        mBinding.rgRecordBtn.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_record_default -> {
                    preRbRecordChecked.isChecked = false
                    updateTvColor(preTvRecordChecked, false)
                    mBinding.rbRecordDefault.isChecked = true
                    updateTvColor(mBinding.tvRecordDefault, true)
                    preRbRecordChecked = mBinding.rbRecordDefault
                    preTvRecordChecked = mBinding.tvRecordDefault
                    MMKVUtil.encode(
                        MMKVBusiKey.HTTP_CAMERA_RECORD_MODEL,
                        BusiBaseConstantsValue.HTTP_CAMERA_RECORD_MODEL_DEFAULT
                    )
                }

                R.id.rb_record_ipc -> {
                    mBinding.rbRecordIpc.isChecked = true
                    updateTvColor(mBinding.tvRecordIpc, true)
                    preRbRecordChecked.isChecked = false
                    updateTvColor(preTvRecordChecked, false)
                    preRbRecordChecked = mBinding.rbRecordIpc
                    preTvRecordChecked = mBinding.tvRecordIpc
                    MMKVUtil.encode(
                        MMKVBusiKey.HTTP_CAMERA_RECORD_MODEL,
                        BusiBaseConstantsValue.HTTP_CAMERA_RECORD_MODEL_IPC
                    )
                }
            }
            if (!ipIsEmpty && !accountIsEmpty && !pswIsEmpty) {
                enableConfirm()
            }
        }
        val recordModel: Int = MMKVUtil.decodeInt(
            MMKVBusiKey.HTTP_CAMERA_RECORD_MODEL,
            BusiBaseConstantsValue.HTTP_CAMERA_RECORD_DEFAULT_MODEL
        )
        if (recordModel == BusiBaseConstantsValue.HTTP_CAMERA_RECORD_MODEL_DEFAULT) {
            mBinding.rbRecordDefault.isChecked = true
        } else {
            mBinding.rbRecordIpc.isChecked = true
        }
        val compensationProgress: Int = MMKVUtil.decodeInt(MMKVBusiKey.MULTI_ROPE_SKIP_COMPENSATION, 0)
        mBinding.skCompensationBar.progress = compensationProgress
        mBinding.skCompensationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                MMKVUtil.encode(MMKVBusiKey.MULTI_ROPE_SKIP_COMPENSATION, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun textChanged() {
        mBinding.etPsw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                pswIsEmpty = TextUtils.isEmpty(s)
                if (!ipIsEmpty && !accountIsEmpty && !pswIsEmpty && !thresholdIsEmpty) {
                    enableConfirm()
                } else {
                    disableConfirm()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.etThreshold.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                thresholdIsEmpty = TextUtils.isEmpty(s)
                if (!ipIsEmpty && !accountIsEmpty && !pswIsEmpty && !thresholdIsEmpty) {
                    enableConfirm()
                } else {
                    disableConfirm()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.etIp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ipIsEmpty = TextUtils.isEmpty(s)
                if (!ipIsEmpty && !accountIsEmpty && !pswIsEmpty && !thresholdIsEmpty) {
                    enableConfirm()
                } else {
                    disableConfirm()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.etAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                accountIsEmpty = TextUtils.isEmpty(s)
                if (!ipIsEmpty && !accountIsEmpty && !pswIsEmpty && !thresholdIsEmpty) {
                    enableConfirm()
                } else {
                    disableConfirm()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun enableConfirm() {
        mBinding.debugDialogConfirm.isEnabled = true
        mBinding.debugDialogConfirm.setBackgroundResource(R.drawable.bg_round_r50_gradient_red)
        mBinding.debugDialogConfirm.setTextColor(resources.getColor(R.color.white, null))
        mBinding.debugDialogConfirm.isClickable = true
    }

    private fun disableConfirm() {
        mBinding.debugDialogConfirm.isEnabled = false
        mBinding.debugDialogConfirm.setBackgroundResource(R.drawable.login_edit_btn_normal_bg)
        mBinding.debugDialogConfirm.setTextColor(resources.getColor(R.color.white_50, null))
        mBinding.debugDialogConfirm.isClickable = false
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.debug_dialog_confirm -> {
                hideKeyBroad()
                //保存密码
                MMKVUtil.encode(MMKVBusiKey.HTTP_CAMERA_IP, mBinding.etIp.text.toString().trim())
                MMKVUtil.encode(
                    MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
                    mBinding.etAccount.text.toString().trim()
                )
                MMKVUtil.encode(MMKVBusiKey.HTTP_CAMERA_PSW, mBinding.etPsw.text.toString().trim())
                MMKVUtil.encode(
                    MMKVBusiKey.MULTI_ROPE_SKIP_THRESHOLD,
                    mBinding.etThreshold.text.toString().trim()
                )
                dismiss()
            }

            R.id.debug_dialog_cancel, R.id.debug_dialog_close -> {
                hideKeyBroad()
                dismiss()
            }

            R.id.debug_dialog_eye -> {
                if (openEye) {
                    mBinding.debugDialogEye.setImageResource(R.mipmap.eye_close)
                    mBinding.etPsw.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                } else {
                    mBinding.debugDialogEye.setImageResource(R.mipmap.eye_open)
                    mBinding.etPsw.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }
                openEye = !openEye
                mBinding.etPsw.setSelection(mBinding.etPsw.length())
            }

            else -> {}
        }
    }

    private fun hideKeyBroad() {
        val imm = mBinding.etPsw.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mBinding.etPsw.windowToken, 0)
    }

    private fun updateTvColor(tv: TextView, select: Boolean) {
        tv.setTextColor(if (select) Color.WHITE else Color.parseColor("#88ffffff"))
    }
}