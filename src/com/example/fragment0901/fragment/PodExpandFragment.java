package com.example.fragment0901.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.fragment0901.R;
import com.example.fragment0901.utils.ESLConstants;
import com.example.fragment0901.utils.ESLNotificationManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import java.io.IOException;

public class PodExpandFragment extends Fragment implements OnClickListener, OnSeekCompleteListener, OnPreparedListener,
		OnCompletionListener, OnInflateListener, OnSeekBarChangeListener, OnBufferingUpdateListener, OnErrorListener {
	private final String tag = ((Object) this).getClass().getSimpleName();
	private View view;
	private Context context;
	private TextView tv1, tv3, timePassed, timeTotal;
	private SeekBar sBar;
	private ImageButton btnPlay, share, back;
    private Button btnFFd, btnRwnd;
	// private View line;
	private ProgressBar prepareProgress;
    private static String title;
	private static String summary;
	private static String link;
	private static String shareLink;
	private static String time;

    private ESLNotificationManager mESLNotificationManager;


	private boolean isPausedInCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;
    private LinearLayout adContainer;

	boolean downloaded = true;

	public MediaPlayer mp;
	private final Handler handler = new Handler();
    private boolean DEBUG = PodListActivity.loggingEnabled();
    private static boolean expandFragmentDestroyed;

    private AdView adView;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.i(tag, "######onCreateActivity, PodExpandFragment");
		context = getActivity();
		view = inflater.inflate(R.layout.pod_expand_fragment, container, false);
		initialViews();
        expandFragmentDestroyed = false;
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (DEBUG) Log.i(tag, event.getAction() + " onKey Back listener Fragment B");
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    if (DEBUG)
                        Log.i(tag, "onKey Back listener ActivityA");
                    if (PodListActivity.getTwoPane()){
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } else {
                        getActivity().finish();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
		Bundle extras = this.getArguments();

        if (extras != null) {
            if (DEBUG) Log.i(tag, "getArguments, title= " + extras.getString("title"));

            title = extras.getString(ESLConstants.TITLE_KEY);
            link = extras.getString(ESLConstants.LINK_KEY);
            shareLink = extras.getString(ESLConstants.SHARE_LINK_KEY);
            summary = extras.getString(ESLConstants.SUMMARY_KEY);
            time = extras.getString(ESLConstants.TIME_KEY);
        }

        mESLNotificationManager = new ESLNotificationManager(context);

		tv1.setText(title);
		tv3.setText(summary);
		back.setOnClickListener(this);
        share.setOnClickListener(this);

        startMediaPlayer();

        adView = new AdView(getActivity());
        adView.setAdUnitId(ESLConstants.MY_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);

        Bundle bundle = new Bundle();
        bundle.putString("color_bg", "EEFAEB");
        bundle.putString("color_text", "CCCCCC");
        AdMobExtras adExtras = new AdMobExtras(bundle);

        adContainer = (LinearLayout) view.findViewById(R.id.adViewContainerExpand);
        adContainer.setVisibility(View.GONE);
        adContainer.addView(adView);

        // Initiate a generic request.
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtras(adExtras).build();
        // .addTestDevice("D681537AB6AAA8DEA387EA0C864CBDC7")
        // Load the adView with the ad request.
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adContainer.setVisibility(View.VISIBLE);
            }
        });
		return view;
	}

    @Override
    public void onStart() {
        super.onStart();
        setupHandler();
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.i(tag, "######onStop, PodExpandFragment");
        super.onStop();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    private void startMediaPlayer(){
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(link);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mp.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mp.setOnPreparedListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnErrorListener(this);

        timeTotal.setText(time);
        timePassed.setText("00:00");
    }

	private void initialViews() {
		sBar = (SeekBar) view.findViewById(R.id.seekBar1);
		btnPlay = (ImageButton) view.findViewById(R.id.imageButton1);
		btnPlay.setBackgroundResource(R.drawable.ic_action_stop);
		tv1 = (TextView) view.findViewById(R.id.textView1);
		tv3 = (TextView) view.findViewById(R.id.textView3);
		timePassed = (TextView) view.findViewById(R.id.timepassed);
		timeTotal = (TextView) view.findViewById(R.id.timetotal);
		back = (ImageButton) view.findViewById(R.id.btnback);
		btnFFd = (Button) view.findViewById(R.id.btnfastforward);
		btnRwnd = (Button) view.findViewById(R.id.btnrewind);
		prepareProgress = (ProgressBar) view.findViewById(R.id.prepare_progress);
		prepareProgress.setVisibility(View.VISIBLE);
        share = (ImageButton) view.findViewById(R.id.share);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onPrepared(final MediaPlayer player) {
		prepareProgress.setVisibility(View.INVISIBLE);
		btnPlay.setBackgroundResource(R.drawable.ic_action_play);
		btnPlay.setOnClickListener(this);
		btnFFd.setOnClickListener(this);
		btnRwnd.setOnClickListener(this);

		mp.setOnCompletionListener(this);
		mp.setOnSeekCompleteListener(this);
		sBar.setOnSeekBarChangeListener(this);
	}

	public boolean mpIsPlaying() {
		if (mp != null && mp.isPlaying())
			return true;
		else
			return false;
	}

	public void onBufferingUpdate(MediaPlayer player, int percent) {
        if (DEBUG) Log.i(tag, "on Buffering Update" + percent);
		sBar.setMax(mp.getDuration());
		// transfer percent to media player duration scale
		sBar.setSecondaryProgress(percent * mp.getDuration() / 100);

		// show progress bar if buffered is smaller than player
		try {
			if ((percent * mp.getDuration() / 100) <= mp.getCurrentPosition())
				prepareProgress.setVisibility(View.VISIBLE);
			else
				prepareProgress.setVisibility(View.INVISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mp.seekTo(progress);
			progress = mp.getCurrentPosition();
			seekBar.setProgress(progress);
			setupHandler();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onInflate(ViewStub arg0, View arg1) {
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		mp.seekTo(100);
		sBar.setProgress(100);
		pauseMedia();
		btnPlay.setBackgroundResource(R.drawable.ic_action_play);
		timePassed.setText(milliSecondsToTimer(mp.getCurrentPosition()));
	}

	@Override
	public void onClick(View v) {
		int currentPosition = mp.getCurrentPosition();
		switch (v.getId()) {
		case R.id.imageButton1:
			if (!mp.isPlaying()) {
				playMedia();
                mESLNotificationManager.addNotification(title , PodListActivity.getTwoPane());
                if (!mp.isPlaying()){
                    prepareProgress.setVisibility(View.VISIBLE);
                    btnPlay.setBackgroundResource(R.drawable.ic_action_stop);
                    startMediaPlayer();
                }
			} else if (mp.isPlaying()) {
				pauseMedia();
			}

			telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (DEBUG) Log.v(tag, "Starting listener");
			phoneStateListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
                    if (DEBUG) Log.v(tag, "Starting CallStateChange");
					switch (state) {
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
                        try{

                            if (mp != null && mp.isPlaying()) {
                                mp.pause();
                                isPausedInCall = true;
                            }
                            break;
                        } catch (Exception e ){
                        }
                        case TelephonyManager.CALL_STATE_IDLE:
						// Phone idle. Start playing.
						if (mp != null) {
							if (isPausedInCall) {
								isPausedInCall = false;
								playMedia();
							}
						}
						break;
					}
				}
			};

			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

			break;

		case R.id.btnfastforward:

			if (currentPosition + 10000 < mp.getDuration()) {
				mp.seekTo(currentPosition + 10000);
				sBar.setProgress(currentPosition + 10000);
			} else {
				// forward to end position
				mp.seekTo(mp.getDuration() - 1000);
			}
			LogMediaPosition();
			break;

		case R.id.btnrewind:
			if (currentPosition - 10000 > 0) {
				mp.seekTo(currentPosition - 10000);
				sBar.setProgress(currentPosition - 10000);
			} else {
				// backward to starting position
				mp.seekTo(200);
			}
			LogMediaPosition();
			break;

        case R.id.share:
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink);
            startActivity(Intent.createChooser(shareIntent, "Share Via"));
            break;

		case R.id.btnback:
            if (PodListActivity.getTwoPane()){
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                Intent intent = new Intent(context, PodListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                // TODO: this flag is not reliable. save state of activity A and return back there. use service for media player.
                context.startActivity(intent);
            }
			break;

		// case R.id.btndownload:
		// Toast.makeText(getApplicationContext(), " download!",
		// Toast.LENGTH_SHORT).show();
		// break;
		}
	}

	private void playMedia() {
		btnPlay.setBackgroundResource(R.drawable.ic_action_pause);
		mp.start();
		setupHandler();
	}

	private void pauseMedia() {
		if (mp.isPlaying()) {
			btnPlay.setBackgroundResource(R.drawable.ic_action_play);
			mp.pause();
		}
	}

	// seekbar handler
	private void setupHandler() {
		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
	}

	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
            if (DEBUG) Log.i(tag, "runnable is running");
			if (mp.isPlaying()) {
				LogMediaPosition();
				handler.postDelayed(this, 1000);
			}
		}
	};

	private void LogMediaPosition() {

		try {
			sBar.setProgress(mp.getCurrentPosition());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timePassed.setText(milliSecondsToTimer(mp.getCurrentPosition()));
	}

	// converting milliseconds to time format
	public String milliSecondsToTimer(long milliseconds) {
		StringBuffer finalTimerString = new StringBuffer();

		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

		if (hours > 0) {
			finalTimerString.append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes))
					.append(":").append(String.format("%02d", seconds));
		} else {
			finalTimerString.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
		}
		return finalTimerString.toString();
	}

    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(tag, "######onDestroy, PodExpandFragment");
        adView.destroy();
        super.onDestroy();
        if (!PodListActivity.getTwoPane())
            getActivity().finish();
    }

	@Override
	public void onDestroyView() {
        if (DEBUG) Log.i(tag, "######onDestroyView, PodExpandFragment");
		super.onDestroyView();
		if (mp.isPlaying()) {
			mp.stop();
		}
		handler.removeCallbacks(sendUpdatesToUI);
		mp.release();
	    mESLNotificationManager.removeNotification();
        expandFragmentDestroyed = true;
	}

    public static boolean isDestroyed() {
        return expandFragmentDestroyed;
    }

}
