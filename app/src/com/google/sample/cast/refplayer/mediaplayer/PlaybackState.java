package com.google.sample.cast.refplayer.mediaplayer;

/*
 * List of various states that we can be in
 */
public enum PlaybackState {
	PLAYING, PAUSED, BUFFERING, IDLE;

	public boolean isPlaying() {
		return this == PlaybackState.PLAYING;
	}

	public boolean isIdle() {
		return this == PlaybackState.IDLE;
	}

    public boolean isBuffering() {
        return this == PlaybackState.BUFFERING;
    }
}
