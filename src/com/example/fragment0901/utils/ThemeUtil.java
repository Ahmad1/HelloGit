package com.example.fragment0901.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fragment0901.ESLApplication;
import com.example.fragment0901.R;

public class ThemeUtil {
    public final static int THEME_DEFAULT = 0;
    public final static int THEME_DARK = 1;
    private static int sTheme = ESLApplication.getESLInstance().getSharedPreferences("com.example.fragment0901",
            Context.MODE_PRIVATE).getInt(ESLConstants.THEME, THEME_DEFAULT);

    public static void changeToTheme(Activity activity, int theme) {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void onActivityCreateSetTheme(Activity activity) {
        SharedPreferences pref = ESLApplication.getESLInstance().
                getSharedPreferences("com.example.fragment0901", Context.MODE_PRIVATE);

        if ( pref != null ) {
            sTheme = pref.getInt(ESLConstants.THEME, THEME_DEFAULT);
        }
        Log.i("xxx" , "sTheme: " + sTheme);
        switch (sTheme) {
            default:
            case THEME_DEFAULT:
                activity.setTheme(R.style.Theme_ESL_List);
                break;
            case THEME_DARK:
                activity.setTheme(R.style.Theme_ESL_List_dark);
                break;
        }
    }
}