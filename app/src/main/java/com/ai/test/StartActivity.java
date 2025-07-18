package com.ai.test;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.adl.auth.common.AdlAppCode;
import com.adl.auth.core.AdlAuthCallback;
import com.adl.auth.core.AdlAuthFactory;
import com.adl.base.activity.NextWriter;
import com.adl.base.log.AdlLogger;
import com.adl.ts.general.R;
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

    private void next() {

        AdlAuthFactory authFactory = AdlAuthFactory.create(getApplicationContext());
        authFactory.setAuthHost(testHost);
        authFactory.setBusinessHost(testBusHost);
        authFactory.setChipType(3);// 1.高通， 2.天山， 3.自动识别
        authFactory.setDebug(true);
        authFactory.setDebugApp(AdlAppCode.SmartScreenApp);

        authFactory.startAuth(this, new AdlAuthCallback() {
            @Override
            public void onSucceed() {
                NextWriter.with(StartActivity.this, MainActivity.class).closeCurrent(true).go();
            }

            @Override
            public void onSetting() {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

}
