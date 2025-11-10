package com.ai.test;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.adl.auth.common.AdlAppCode;
import com.adl.auth.core.AdlAuthCallback;
import com.adl.auth.core.AdlAuthFactory;
import com.adl.base.activity.NextWriter;
import com.adl.base.log.AdlLogger;
import com.adl.service.common.IDefine;
import com.adl.service.outer.AdlService;
import com.ai.constants.MultiDemoConstants;
import com.ai.ui.init.InitActivity;
import com.qiniu.android.utils.LogUtil;
import com.ty.bus.multi.demo.R;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.constants.MMKVBusiKey;
import com.ty.tybusbase.utils.MMKVUtil;
import com.yanzhenjie.permission.AndPermission;

public class StartActivity extends BaseActivity {

    private static String testHost = "http://112.19.167.50:18005/gateway/api/device/auth";
    private static String testBusHost = "http://112.19.167.50:18082/gateway/api";

    //生产环境
    private static String host = "http://globalauth.nezhasport.com/gateway/api/device/auth";
    private static String busHost = "https://ssp.nezhasport.com/gateway/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_start);

        if (checkPermissions(Permissions)) {
            next();
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permissions)
                    .onGranted(data -> {
                        next();
                    })
                    .onDenied(data -> {
                        AdlLogger.d("--->");
                    }).start();
        }
    }
private String testUrl = "http://172.16.0.206:8082/gateway/api";
    private void next() {
        MMKVUtil.INSTANCE.encode(MMKVBusiKey.MODEL_DEBUG,true);
        MMKVUtil.INSTANCE.encode(MMKVBusiKey.SKELETON_ABLE,true);

        AdlService.getService()
                .setSdcard("zy.sport.cache")
                .setDebug(true)
                .setPlatform(IDefine.PlatformK12)
                .setQiNiuHost(testUrl)
                .setQiNiuBucket("adl-publicres")
                .setHost(testUrl)
                .setFacePrefix("https://f.nezhasport.com/")
                .build();

        AdlAuthFactory authFactory = AdlAuthFactory.create(getApplicationContext());
        authFactory.setAuthHost(testHost);
        authFactory.setBusinessHost(testBusHost);
        authFactory.setChipType(3);// 1.高通， 2.天山， 3.自动识别
        authFactory.setDebug(true);
        authFactory.setUseNewTsModel(true);
        authFactory.setDebugApp(AdlAppCode.SmartScreenApp);
        authFactory.startAuth(this, new AdlAuthCallback() {
            @Override
            public void onSucceed() {
                AdlService.getService().setDeviceToken(authFactory.getAccessToken());
                LogUtil.d("token:${authFactory.accessToken}");
                LogUtil.d("sn:${DeviceUtil.getSerialNumber()}");
                MMKVUtil.INSTANCE.encode(MultiDemoConstants.ACCESS_TOKEN, authFactory.getAccessToken());
                MMKVUtil.INSTANCE.encode(MultiDemoConstants.REGISTER_CODE, authFactory.getRegisterCode());
                MMKVUtil.INSTANCE.encode(MultiDemoConstants.PRODUCT_CODE, authFactory.getProductCode());
                MMKVUtil.INSTANCE.encode(MultiDemoConstants.APP_CODE, authFactory.getAppCode());
                NextWriter.with(StartActivity.this, InitActivity.class).closeCurrent(true).go();
            }

            @Override
            public void onSetting() {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

}
