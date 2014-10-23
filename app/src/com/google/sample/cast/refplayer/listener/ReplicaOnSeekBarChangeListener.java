package com.google.sample.cast.refplayer.listener;

import android.widget.SeekBar;

import com.google.sample.cast.refplayer.mediaplayer.LocalPlayerActivity;
import com.google.sample.castcompanionlibrary.utils.Utils;

public class ReplicaOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {


    private final LocalPlayerActivity localPlayerActivity;

    public ReplicaOnSeekBarChangeListener(LocalPlayerActivity localPlayerActivity) {
        super();
        this.localPlayerActivity = localPlayerActivity;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (localPlayerActivity.isPlaying()) {
            localPlayerActivity.play(seekBar.getProgress());
        } else if (!localPlayerActivity.isIdle()) {
            localPlayerActivity.seekTo(seekBar.getProgress());
        }
        localPlayerActivity.startControllersTimer();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        localPlayerActivity.stopTrickplayTimer();
        localPlayerActivity.pause();
        localPlayerActivity.stopControllersTimer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        localPlayerActivity.setStartText(Utils.formatMillis(progress));
    }
}
