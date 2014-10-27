package com.google.sample.cast.refplayer.browser;

import android.content.Context;

import com.google.android.gms.cast.MediaInfo;
import com.google.gson.Gson;
import com.google.sample.cast.refplayer.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReplicaVideoProvider {
    private Context context;

    public ReplicaVideoProvider(Context context) {
        this.context = context;
    }

    public List<MediaInfo> buildMediaList() {
        List<MediaInfo> mediaInfos = new ArrayList<MediaInfo>();
        InputStream inputStream = context.getResources().openRawResource(R.raw.videos);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        VideoList videoList = new Gson().fromJson(inputStreamReader, VideoList.class);
        for (Content content : videoList.getReturn().getContents()) {
            mediaInfos.add(content.buildMediaInfo());
        }
        return mediaInfos;
    }
}
