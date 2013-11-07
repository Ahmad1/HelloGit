package com.example.fragment0901.fragment;

import java.io.IOException;

import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.fragment0901.R;

public class PodExpandFragment extends Fragment implements OnClickListener,
		OnSeekCompleteListener, OnPreparedListener, OnCompletionListener,
		OnInflateListener, OnSeekBarChangeListener, OnBufferingUpdateListener,
		OnErrorListener {
	private final String tag = ((Object)this).getClass().getSimpleName();
	private View view;
	private Context context;
	private TextView tv1, tv3, timePassed, timeTotal;
	private SeekBar sBar;
	private ImageButton btnPlay, btnffd, btnrwnd, download, back;
	// private View line;
	private ProgressBar prepareProgress;
	private PodExpandActivity mPodExpandActivity;
	private static String title;
	private static String summary;
	private static String link;
	private static String time;

	private boolean isPausedInCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;

	boolean downloaded = true;

	public MediaPlayer mp;
	private final Handler handler = new Handler();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e(tag, "onCreateView");
		context = getActivity();
		view = inflater.inflate(R.layout.pod_expand_fragment, container, false);
		initialViews();
		mp = new MediaPlayer();
		
		Bundle extras = this.getArguments();
		Log.e(tag, "getArguments, title= " + extras.getString("title"));
		
		title = extras.getString("title");
		link = extras.getString("link");
		summary = extras.getString("summary");
		time = extras.getString("time");
		
		tv1.setText(title);
		tv3.setText(summary);
		back.setOnClickListener(this);
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
		
		return view;
	}

	private void initialViews() {
		sBar = (SeekBar) view.findViewById(R.id.seekBar1);
		btnPlay = (ImageButton) view.findViewById(R.id.imageButton1);
		btnPlay.setBackgroundResource(R.drawable.stop);
		tv1 = (TextView) view.findViewById(R.id.textView1);
		tv3 = (TextView) view.findViewById(R.id.textView3);
		timePassed = (TextView) view.findViewById(R.id.timepassed);
		timeTotal = (TextView) view.findViewById(R.id.timetotal);
		back = (ImageButton) view.findViewById(R.id.btnback);
		btnffd = (ImageButton) view.findViewById(R.id.btnfastforward);
		btnrwnd = (ImageButton) view.findViewById(R.id.btnrewind);
		prepareProgress = (ProgressBar) view
				.findViewById(R.id.prepare_progress);
		prepareProgress.setVisibility(View.VISIBLE);
	}

	public void onPrepared(final MediaPlayer player) {
		prepareProgress.setVisibility(View.INVISIBLE);
		btnPlay.setBackgroundResource(R.drawable.play);
		btnPlay.setOnClickListener(this);
		btnffd.setOnClickListener(this);
		btnrwnd.setOnClickListener(this);

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
		// Log.i(TAG, "on Buffering Update" + percent);
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
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
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
		btnPlay.setBackgroundResource(R.drawable.play);
		timePassed.setText(milliSecondsToTimer(mp.getCurrentPosition()));
	}

	@Override
	public void onClick(View v) {
		int currentPosition = mp.getCurrentPosition();
		switch (v.getId()) {
		case R.id.imageButton1:
			if (!mp.isPlaying()) {
				playMedia();
			} else if (mp.isPlaying()) {
				pauseMedia();
			}
			telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// Log.v(TAG, "Starting listener");
			phoneStateListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					// Log.v(TAG, "Starting CallStateChange");
					switch (state) {
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						if (mp.isPlaying()) {
							mp.pause();
							isPausedInCall = true;
						}
						break;
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

			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);

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

		/*
		 * case R.id.btnback: Intent a = new Intent(this, MainActivity.class);
		 * a.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); startActivity(a);
		 * break;
		 */

		// case R.id.btndownload:
		// Toast.makeText(getApplicationContext(), " download!",
		// Toast.LENGTH_SHORT).show();
		// break;
		}
		// mPodExpandActivity.addNotification();
	}

	private void playMedia() {
		btnPlay.setBackgroundResource(R.drawable.pause);
		mp.start();
		setupHandler();
	}

	private void pauseMedia() {
		if (mp.isPlaying()) {
			btnPlay.setBackgroundResource(R.drawable.play);
			mp.pause();
		}
	}

	// seekbar handler
	private void setupHandler() {
		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
	}

	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
			// Log.i(TAG, "runnable is running");
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
			finalTimerString.append(String.format("%02d", hours)).append(":")
					.append(String.format("%02d", minutes)).append(":")
					.append(String.format("%02d", seconds));
		} else {
			finalTimerString.append(String.format("%02d", minutes)).append(":")
					.append(String.format("%02d", seconds));
		}
		return finalTimerString.toString();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if (mp.isPlaying()) {
			mp.stop();
		}
		handler.removeCallbacks(sendUpdatesToUI);
		mp.release();
		// mPodExpandActivity.removeNotification();
	}

}
