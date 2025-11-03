package com.ty.identifty.base;

public class TyLivingModel {

    private TyFaceInfo faceInfo;
    private TyFaceInfo[] trackFaceInfo;

    public TyFaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(TyFaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public TyFaceInfo[] getTrackFaceInfo() {
        return trackFaceInfo;
    }

    public void setTrackFaceInfo(TyFaceInfo[] trackFaceInfo) {
        this.trackFaceInfo = trackFaceInfo;
    }
}
