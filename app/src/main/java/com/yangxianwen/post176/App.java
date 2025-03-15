package com.yangxianwen.post176;

import android.app.Application;

public class App extends Application {

    private static App ins;

    @Override
    public void onCreate() {
        super.onCreate();
        ins = this;
    }

    public static App getIns() {
        return ins;
    }
}
