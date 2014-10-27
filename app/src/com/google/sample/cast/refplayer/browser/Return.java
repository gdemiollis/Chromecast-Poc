package com.google.sample.cast.refplayer.browser;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class Return {

	@Expose
	private List<Content> contents = new ArrayList<Content>();

	public List<Content> getContents() {
		return contents;
	}

}