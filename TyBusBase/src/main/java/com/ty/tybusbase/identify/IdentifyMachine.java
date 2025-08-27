package com.ty.tybusbase.identify;

import com.adl.lib.pose.AdlPoseSkeleton;
import com.ty.tybusbase.entity.AccountInfo;

/**
 * @author :WEI_CP
 * Time:2022/3/2 15:43
 * Description: this is IdentifyBase
 * 身份识别基类
 */
public interface IdentifyMachine {

    void openMachine(IdentifyMachineListener listener);
    /**
     * 比赛模式
     * @param listener
     */
    void openMachineByMatchModel(IdentifyMachineListener listener, AccountInfo accountInfo);
    void identify(AdlPoseSkeleton aiResponse, byte[] data, int w, int h,int type);
    void identify(AdlPoseSkeleton aiResponse, byte[] data, int w, int h,boolean isLeft);

    void closeMachine();

    void setVisibility(int visible);

    void restUI();

    void find(String sportLocationLeft);
}
