package com.ai.ui.nfc

import android.nfc.NfcManager
import android.os.Bundle
import com.ty.bus.multi.demo.databinding.ActivityNfcReaderBinding
import com.ty.nfc.reader.base.NfcType
import com.ty.nfc.reader.base.ReadCallback
import com.ty.nfc.reader.manager.NfcReaderManager
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.base.BaseViewModel

class NfcReaderActivity : BaseActivity<ActivityNfcReaderBinding, BaseViewModel>() {
    private var nfcType = NfcType.NFC_A
    private val nfcManager by lazy { NfcReaderManager() }
    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.btnType1.setOnClickListener {
            nfcType = NfcType.NFC_A
        }
        mBinding.btnType2.setOnClickListener {
            nfcType = NfcType.NFC_B
        }
        mBinding.btnInit.setOnClickListener {
            nfcManager.init(this, nfcType)
        }
        mBinding.btnRead.setOnClickListener {
            nfcManager.startRead(object : ReadCallback {
                override fun readCardBack(cardStr: String?) {
                    runOnUiThread {
                        mBinding.tvCardNum.append("${cardStr}\n")
                    }
                }

            })
        }
        mBinding.btnStop.setOnClickListener {
            nfcManager.stopRead()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nfcManager.release()
    }
}