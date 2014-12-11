package com.example.fragment0901.utils;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.banner.AdView;
import com.example.fragment0901.activities.PodListActivity;

public class MobFoxAdLoad implements AdListener{
    private Activity mActivity;
    private LinearLayout container;
    private AdView adView;
    private boolean DEBUG = PodListActivity.loggingEnabled();
    private String tag = this.getClass().getSimpleName();

    public MobFoxAdLoad(Activity mActivity, LinearLayout container) {
        this.mActivity = mActivity;
        this.container = container;
        loadMobFoxAd();
    }

    private void loadMobFoxAd(){
        container.removeAllViews();
        adView = null;
        adView = new AdView(mActivity, "http://my.mobfox.com/request.php",ESLConstants.PUBLISHER_ID, true, true);
        adView.setAdspaceWidth(320);
        // Optional, used to set the custom size of banner placement. Without setting it,
        // the SDK will use default size of 320x50 or 300x50 depending on device type.
        adView.setAdspaceHeight(50);
        adView.setAdspaceStrict(false);
        // Optional, tells the server to only supply banner ads that are exactly of the desired size.
        // Without setting it, the server could also supply smaller Ads when no ad of desired size is available.
        adView.setAdListener(this);
        container.addView(adView);
    }

    public void releaseAd(){
        if (adView!= null) adView.release();
    }

    // MobFox Ad Listener
    @Override
    public void adClicked() {
        if (DEBUG) Log.i(tag, "######adListener, adClicked");
    }

    @Override
    public void adClosed(Ad ad, boolean b) {
        if (DEBUG) Log.i(tag, "######adListener, adClosed");
    }

    @Override
    public void adLoadSucceeded(Ad ad) {
        container.setVisibility(View.VISIBLE);
        if (DEBUG) Log.i(tag, "######adListener, adLoadSucceeded");
    }

    @Override
    public void adShown(Ad ad, boolean b) {
        if (DEBUG) Log.i(tag, "######adListener, adShown");
    }

    @Override
    public void noAdFound() {
        if (DEBUG) Log.i(tag, "######adListener, noAdFound");
        container.setVisibility(View.GONE);
        adView.release();
    }

}
