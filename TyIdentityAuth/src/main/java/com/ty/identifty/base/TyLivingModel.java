package com.ty.identifty.base;

public class TyLivingModel {

    private TyFaceInfo faceInfo;
    private TyFaceInfo[] trackFaceInfo;
    private FailType failType = FailType.NONE;
    private String msg;

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

    public FailType getFailType() {
        return failType;
    }

    public void setFailType(FailType failType) {
        this.failType = failType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
