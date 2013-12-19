package com.example.fragment0901.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.fragment0901.R;
import com.example.fragment0901.utils.ESLConstants;

public class PodExpandActivity extends FragmentActivity  {
	private String title;
	private String tag = ((Object) this).getClass().getSimpleName();
    private Context context;
    private boolean DEBUG = PodListActivity.loggingEnabled();

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		getAppOrientation();
		super.onCreate(arg0);
        context = getApplicationContext();
		setContentView(R.layout.activity_pod_expand);
		Intent intent = this.getIntent();
		Bundle extras = intent.getBundleExtra("selectedItem");
		PodExpandFragment expandFragment = new PodExpandFragment();
		expandFragment.setArguments(extras);

        if (getFragmentManager().findFragmentByTag(ESLConstants.EXPAND_FRAGMENT) != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(ESLConstants.EXPAND_FRAGMENT));
            fragmentTransaction.commit();
        }

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.expandContainer, expandFragment , ESLConstants.EXPAND_FRAGMENT);
        transaction.addToBackStack(null);
		transaction.commit();
	}

	public void getAppOrientation() {
		if (PodListActivity.getTwoPane()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

}
