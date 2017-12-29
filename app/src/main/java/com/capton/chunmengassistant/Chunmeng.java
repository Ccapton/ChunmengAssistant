package com.capton.chunmengassistant;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by capton on 2017/6/20.
 */

public class Chunmeng extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"="+getString(R.string.xf_appid));
    }



}
