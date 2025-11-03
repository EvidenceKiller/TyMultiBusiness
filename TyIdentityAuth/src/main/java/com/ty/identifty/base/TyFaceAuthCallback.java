package com.ty.identifty.base;

public interface TyFaceAuthCallback {

    void initLicenseSuccess();

    void initLicenseFail(int errorCode, String msg);
}
