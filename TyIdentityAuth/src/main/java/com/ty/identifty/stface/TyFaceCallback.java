package com.ty.identifty.stface;

/**
 * 人脸识别回调
 */
public interface TyFaceCallback {


    void onFaceFast(TyLivingModel model);

    void onFaceInfo(TyLivingModel model);


}
