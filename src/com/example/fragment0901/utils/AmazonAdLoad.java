package com.example.fragment0901.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;
import com.example.fragment0901.activities.PodListActivity;


public class AmazonAdLoad implements AdListener{
    private Activity mActivity;
    private LinearLayout container;
    private AdLayout amazonAdView;
    private boolean DEBUG = PodListActivity.loggingEnabled();
    private String tag = this.getClass().getSimpleName();
    private MobFoxAdLoad mobFoxAd;

    public AmazonAdLoad(Activity activity, LinearLayout container) {
        this.mActivity = activity;
        this.container = container;
        loadAmazonAd();
    }

    private void loadAmazonAd(){
        AdRegistration.enableLogging(true);
        AdRegistration.enableTesting(true);
        AdRegistration.setAppKey(ESLConstants.AMAZON_APP_ID);
        // Initialize ad view
        amazonAdView = new AdLayout(mActivity, com.amazon.device.ads.AdSize.SIZE_320x50);
        amazonAdView.setTimeout(15000);
        AdTargetingOptions options = new AdTargetingOptions();
        options.setAge(30);
        amazonAdView.setListener(this);
        container.addView(amazonAdView);
        amazonAdView.loadAd(options);
    }

    public void releaseAd(){
        if (amazonAdView!= null) amazonAdView.destroy();
        if (mobFoxAd!= null) mobFoxAd.releaseAd();
    }
    // AMAZON Ad Listener
    @Override
    public void onAdLoaded(AdLayout adLayout, AdProperties adProperties) {
        container.setVisibility(View.VISIBLE);
        if (DEBUG) Log.i(tag, "######AMAZONadListener, onAdLoaded");
    }

    @Override
    public void onAdExpanded(AdLayout adLayout) {
        if (DEBUG) Log.i(tag, "######AMAZONadListener, onAdExpanded");
    }

    @Override
    public void onAdCollapsed(AdLayout adLayout) {
        if (DEBUG) Log.i(tag, "######AMAZONadListener, onAdCollapsed");
    }

    @Override
    public void onAdFailedToLoad(AdLayout adLayout, AdError adError) {
        if (DEBUG) Log.i(tag, "######AMAZONadListener, onAdFailedToLoad");
        if (DEBUG) Log.i(tag, "######AMAZONadListener, adError: "+ adError.getMessage());
        container.removeView(amazonAdView);
        container.setVisibility(View.GONE);
        mobFoxAd = new MobFoxAdLoad(mActivity, container);
    }
}
