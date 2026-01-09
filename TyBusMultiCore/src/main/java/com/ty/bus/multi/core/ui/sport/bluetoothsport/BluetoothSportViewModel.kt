package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.tybusbase.base.BaseSportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class BluetoothSportViewModel(application: Application) : BaseSportViewModel(application) {

    //运动卡片的状态
//    var sportCardStateLD = MutableLiveData<Pair<Int, ForwardFlexionCardState>>()
    var sportCardState =
        MutableStateFlow<Pair<Int, BluetoothSportCardState>>(Pair(0, BluetoothSportCardState.IDLE))

    fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            SystemClock.sleep(2000)
            sportCardState.emit(Pair(1, BluetoothSportCardState.CONNECTED))
        }
    }
}