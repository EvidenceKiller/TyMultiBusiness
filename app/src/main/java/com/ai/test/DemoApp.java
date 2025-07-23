package com.ai.test;

import com.adl.base.AdlApp;
import com.tencent.mmkv.MMKV;
import com.ty.tybusbase.utils.ApplicationUtil;
import com.ty.tybusbase.utils.MMKVUtil;

public class DemoApp extends AdlApp {

    @Override
    public void onCreate() {
        super.onCreate();
        loadXfSpeech();
        //ZySportContext.init(this);
        SoundUtil.getInstance().init(this);
        ApplicationUtil.INSTANCE.init(this);
        MMKV.initialize(this);
    }
}
