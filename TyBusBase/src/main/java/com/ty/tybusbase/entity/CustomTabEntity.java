package com.ty.tybusbase.entity;


public class CustomTabEntity {
    public String title;

    public int selectIcon;
    public int unSelectIcon;

    public String selectUrl;
    public String unSelectUrl;

    public String actionCategoryUid;
    public CustomTabEntity(String title, int selectIcon, int unSelectIcon) {
        this.title = title;
        this.selectIcon = selectIcon;
        this.unSelectIcon = unSelectIcon;
    }

    public CustomTabEntity(String title, int selectIcon, int unSelectIcon, String actionCategoryUid) {
        this.title = title;
        this.selectIcon = selectIcon;
        this.unSelectIcon = unSelectIcon;
        this.actionCategoryUid = actionCategoryUid;
    }
    public CustomTabEntity(String title) {
        this.title = title;

    }
    public CustomTabEntity(String title, String selectUrl, String unSelectUrl) {
        this.title = title;
        this.selectUrl = selectUrl;
        this.unSelectUrl = unSelectUrl;
    }

    public CustomTabEntity(String title, String selectUrl, String unSelectUrl, String actionCategoryUid) {
        this.title = title;
        this.selectUrl = selectUrl;
        this.unSelectUrl = unSelectUrl;
        this.actionCategoryUid = actionCategoryUid;
    }
}