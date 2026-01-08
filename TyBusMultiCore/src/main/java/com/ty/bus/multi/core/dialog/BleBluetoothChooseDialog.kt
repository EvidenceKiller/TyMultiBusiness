package com.ty.bus.multi.core.dialog

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.adl.base.common.AdlToast
import com.ty.bus.multi.core.bluetooth.BluetoothDiscover
import com.ty.bus.multi.databinding.DialogChooseBluetoothBinding
import com.ty.bus.multi.databinding.ItemChooseBluetoothBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.expand.Action1
import com.zy.callback.OnScanCallback
import com.zy.clazz.MultiDeviceHelper

class BleBluetoothChooseDialog : BaseDialogFragment<DialogChooseBluetoothBinding, BaseViewModel>() {
    private var onBlueChooseListener: Action1<String>? = null
    private val mHelper by lazy { MultiDeviceHelper(context) }
    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()
    private var filterDeviceList: MutableList<String>? = null
    override fun initViewObservable() {

    }

    override fun initData() {

    }

    override fun initView() {
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
        mBinding.ivClose.setOnClickListener { dismiss() }
        initRv()
        scanBluetooth()
    }

    fun addFilterDevice(device: String?): BleBluetoothChooseDialog {
        if (device.isNullOrEmpty()) return this
        if (filterDeviceList == null) {
            filterDeviceList = mutableListOf()
        }
        filterDeviceList?.add(device)
        return this
    }

    @SuppressLint("MissingPermission")
    private fun initRv() {
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.adapter =
            object : BaseBindingAdapter<ItemChooseBluetoothBinding, BluetoothDevice>(deviceList) {
                override fun convert(
                    holder: VBViewHolder<ItemChooseBluetoothBinding>,
                    item: BluetoothDevice
                ) {
                    holder.vb.apply {
                        tvDeviceName.text = item.name
                        tvMacAddress.text = item.address
                        root.setOnClickListener { onItemCLick(item) }
                    }
                }
            }
    }

    private fun scanBluetooth() {
        //  默认已连接蓝牙
        mHelper.scanBleDevice(object : OnScanCallback {
            override fun onFinish() {
                AdlToast.show("扫描完毕")
            }

            override fun onScanning(device: BluetoothDevice?, rssi: Int) {
                if (filterDeviceList?.contains(device?.address) == true) {
                    return
                }
                device?.let {
                    if (!deviceList.contains(it)) {
                        deviceList.add(it)
                        mBinding.recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }
        })

//        val targetDevice = BluetoothDiscover.getInstance().targetDevice
//        if (targetDevice != null) {
//            deviceList.add(targetDevice)
//            mBinding.recyclerView.adapter?.notifyDataSetChanged()
//        }
    }

    fun onItemCLick(device: BluetoothDevice) {
        onBlueChooseListener?.invoke(device.address)
        dismiss()
    }

    fun setOnBlueChooseListener(onClickListener: Action1<String>): BleBluetoothChooseDialog {
        this.onBlueChooseListener = onClickListener
        return this
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mHelper.stopScan()
        mHelper.release()
    }
}