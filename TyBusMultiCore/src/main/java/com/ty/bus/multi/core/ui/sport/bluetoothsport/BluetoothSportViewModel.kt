package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import com.ty.bus.multi.core.factory.bluetoothsport.AbstractBluetoothSportFactory
import com.ty.bus.multi.core.factory.bluetoothsport.BluetoothSportProduceFactory
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.tybusbase.base.BaseSportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class BluetoothSportViewModel(application: Application) : BaseSportViewModel(application) {

    var sportCardState =
        MutableSharedFlow<Pair<Int, BluetoothSportCardState>>()
    var factory: AbstractBluetoothSportFactory? = null
    fun initData() {
        factory = BluetoothSportProduceFactory.getFactory(sportInfo?.sportSkuId)
        viewModelScope.launch(Dispatchers.IO) {
            SystemClock.sleep(1000)
            sportCardState.emit(Pair(1, BluetoothSportCardState.CONNECTED))
            sportCardState.emit(Pair(2, BluetoothSportCardState.CONNECTED))
            sportCardState.emit(Pair(3, BluetoothSportCardState.CONNECTED))
        }
    }
}