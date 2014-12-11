package com.example.fragment0901.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.fragment0901.ESLApplication;
import com.example.fragment0901.R;
import com.example.fragment0901.utils.ESLConstants;
import com.example.fragment0901.utils.ThemeUtil;

/**
 * Created by root on 7/30/14.
 */
public class SettingActivity extends Activity {
    private CheckBox themeSelector;
    private SharedPreferences sharedPrefs;
    private boolean changed = true;
    private int selectedTheme = ThemeUtil.THEME_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);


        sharedPrefs = this.getSharedPreferences("com.example.fragment0901", Context.MODE_PRIVATE);
        int theme = sharedPrefs.getInt(ESLConstants.THEME, ThemeUtil.THEME_DEFAULT);
        boolean darkTheme = (theme != ThemeUtil.THEME_DEFAULT);
        themeSelector = (CheckBox) findViewById(R.id.theme_selector);
        themeSelector.setChecked(darkTheme);
        themeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    selectedTheme = ThemeUtil.THEME_DARK;
                } else {
                    selectedTheme = ThemeUtil.THEME_DEFAULT;
                }

                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(ESLConstants.THEME, selectedTheme);
                editor.commit();
                Log.e("ahmad", "Selected Theme ##" + sharedPrefs.getInt(ESLConstants.THEME, ThemeUtil.THEME_DEFAULT));

                if (changed) {
                    launchAppWillResetDialog();
                }
                Log.i("ahmad", "changed? " + changed);
                changed = !changed;
            }
        });

    }

    private void launchAppWillResetDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this);
        dialogBuilder.setTitle("Exit Application");
        dialogBuilder.setIcon(R.drawable.warning);
        dialogBuilder.setMessage("In order to change theme app needs to reset, proceed?").
            setNegativeButton("No" , new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // dismiss.
                }
            }).setPositiveButton("Reset app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    resetApplication();
                }
            }).show();
    }

    private void resetApplication() {
        Context context = SettingActivity.this;
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
        System.exit(2);
    }

}
