package com.example.fragment0901.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fragment0901.ESLApplication;
import com.example.fragment0901.R;
import com.example.fragment0901.fragment.PodExpandFragment;
import com.example.fragment0901.utils.CallBacksInterface;
import com.example.fragment0901.utils.ESLConstants;
import com.example.fragment0901.utils.PodCast;
import com.example.fragment0901.utils.ThemeUtil;

public class PodListActivity extends FragmentActivity implements CallBacksInterface{

	private String tag = ((Object) this).getClass().getSimpleName();
	private TextView connectionError;
	private Button Retry;
	private static boolean twoPane;
	private FrameLayout detailFrame;
	private Bundle bundle = new Bundle();
    private static boolean DEBUG = true;
    private PodExpandFragment podcastDetail;
    private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ThemeUtil.onActivityCreateSetTheme(this);
        sharedPrefs = ESLApplication.getESLInstance().
                getSharedPreferences("com.example.fragment0901", Context.MODE_PRIVATE);

        setAppOrientation();

        if (getConnectionStatus()) {
            setContentView(R.layout.activity_pod_list);
            detailFrame = (FrameLayout) findViewById(R.id.detailFrame);
            // twoPane = (detailFrame != null && detailFrame.getVisibility() == View.VISIBLE);

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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        if (DEBUG) Log.i(tag, "@screen width int... " + width);
        if (DEBUG) Log.i(tag, " @screen height int... " + height);
		Resources resources = this.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float smallestWidthDp = Math.min(width, height) / (metrics.densityDpi / 160f);
        if (DEBUG) Log.i(tag, "min @screen width in dp... " + smallestWidthDp);

		if (smallestWidthDp >= ESLConstants.LARGE_SCREEN_WIDTH_600DP) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			twoPane = true;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			twoPane = false;
		}
        if (DEBUG) Log.i(tag, "@screen Done setting orientation... twoPane: " + twoPane);
	}

	public static boolean getTwoPane() {
		return twoPane;
	}

	@Override
	public void onItemSelected(PodCast podcast) {
        if (DEBUG) Log.i(tag, "onItemSelected called");
        boolean samePodCast = false;
        if (bundle != null && !TextUtils.isEmpty(podcast.getTitle()) &&
                podcast.getTitle().equalsIgnoreCase(bundle.getString(ESLConstants.TITLE_KEY))) {
			samePodCast = true;
		}

        if (twoPane){
            if (!samePodCast || PodExpandFragment.isDestroyed()) {
                assert bundle != null;
                bundle.putString(ESLConstants.TITLE_KEY, podcast.getTitle());
                bundle.putString(ESLConstants.LINK_KEY, podcast.getLink());
                bundle.putString(ESLConstants.SHARE_LINK_KEY, podcast.getshareLink());
                bundle.putString(ESLConstants.SUMMARY_KEY, podcast.getSummary());
                bundle.putString(ESLConstants.TIME_KEY, podcast.getDuration());
                bundle.putString(ESLConstants.DATE_KEY, podcast.getDate());

                if (getFragmentManager().findFragmentByTag(ESLConstants.EXPAND_FRAGMENT) != null) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(ESLConstants.EXPAND_FRAGMENT));
                    fragmentTransaction.commit();
                }
                podcastDetail = new PodExpandFragment();
                podcastDetail.setArguments(bundle);

                if (DEBUG) Log.i(tag, bundle.getString("title") + " twopane ## selected Item");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.detailFrame, podcastDetail, ESLConstants.EXPAND_FRAGMENT);
                ft.addToBackStack(null);
                ft.commit();
            }
        } else {
            if (!samePodCast && PodExpandActivity.expandActivity != null){
                PodExpandActivity.expandActivity.finish();
            }
            assert bundle != null;
            bundle.putString(ESLConstants.TITLE_KEY, podcast.getTitle());
            bundle.putString(ESLConstants.LINK_KEY, podcast.getLink());
            bundle.putString(ESLConstants.SHARE_LINK_KEY, podcast.getshareLink());
            bundle.putString(ESLConstants.SUMMARY_KEY, podcast.getSummary());
            bundle.putString(ESLConstants.TIME_KEY, podcast.getDuration());
            bundle.putString(ESLConstants.DATE_KEY, podcast.getDate());

            Intent mIntent = new Intent(this, PodExpandActivity.class);
            if (DEBUG) Log.i(tag, bundle.getString("title") + " intent ## selected Item");
            mIntent.putExtra("selectedItem", bundle);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mIntent);
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

    public static boolean loggingEnabled() {
        return DEBUG;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (podcastDetail != null && !podcastDetail.isDestroyed()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP ) {
                podcastDetail.volumeChanged();
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pod_list, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.twitter:
                String twUrl =ESLConstants.TWITTER_URL;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(twUrl)));
                return true;
            case R.id.facebook:
                String fbUrl =ESLConstants.FB_URL;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fbUrl)));
                return true;
            case R.id.rateApp:
                String gpUrl =ESLConstants.GP_URL;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(gpUrl)));
                return true;
            case R.id.email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",ESLConstants.EMAIL, null));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;
        default:
                return super.onOptionsItemSelected(item);
        }
    }
}
