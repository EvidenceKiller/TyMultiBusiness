package com.ty.tybusbase.dialog

import android.content.DialogInterface
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.DialogCommonSingleBtnTipBinding
import com.ty.tybusbase.databinding.DialogCommonTipBinding

class CommonSingleBtnTipDialog :
    BaseDialogFragment<DialogCommonSingleBtnTipBinding, BaseViewModel>() {

    private var content: String? = null
    private var dismissCallback: Action? = null

    fun setContent(content: String): CommonSingleBtnTipDialog {
        this.content = content
        return this
    }

    fun setDismissCallback(callback: Action): CommonSingleBtnTipDialog {
        this.dismissCallback = callback
        return this
    }

    override fun initViewObservable() {
        mBinding.tvConfirm.setOnClickListener {
            dismiss()
        }
    }

    override fun initData() {
        content?.let {
            mBinding.tvContent.text = it
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallback?.invoke()
    }


}