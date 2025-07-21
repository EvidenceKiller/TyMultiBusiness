package com.ty.tybusbase.dialog

import android.content.Context
import com.ty.tybusbase.base.BaseDialog
import com.ty.tybusbase.databinding.DialogLoadingBinding


class LoadingDialog(context: Context) : BaseDialog<DialogLoadingBinding>(context) {

    override fun initView() {
        mBinding.run {

        }
    }

    fun updateLoadingText(text: String) {
        mBinding.tvLoadingText.text = text
    }


}