package com.example.fragment0901;

import android.app.Application;
import android.content.Context;
import android.util.AndroidRuntimeException;


public class ESLApplication extends Application {
    private static ESLApplication INSTANCE;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        INSTANCE = this;
        // Initializing instance from here.
        // This will get us access to resources if necessary.
    }

    public static ESLApplication getESLInstance(){
        if (INSTANCE == null) {
            throw new AndroidRuntimeException("ESL application was not initialized");
        }
        return INSTANCE;
    }

}
