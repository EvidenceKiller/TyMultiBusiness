package com.ai.test;

import com.adl.base.AdlApp;

public class DemoApp extends AdlApp {

    @Override
    public void onCreate() {
        super.onCreate();
        loadXfSpeech();
        //ZySportContext.init(this);
        SoundUtil.getInstance().init(this);
    }
}
