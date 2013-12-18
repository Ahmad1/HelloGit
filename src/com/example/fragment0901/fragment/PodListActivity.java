package com.example.fragment0901.fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fragment0901.R;
import com.example.fragment0901.utils.CallBacksInterface;
import com.example.fragment0901.utils.PodCast;

public class PodListActivity extends FragmentActivity implements CallBacksInterface {
	private static final int LARGE_SCREEN_WIDTH_600DP = 600;
	private String tag = ((Object) this).getClass().getSimpleName();
	private TextView connectionError;
	private Button Retry;
	private static boolean twoPane;
	private FrameLayout detailFrame;
	private Bundle bundle = new Bundle();
    private static boolean DEBUG = true;

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
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Resources resources = this.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float smallestWidthDp = Math.min(width, height) / (metrics.densityDpi / 160f);
        if (DEBUG) Log.i(tag, "min screen width in dp... " + smallestWidthDp);

		if (smallestWidthDp > LARGE_SCREEN_WIDTH_600DP) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			twoPane = true;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			twoPane = false;
		}
	}

	public static boolean getTwoPane() {
		return twoPane;
	}

	@Override
	public void onItemSelected(PodCast podcast) {
        if (DEBUG) Log.i(tag, "onItemSelected called");
		boolean samePodCast = false;
		if (bundle != null && bundle.getString("title") == podcast.getTitle()) {
			samePodCast = true;
		}
		if (!samePodCast) {
			bundle.putString("title", podcast.getTitle());
			bundle.putString("link", podcast.getLink());
			bundle.putString("summary", podcast.getSummary());
			bundle.putString("time", podcast.getDuration());
			bundle.putString("date", podcast.getDate());

			if (twoPane) {
				getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				Fragment podcastDetail = new PodExpandFragment();
				podcastDetail.setArguments(bundle);
                if (DEBUG) Log.i(tag, bundle.getString("title") + " twopane ## selected Item");
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.detailFrame, podcastDetail);
				ft.addToBackStack(null);
				ft.commit();
			} else {
				Intent mIntent = new Intent(this, PodExpandActivity.class);
                if (DEBUG) Log.i(tag, bundle.getString("title") + " intent ## selected Item");
				mIntent.putExtra("selectedItem", bundle);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mIntent);
			}
		} else {
            // if it is samePodcast && isPlaying --> just bring it to front
            finish();
		}
	}

	private boolean getConnectionStatus() {
		boolean found = false;
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(
				Context.CONNECTIVITY_SERVICE);
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

    public static boolean loggingEnabled() {
        return DEBUG;
    }
}
