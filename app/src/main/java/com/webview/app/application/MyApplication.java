package com.webview.app.application;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化极光推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
