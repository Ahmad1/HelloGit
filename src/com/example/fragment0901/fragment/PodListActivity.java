package com.example.fragment0901.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fragment0901.R;
import com.example.fragment0901.adapter.PodCast;

public class PodListActivity extends FragmentActivity implements PodListFragment.CallBacks {
	private String tag = this.getClass().getSimpleName();
	private TextView connectionError;
	private Button Retry;
	private boolean twoPane;
	private FrameLayout detailFrame;
	private Bundle bundle = new Bundle();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppOrientation();
		if (getConnectionStatus()) {
			setContentView(R.layout.activity_pod_list);
			detailFrame = (FrameLayout) findViewById(R.id.detailFrame);
			twoPane = (detailFrame != null && detailFrame.getVisibility() == View.VISIBLE);
			
		} else {
			setContentView(R.layout.noconnection);
			connectionError = (TextView) findViewById(R.id.connection_error);
			Retry = (Button) findViewById(R.id.retry);
			Retry.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
			});
		}
	}

	public void setAppOrientation() {
		if (Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	public void onItemSelected(PodCast podcast) {
		bundle.putString("title", podcast.getTitle());
		bundle.putString("link", podcast.getLink());
		bundle.putString("summary", podcast.getSummary());
		bundle.putString("time", podcast.getDuration());
		bundle.putString("date", podcast.getDate());

		if (twoPane) {
			Fragment podcastDetail = new PodExpandFragment();
			podcastDetail.setArguments(bundle);
			Log.i(tag, bundle.getString("title") + " twopane ## selected Item");
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.detailFrame, podcastDetail);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			Intent mIntent = new Intent(PodListActivity.this,
					PodExpandActivity.class);
			Log.i(tag, bundle.getString("title") + " intent ## selected Item");
			mIntent.putExtra("selectedItem", bundle);
			// mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mIntent);
		}

	}
	
	private boolean getConnectionStatus() {
		boolean found = false;
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			found = true;
		}
		return found;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pod_list, menu);
		return true;
	}

}
