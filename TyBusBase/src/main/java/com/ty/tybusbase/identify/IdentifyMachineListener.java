package com.ty.tybusbase.identify;

import com.ty.tybusbase.entity.AccountInfo;

/**
 * @author :WEI_CP
 * Time:2022/3/7 9:24
 * Description: this is IdentifyMachineListener
 */
public interface IdentifyMachineListener {
    void onIdentityMachineInit(boolean initResult);

    void onIdentityTimeOn();

    void onIdentityOk(AccountInfo accountInfo);

    void onIdentityTimeFail();
}
