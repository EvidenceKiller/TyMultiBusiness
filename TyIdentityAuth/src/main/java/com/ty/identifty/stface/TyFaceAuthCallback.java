package com.ty.identifty.stface;

public interface TyFaceAuthCallback {

    void initLicenseSuccess();

    void initLicenseFail(int errorCode, String msg);
}
