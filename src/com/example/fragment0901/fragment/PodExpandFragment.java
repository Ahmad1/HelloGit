package com.example.fragment0901.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.banner.AdView;
import com.example.fragment0901.R;
import com.example.fragment0901.utils.ESLConstants;
import com.example.fragment0901.utils.ESLNotificationManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PodExpandFragment extends Fragment implements OnClickListener, OnSeekCompleteListener, OnPreparedListener,
		OnCompletionListener, OnInflateListener, OnSeekBarChangeListener, OnBufferingUpdateListener, OnErrorListener, com.adsdk.sdk.AdListener {
	private final String tag = ((Object) this).getClass().getSimpleName();
	private View view;
	private Context mContext;
	private TextView tv1, tv3, timePassed, timeTotal;
	private SeekBar sBar, volumebar;
	private ImageButton btnPlay, share, back, volume;
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
    private AudioManager am;

    private AdView adView;
    private boolean volumebarShown;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.i(tag, "######onCreateActivity, PodExpandFragment");
		mContext = getActivity();
        am = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
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

        mESLNotificationManager = new ESLNotificationManager(mContext);

		tv1.setText(title);
		tv3.setText(summary);
		back.setOnClickListener(this);
		volume.setOnClickListener(this);
        share.setOnClickListener(this);

        startMediaPlayer();

        adView = new AdView(mContext, "http://my.mobfox.com/request.php",ESLConstants.PUBLISHER_ID, true, true);
        adView.setAdspaceWidth(320);
        // Optional, used to set the custom size of banner placement. Without setting it,
        // the SDK will use default size of 320x50 or 300x50 depending on device type.
        adView.setAdspaceHeight(50);
        adView.setAdspaceStrict(false);
        // Optional, tells the server to only supply banner ads that are exactly of the desired size.
        // Without setting it, the server could also supply smaller Ads when no ad of desired size is available.

        adView.setAdListener(this);
        adContainer = (LinearLayout) view.findViewById(R.id.adViewContainerExpand);
        adContainer.setVisibility(View.GONE);
        adContainer.addView(adView);

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

    RelativeLayout volumeContainer;
	private void initialViews() {
		sBar = (SeekBar) view.findViewById(R.id.seekBar1);
		btnPlay = (ImageButton) view.findViewById(R.id.imageButton1);
		btnPlay.setBackgroundResource(R.drawable.ic_action_stop);
		tv1 = (TextView) view.findViewById(R.id.textView1);
		tv3 = (TextView) view.findViewById(R.id.textView3);
		timePassed = (TextView) view.findViewById(R.id.timepassed);
		timeTotal = (TextView) view.findViewById(R.id.timetotal);
		back = (ImageButton) view.findViewById(R.id.btnBack);
		volume = (ImageButton) view.findViewById(R.id.volume);
		volumebar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        volumeContainer = (RelativeLayout) view.findViewById(R.id.volume_container);
		btnFFd = (Button) view.findViewById(R.id.btnfastforward);
		btnRwnd = (Button) view.findViewById(R.id.btnrewind);
		prepareProgress = (ProgressBar) view.findViewById(R.id.prepare_progress);
		prepareProgress.setVisibility(View.VISIBLE);
        share = (ImageButton) view.findViewById(R.id.share);
        volumebar.setEnabled(true);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

	}

    // called from the activity which created this fragment.
    public void volumeChanged() {
        if (volumeContainer.getVisibility()== View.VISIBLE) {
            if (volumebar != null && audioManager != null) {
                volumebar.setMax(audioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                volumebar.setProgress(audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        }
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
        volumebar.setOnSeekBarChangeListener(mVolumebarListener);
	}

    private int progressVolume;
    private AudioManager audioManager;
    OnSeekBarChangeListener mVolumebarListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progressVolume = progress;
            if (fromUser) {
                volumebar.setMax(audioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                volumebar.setProgress(progressVolume);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progressVolume, 0);
                Log.i("TAG", "VOLUME CHANGED=" + progressVolume);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

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
        abandonAudioFocus();
		btnPlay.setBackgroundResource(R.drawable.ic_action_play);
		timePassed.setText(milliSecondsToTimer(mp.getCurrentPosition()));
	}

    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
            pauseMedia();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            playMedia();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
            am.abandonAudioFocus(afChangeListener);
            pauseMedia();
        }
    }
};

    private boolean requestTheAudioFocus(){
        boolean granted ;
        int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        granted = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (granted){
            // am.registerMediaButtonEventReceiver(RemoteControlReceiver); TODO
        }
        return granted;
    }

    private void abandonAudioFocus(){
        am.abandonAudioFocus(afChangeListener);
    }

	@Override
	public void onClick(View v) {
		int currentPosition = mp.getCurrentPosition();
		switch (v.getId()) {
		case R.id.imageButton1:
			if (!mpIsPlaying()) {
                requestTheAudioFocus();
                playMedia();
                mESLNotificationManager.addNotification(title , PodListActivity.getTwoPane());
                if (!mpIsPlaying()){
                    prepareProgress.setVisibility(View.VISIBLE);
                    btnPlay.setBackgroundResource(R.drawable.ic_action_stop);
                    startMediaPlayer();
                }
			} else if (mpIsPlaying()) {
				pauseMedia();
                abandonAudioFocus();
			}

			telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (DEBUG) Log.v(tag, "Starting listener");
			phoneStateListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
                    if (DEBUG) Log.v(tag, "Starting CallStateChange");
					switch (state) {
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
                        try{
                            if (mpIsPlaying()) {
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

		case R.id.btnBack:
            if (PodListActivity.getTwoPane()){
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                Intent intent = new Intent(mContext, PodListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                // TODO: this flag is not reliable. save state of activity A and return back there. use service for media player.
                mContext.startActivity(intent);
            }
			break;

        case R.id.volume:
            volumebar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumebar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));
            showVolumeSeekBar();
            break;

		// case R.id.btndownload:
		// Toast.makeText(getApplicationContext(), " download!",
		// Toast.LENGTH_SHORT).show();
		// break;
		}
	}

    private TimerTask volumeTask;
    private Animation fadeOut;
    private void showVolumeSeekBar() {
        if (!volumebarShown) {
            volumeContainer.setVisibility(View.VISIBLE);
            volumeTask = new TimerTask() {
                @Override
                public void run() {
                    hideVolumebar();
                }
            };
            volumebarShown = true;
            new Timer().schedule(volumeTask , 3500);
        } else {
            hideVolumebar();
            if (volumeTask != null) volumeTask.cancel();
        }
    }

    private void hideVolumebar() {
        if (volumebarShown){
            fadeOut = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
            if (fadeOut != null ) fadeOut.setDuration(500);
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                volumeContainer.startAnimation(fadeOut);
                volumeContainer.setVisibility(View.GONE);
                volumebarShown = false;
            }
            });
        }
    }

    private void playMedia() {
		btnPlay.setBackgroundResource(R.drawable.ic_action_pause);
		mp.start();
		setupHandler();
	}

	private void pauseMedia() {
		if (mpIsPlaying()) {
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
			if (mpIsPlaying()) {
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
        // adView.pause();
        super.onPause();
        hideVolumebar();
        if (volumeTask != null) volumeTask.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        // adView.resume();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(tag, "######onDestroy, PodExpandFragment");
        super.onDestroy();
        if (!PodListActivity.getTwoPane())
            getActivity().finish();
        am.abandonAudioFocus(afChangeListener);
    }

	@Override
	public void onDestroyView() {
        if (DEBUG) Log.i(tag, "######onDestroyView, PodExpandFragment");
		super.onDestroyView();
		if (mpIsPlaying()) {
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
        adContainer.setVisibility(View.VISIBLE);
        if (DEBUG) Log.i(tag, "######adListener, adLoadSucceeded");
    }

    @Override
    public void adShown(Ad ad, boolean b) {
        if (DEBUG) Log.i(tag, "######adListener, adShown");
    }

    @Override
    public void noAdFound() {
        if (DEBUG) Log.i(tag, "######adListener, noAdFound");
    }
}
