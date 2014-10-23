package com.google.sample.cast.refplayer.callback;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.mediaplayer.CastingActivity;
import com.google.sample.cast.refplayer.mediaplayer.PlaybackLocation;
import com.google.sample.castcompanionlibrary.cast.callbacks.BaseCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.utils.Utils;

public class ReplicaVideoCastConsumer extends BaseCastConsumerImpl implements IVideoCastConsumer {

	private CastingActivity activity;
	private MediaInfo mRemoteMediaInformation;

	public ReplicaVideoCastConsumer(CastingActivity activity) {
		this.activity = activity;
	}

	public static final String APP_DESTRUCTION_KEY = "application_destruction";
	public static final String FTU_SHOWN_KEY = "ftu_shown";

	@Override
	public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
		Log.d(getClass().getSimpleName(), "onApplicationLaunched() is reached");
		if (activity.hasMediaSelected()) {

			if (activity.isPlaying()) {
				activity.pause();
				try {
					activity.loadRemoteMedia(true);
					activity.finish();
				} catch (Exception e) {
					com.google.sample.cast.refplayer.utils.Utils.handleException(activity, e);
				}
			} else {
				activity.updatePlaybackLocation(PlaybackLocation.REMOTE);
			}
		}
	}

	@Override
	public boolean onApplicationConnectionFailed(int errorCode) {
		return false;
	}

	@Override
	public void onApplicationStopFailed(int errorCode) {

	}

	@Override
	public void onApplicationStatusChanged(String appStatus) {

	}

	@Override
	public void onDisconnected() {
		Log.d(getClass().getSimpleName(), "onDisconnected() is reached");
		activity.pause();
		activity.updatePlaybackLocation(PlaybackLocation.LOCAL);
	}

	@Override
	public void onVolumeChanged(double value, boolean isMute) {

	}

	@Override
	public void onApplicationDisconnected(int errorCode) {
		Log.d(getClass().getSimpleName(), "onApplicationDisconnected() is reached with errorCode: " + errorCode);
		activity.updatePlaybackLocation(PlaybackLocation.LOCAL);
	}

	@Override
	public void onRemoteMediaPlayerMetadataUpdated() {
		try {
			mRemoteMediaInformation = activity.getRemoteMediaInformation();
		} catch (Exception e) {
			// silent
		}
	}

	@Override
	public void onRemoteMediaPlayerStatusUpdated() {

	}

	@Override
	public void onRemovedNamespace() {

	}

	@Override
	public void onDataMessageSendFailed(int errorCode) {

	}

	@Override
	public void onDataMessageReceived(String message) {

	}

	@Override
	public void onTextTrackStyleChanged(TextTrackStyle style) {

	}

	@Override
	public void onTextTrackEnabledChanged(boolean isEnabled) {

	}

	@Override
	public void onTextTrackLocaleChanged(Locale locale) {

	}

	@Override
	public void onFailed(int resourceId, int statusCode) {

	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(getClass().getSimpleName(), "onConnectionSuspended() was called with cause: " + cause);
		Utils.showToast(activity, R.string.connection_temp_lost);
	}

	@Override
	public void onConnectivityRecovered() {
		Utils.showToast(activity, R.string.connection_recovered);
	}

	@Override
	public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
		if (!isFtuShown(activity)) {
			setFtuShown(activity);

			Log.d(getClass().getSimpleName(), "Route is visible: " + info);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (activity.isMediaRouteMenuItemVisible()) {
						Log.d(getClass().getSimpleName(), "Cast Icon is visible: " + info.getName());
						showFtu();
					}
				}
			}, 1000);
		}
	}

	public boolean isFtuShown(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPref.getBoolean(FTU_SHOWN_KEY, false);
	}

	public void setFtuShown(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		sharedPref.edit().putBoolean(FTU_SHOWN_KEY, true).commit();
	}

	private void showFtu() {
		new ShowcaseView.Builder(activity)
		        .setTarget(new ActionViewTarget(activity, ActionViewTarget.Type.MEDIA_ROUTE_BUTTON))
		        .setContentTitle(R.string.touch_to_cast)
		        .build();
	}
}
