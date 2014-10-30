package com.google.sample.cast.refplayer.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.cast.refplayer.CastApplication;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.callback.ReplicaVideoCastConsumer;
import com.google.sample.cast.refplayer.settings.CastPreference;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

public abstract class CastingActivity extends ActionBarActivity {

	protected VideoCastManager mCastManager;
	private MenuItem mediaRouteMenuItem;
	private MiniController mMini;
	private ReplicaVideoCastConsumer mCastConsumer;
	protected MediaInfo mSelectedMedia;
	protected PlaybackLocation mLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCastManager = CastApplication.getCastManager(this);
		mCastConsumer = new ReplicaVideoCastConsumer(this);
	}

	@Override
	protected void onStart() {
		Log.d(getClass().getSimpleName(), "onStart was called");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(getClass().getSimpleName(), "onResume() was called");
		mCastManager = CastApplication.getCastManager(this);
		if (null != mCastManager) {
			mCastManager.addVideoCastConsumer(mCastConsumer);
			mCastManager.incrementUiCounter();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCastManager.removeVideoCastConsumer(mCastConsumer);
		mMini.removeOnMiniControllerChangedListener(mCastManager);
		mCastManager.decrementUiCounter();
	}

	@Override
	protected void onStop() {
		Log.d(getClass().getSimpleName(), "onStop() was called");
		super.onStop();
	}

	public boolean isMediaRouteMenuItemVisible() {
		return mediaRouteMenuItem.isVisible();
	}

	protected void setupActionBar() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setIcon(R.drawable.actionbar_logo_castvideos);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
		//getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_transparent_democastoverlay));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, CastPreference.class);
			startActivity(i);
			break;
		}
		return true;
	}

	public void loadRemoteMedia(boolean autoPlay) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
		return true;
	}

	protected void setupMiniController() {
		mMini = (MiniController) findViewById(R.id.miniController1);
		mCastManager.addMiniController(mMini);
	}

	@Override
	protected void onDestroy() {
		Log.d(getClass().getSimpleName(), "onDestroy() is called");
		if (null != mCastManager) {
			mMini.removeOnMiniControllerChangedListener(mCastManager);
			mCastManager.removeMiniController(mMini);
			mCastManager.clearContext(this);
			mCastConsumer = null;
		}
		super.onDestroy();
	}

	public MediaInfo getRemoteMediaInformation() throws TransientNetworkDisconnectionException, NoConnectionException {
		return mCastManager.getRemoteMediaInformation();
	}

	public abstract boolean isPlaying();

	public void pause() {
	}

	public void updatePlaybackLocation(PlaybackLocation location) {
		this.mLocation = location;
	}

    public boolean hasMediaSelected() {
        return null != mSelectedMedia;
    }
}
