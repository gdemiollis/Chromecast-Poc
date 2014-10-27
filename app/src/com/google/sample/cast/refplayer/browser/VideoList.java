package com.google.sample.cast.refplayer.browser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoList {

	@SerializedName("return")
	@Expose
	private Return _return;

	public Return getReturn() {
		return _return;
	}

}