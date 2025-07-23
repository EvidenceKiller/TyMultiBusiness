package com.ty.tybusbase.netobserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import com.ty.tybusbase.utils.NetworkUtil


/**
 * @description:
 * @author: chenwei
 * @date :   2022/11/16 09:51
 */
class NetStateChangeReceiver : BroadcastReceiver() {
    private object InstanceHolder {
        val INSTANCE: NetStateChangeReceiver = NetStateChangeReceiver()
    }

    private val mObservers: MutableList<NetStateChangeObserver> = ArrayList()

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val networkType = NetworkUtil.getNetworkType(context)
            Log.i("NetStateChangeReceiver", networkType.toString())
            notifyObservers(networkType)
        }
    }

    private fun notifyObservers(networkType: NetworkType) {
        if (networkType == NetworkType.NETWORK_NO) {
            for (observer in mObservers) {
                observer.onNetDisconnected()
            }
        } else {
            for (observer in mObservers) {
                observer.onNetConnected(networkType)
            }
        }
    }

    companion object {
        fun registerReceiver(context: Context) {
            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(InstanceHolder.INSTANCE, intentFilter)
        }

        fun unRegisterReceiver(context: Context) {
            context.unregisterReceiver(InstanceHolder.INSTANCE)
        }

        fun registerObserver(observer: NetStateChangeObserver?) {
            if (observer == null) {
                return
            }
            if (!InstanceHolder.INSTANCE.mObservers.contains(observer)) {
                InstanceHolder.INSTANCE.mObservers.add(observer)
            }
        }

        fun unRegisterObserver(observer: NetStateChangeObserver?) {
            if (observer == null) {
                return
            }
            if (InstanceHolder.INSTANCE.mObservers == null) {
                return
            }
            InstanceHolder.INSTANCE.mObservers.remove(observer)
        }
    }
}