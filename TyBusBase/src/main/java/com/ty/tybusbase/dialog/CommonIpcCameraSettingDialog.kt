package com.ty.tybusbase.dialog

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.databinding.BusiBaseDialogHttpCameraSettingBinding
import com.ty.tybusbase.entity.IpcCameraConfig

/**
 * @description: 调试模式dialog
 * @author: zhou
 * @date :   2023/7/14 09:25
 */
class CommonIpcCameraSettingDialog(val saveKey: String) :
    BaseDialogFragment<BusiBaseDialogHttpCameraSettingBinding, BaseViewModel>(), OnClickListener {
    private var openEye = false
    private var ipIsEmpty = false
    private var accountIsEmpty = false
    private var pswIsEmpty = false
    private var thresholdIsEmpty = false

    override fun initData() {
        isCancelable = false
        val config = BusiBaseConfig.readIpcCameraConfig(saveKey)
        mBinding.etIp.setText(config.ip)
        mBinding.etAccount.setText(config.account)
        mBinding.etPsw.setText(config.psw)
        textChanged()
    }

    override fun initViewObservable() {

        mBinding.debugDialogClose.setOnClickListener(this)
        mBinding.debugDialogCancel.setOnClickListener(this)
        mBinding.debugDialogConfirm.setOnClickListener(this)
        mBinding.debugDialogEye.setOnClickListener(this)

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
        mBinding.debugDialogConfirm.setBackgroundResource(R.drawable.busi_base_login_edit_btn_normal_bg)
        mBinding.debugDialogConfirm.setTextColor(resources.getColor(R.color.white_50, null))
        mBinding.debugDialogConfirm.isClickable = false
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.debug_dialog_confirm -> {
                hideKeyBroad()
                //保存IPC配置
                BusiBaseConfig.saveIpcCameraConfig(
                    saveKey, IpcCameraConfig(
                        mBinding.etIp.text.toString().trim(),
                        mBinding.etAccount.text.toString().trim(),
                        mBinding.etPsw.text.toString().trim()
                    )
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
                    mBinding.etPsw.inputType =
                        InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
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

}