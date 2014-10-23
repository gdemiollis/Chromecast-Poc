package com.google.sample.cast.refplayer.mediaplayer;

/*
     * indicates whether we are doing a local or a remote playback
	 */
	public enum PlaybackLocation {
		LOCAL,
		REMOTE;

    public boolean isLocal() {
        return this == PlaybackLocation.LOCAL;
    }

    public boolean isRemote() {
        return this == PlaybackLocation.REMOTE;
    }


    }