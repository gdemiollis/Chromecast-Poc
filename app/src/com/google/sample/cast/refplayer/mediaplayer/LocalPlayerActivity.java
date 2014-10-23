/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cast.refplayer.mediaplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.cast.refplayer.CastApplication;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.listener.ReplicaOnSeekBarChangeListener;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;

public class LocalPlayerActivity extends CastingActivity {

	private static final String TAG = "LocalPlayerActivity";
	private VideoView mVideoView;
	private TextView mTitleView;
	private TextView mDescriptionView;
	private TextView mStartText;
	private TextView mEndText;
	private SeekBar mSeekbar;
	private ImageView mPlayPause;
	private ProgressBar mLoading;
	private View mControlers;
	private View mContainer;
	private ImageView mCoverArt;
	private Timer mSeekbarTimer;
	private Timer mControlersTimer;
	private final Handler mHandler = new Handler();
	private AQuery mAquery;
	private boolean mControlersVisible;
	private int mDuration;
	private PlaybackState mPlaybackState = PlaybackState.IDLE;

	private TextView mAuthorView;

	public boolean isPlaying() {
		return mPlaybackState.isPlaying();
	}

	public boolean isIdle() {
		return mPlaybackState.isIdle();
	}

	public void seekTo(int progress) {
		mVideoView.seekTo(progress);
	}

	public void pause() {
		mPlaybackState = PlaybackState.PAUSED;
		mVideoView.pause();
	}

	public void setStartText(String startText) {
		mStartText.setText(startText);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_activity);
		mAquery = new AQuery(this);
		loadViews();
		setupActionBar();
		setupControlsCallbacks();
		setupMiniController();
		// see what we need to play and were
		Bundle b = getIntent().getExtras();
		if (null != b) {
			mSelectedMedia = Utils.toMediaInfo(getIntent().getBundleExtra("media"));
			boolean mShouldStartPlayback = b.getBoolean("shouldStart");
			int startPosition = b.getInt("startPosition", 0);
			String url = mSelectedMedia.getContentId();
			mVideoView.setVideoURI(Uri.parse(url));
			Log.d(TAG, "Setting url of the VideoView to: " + mSelectedMedia.getContentId());
			if (mShouldStartPlayback) {
				// this will be the case only if we are coming from the
				// CastControllerActivity by disconnecting from a device
				mPlaybackState = PlaybackState.PLAYING;
				updatePlaybackLocation(PlaybackLocation.LOCAL);
				updatePlayButton(mPlaybackState);
				if (startPosition > 0) {
					mVideoView.seekTo(startPosition);
				}
				mVideoView.start();
				startControllersTimer();
			} else {
				// we should load the video but pause it
				// and show the album art.
				if (mCastManager.isConnected()) {
					updatePlaybackLocation(PlaybackLocation.REMOTE);
				} else {
					updatePlaybackLocation(PlaybackLocation.LOCAL);
				}
				mPlaybackState = PlaybackState.PAUSED;
				updatePlayButton(mPlaybackState);
			}
		}
		if (null != mTitleView) {
			updateMetadata(true);
		}
	}

	public void updatePlaybackLocation(PlaybackLocation location) {
		super.updatePlaybackLocation(location);
		if (location.isLocal()) {
			if (mPlaybackState.isPlaying() || mPlaybackState.isBuffering()) {
				setCoverArtStatus(null);
				startControllersTimer();
			} else {
				stopControllersTimer();
				setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils.
				        getImageUrl(mSelectedMedia, 0));
			}

			getSupportActionBar().setTitle("");
		} else {
			stopControllersTimer();
			setCoverArtStatus(com.google.sample.castcompanionlibrary.utils.Utils.
			        getImageUrl(mSelectedMedia, 0));
			updateControlersVisibility(true);
		}
	}

	public void play(int position) {
		startControllersTimer();
		switch (mLocation) {
		case LOCAL:
			mVideoView.seekTo(position);
			mVideoView.start();
			break;
		case REMOTE:
			mPlaybackState = PlaybackState.BUFFERING;
			updatePlayButton(mPlaybackState);
			try {
				mCastManager.play(position);
			} catch (Exception e) {
				com.google.sample.cast.refplayer.utils.Utils.handleException(this, e);
			}
			break;
		default:
			break;
		}
		restartTrickplayTimer();
	}

	private void togglePlayback() {
		stopControllersTimer();
		switch (mPlaybackState) {
		case PAUSED:
			switch (mLocation) {
			case LOCAL:
				mVideoView.start();
				if (!mCastManager.isConnecting()) {
					Log.d(TAG, "Playing locally...");
					mCastManager.clearPersistedConnectionInfo(VideoCastManager.CLEAR_SESSION);
				}
				mPlaybackState = PlaybackState.PLAYING;
				startControllersTimer();
				restartTrickplayTimer();
				updatePlaybackLocation(PlaybackLocation.LOCAL);
				break;
			case REMOTE:
				try {
					mCastManager.checkConnectivity();
					Log.d(TAG, "Playing remotely...");
					loadRemoteMedia(0, true);
					finish();
				} catch (Exception e) {
					com.google.sample.cast.refplayer.utils.Utils.handleException(LocalPlayerActivity.this, e);
					return;
				}
				break;
			default:
				break;
			}
			break;

		case PLAYING:
			mPlaybackState = PlaybackState.PAUSED;
			mVideoView.pause();
			break;

		case IDLE:
			mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
			mVideoView.seekTo(0);
			mVideoView.start();
			mPlaybackState = PlaybackState.PLAYING;
			restartTrickplayTimer();
			break;

		default:
			break;
		}
		updatePlayButton(mPlaybackState);
	}

	private void setCoverArtStatus(String url) {
		if (null != url) {
			mAquery.id(mCoverArt).image(url);
			mCoverArt.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.INVISIBLE);
		} else {
			mCoverArt.setVisibility(View.GONE);
			mVideoView.setVisibility(View.VISIBLE);
		}
	}

	public void stopTrickplayTimer() {
		Log.d(TAG, "Stopped TrickPlay Timer");
		if (null != mSeekbarTimer) {
			mSeekbarTimer.cancel();
		}
	}

	private void restartTrickplayTimer() {
		stopTrickplayTimer();
		mSeekbarTimer = new Timer();
		mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1000);
		Log.d(TAG, "Restarted TrickPlay Timer");
	}

	public void stopControllersTimer() {
		if (null != mControlersTimer) {
			mControlersTimer.cancel();
		}
	}

	public void startControllersTimer() {
		if (null != mControlersTimer) {
			mControlersTimer.cancel();
		}
		if (mLocation.isRemote()) {
			return;
		}
		mControlersTimer = new Timer();
		mControlersTimer.schedule(new HideControllersTask(), 5000);
	}

	// should be called from the main thread
	private void updateControlersVisibility(boolean show) {
		if (show) {
			getSupportActionBar().show();
			mControlers.setVisibility(View.VISIBLE);
			mControlersVisible = true;
		} else {
			getSupportActionBar().hide();
			mControlers.setVisibility(View.INVISIBLE);
			mControlersVisible = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause() was called");
		if (mLocation.isLocal()) {

			if (null != mSeekbarTimer) {
				mSeekbarTimer.cancel();
				mSeekbarTimer = null;
			}
			if (null != mControlersTimer) {
				mControlersTimer.cancel();
			}
			// since we are playing locally, we need to stop the playback of
			// video (if user is not watching, pause it!)
			mVideoView.pause();
			mPlaybackState = PlaybackState.PAUSED;
			updatePlayButton(PlaybackState.PAUSED);
		}
	}

	private class HideControllersTask extends TimerTask {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					updateControlersVisibility(false);
				}
			});

		}
	}

	private class UpdateSeekbarTask extends TimerTask {

		@Override
		public void run() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					int currentPos = 0;
					if (mLocation.isLocal()) {
						currentPos = mVideoView.getCurrentPosition();
						updateSeekbar(currentPos, mDuration);
					}
				}
			});
		}
	}

	private void setupControlsCallbacks() {
		mVideoView.setOnErrorListener(onErrorListener);
		mVideoView.setOnPreparedListener(onPrepareListener);
		mVideoView.setOnCompletionListener(onCompletionListener);
		mVideoView.setOnTouchListener(onTouchListener);
		mSeekbar.setOnSeekBarChangeListener(new ReplicaOnSeekBarChangeListener(this));
		mPlayPause.setOnClickListener(onClickListener);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void updateSeekbar(int position, int duration) {
		mSeekbar.setProgress(position);
		mSeekbar.setMax(duration);
		mStartText.setText(com.google.sample.castcompanionlibrary.utils.Utils
		        .formatMillis(position));
		mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils.formatMillis(duration));
	}

	private void updatePlayButton(PlaybackState state) {
		switch (state) {
		case PLAYING:
			mLoading.setVisibility(View.INVISIBLE);
			mPlayPause.setVisibility(View.VISIBLE);
			mPlayPause.setImageDrawable(
			        getResources().getDrawable(R.drawable.ic_av_pause_dark));
			break;
		case PAUSED:
		case IDLE:
			mLoading.setVisibility(View.INVISIBLE);
			mPlayPause.setVisibility(View.VISIBLE);
			mPlayPause.setImageDrawable(
			        getResources().getDrawable(R.drawable.ic_av_play_dark));
			break;
		case BUFFERING:
			mPlayPause.setVisibility(View.INVISIBLE);
			mLoading.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			        WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
			updateMetadata(false);
			mContainer.setBackgroundColor(getResources().getColor(R.color.black));

		} else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
			        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(
			        WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
			updateMetadata(true);
			mContainer.setBackgroundColor(getResources().getColor(R.color.white));
		}
	}

	private void updateMetadata(boolean visible) {
		Point mDisplaySize;
		if (!visible) {
			mDescriptionView.setVisibility(View.GONE);
			mTitleView.setVisibility(View.GONE);
			mAuthorView.setVisibility(View.GONE);
			mDisplaySize = com.google.sample.cast.refplayer.utils.Utils.getDisplaySize(this);
			RelativeLayout.LayoutParams lp = new
			        RelativeLayout.LayoutParams(mDisplaySize.x,
			                mDisplaySize.y + getSupportActionBar().getHeight());
			lp.addRule(RelativeLayout.CENTER_IN_PARENT);
			mVideoView.setLayoutParams(lp);
			mVideoView.invalidate();
		} else {
			MediaMetadata mm = mSelectedMedia.getMetadata();
			mDescriptionView.setText(mm.getString(MediaMetadata.KEY_STUDIO));
			mTitleView.setText(mm.getString(MediaMetadata.KEY_TITLE));
			mAuthorView.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
			mDescriptionView.setVisibility(View.VISIBLE);
			mTitleView.setVisibility(View.VISIBLE);
			mAuthorView.setVisibility(View.VISIBLE);
			mDisplaySize = com.google.sample.cast.refplayer.utils.Utils.getDisplaySize(this);
			float mAspectRatio = 72f / 128;
			RelativeLayout.LayoutParams lp = new
			        RelativeLayout.LayoutParams(mDisplaySize.x,
			                (int) (mDisplaySize.x * mAspectRatio));
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mVideoView.setLayoutParams(lp);
			mVideoView.invalidate();
		}
	}

	private void loadRemoteMedia(int progress, boolean autoPlay) {
		mCastManager.startCastControllerActivity(this, mSelectedMedia, progress, autoPlay);
	}

	@Override
	public void loadRemoteMedia(boolean autoPlay) {
		mCastManager.startCastControllerActivity(this, mSelectedMedia, mSeekbar.getProgress(), autoPlay);
	}

	private void loadViews() {
		mVideoView = (VideoView) findViewById(R.id.videoView1);
		mTitleView = (TextView) findViewById(R.id.textView1);
		mDescriptionView = (TextView) findViewById(R.id.textView2);
		mDescriptionView.setMovementMethod(new ScrollingMovementMethod());
		mAuthorView = (TextView) findViewById(R.id.textView3);
		mStartText = (TextView) findViewById(R.id.startText);
		mEndText = (TextView) findViewById(R.id.endText);
		mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
		// mVolBar = (SeekBar) findViewById(R.id.seekBar2);
		mPlayPause = (ImageView) findViewById(R.id.imageView2);
		mLoading = (ProgressBar) findViewById(R.id.progressBar1);
		// mVolumeMute = (ImageView) findViewById(R.id.imageView2);
		mControlers = findViewById(R.id.controllers);
		mContainer = findViewById(R.id.container);
		mCoverArt = (ImageView) findViewById(R.id.coverArtView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopControllersTimer();
		stopTrickplayTimer();

	}

	private OnErrorListener onErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e(TAG, "OnErrorListener.onError(): VideoView encountered an " +
			        "error, what: " + what + ", extra: " + extra);
			String msg = "";
			if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
				msg = getString(R.string.video_error_media_load_timeout);
			} else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
				msg = getString(R.string.video_error_server_unaccessible);
			} else {
				msg = getString(R.string.video_error_unknown_error);
			}
			Utils.showErrorDialog(LocalPlayerActivity.this, msg);
			mVideoView.stopPlayback();
			mPlaybackState = PlaybackState.IDLE;
			updatePlayButton(mPlaybackState);
			return true;
		}
	};

	private OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (!mControlersVisible) {
				updateControlersVisibility(true);
			}
			startControllersTimer();
			return false;
		}
	};

	private OnPreparedListener onPrepareListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "onPrepared is reached");
			mDuration = mp.getDuration();
			mEndText.setText(com.google.sample.castcompanionlibrary.utils.Utils
			        .formatMillis(mDuration));
			mSeekbar.setMax(mDuration);
			restartTrickplayTimer();
		}
	};

	private OnCompletionListener onCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			stopTrickplayTimer();
			mPlaybackState = PlaybackState.IDLE;
			updatePlayButton(PlaybackState.IDLE);
		}
	};

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.imageView2)
				togglePlayback();
		}
	};
}
