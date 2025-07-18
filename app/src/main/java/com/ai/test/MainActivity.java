package com.ai.test;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.adl.base.speech.AdlTextSpeaker;
import com.ty.bus.multi.core.UINavConfig;
import com.ty.bus.multi.core.UINavigation;
import com.ty.bus.multi.demo.R;

public class MainActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);

        AdlTextSpeaker.init(this);


        findViewById(R.id.bnt_0).setOnClickListener(view -> {
            UINavigation.goFiveSkipRope(MainActivity.this, new UINavConfig());
        });

        findViewById(R.id.bnt_11).setOnClickListener(view -> {
            UINavigation.goTenSkipRope(MainActivity.this, new UINavConfig());
        });

        findViewById(R.id.bnt_10).setOnClickListener(view -> {
            UINavigation.goThreeSitReach(MainActivity.this, new UINavConfig());
        });
    }
}
