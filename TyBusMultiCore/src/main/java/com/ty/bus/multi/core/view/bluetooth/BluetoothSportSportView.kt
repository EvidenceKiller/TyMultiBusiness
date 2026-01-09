package com.ty.bus.multi.core.view.bluetooth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.databinding.ViewBluetoothSportSportBinding

class BluetoothSportSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewBluetoothSportSportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
}