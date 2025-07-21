package com.ty.tybusbase.netobserver;

/**
 * @description:
 * @author: chenwei
 * @date :   2022/11/16 09:57
 */
public interface NetStateChangeObserver {
    void onNetDisconnected();
    void onNetConnected(NetworkType networkType);
}
