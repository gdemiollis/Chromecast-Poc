package com.google.sample.cast.refplayer.browser;

import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.gson.annotations.Expose;

public class Content {

    @Expose
    private String id;
    @Expose
    private String url;
	@Expose
	private String contentType;
	@Expose
	private String sourceType;
	@Expose
	private String name;
	@Expose
	private String title;
	@Expose
	private String description;
	@Expose
	private String state;
	@Expose
	private String creator;
	@Expose
	private String creationDate;
	@Expose
	private String modifiedDate;
	@Expose
	private String publicationDate;
	@Expose
	private String thumbnailUrl;

	public MediaInfo buildMediaInfo() {
		MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

		/*
		 * url = "http://bellvps1.cpl.delvenetworks.com/media/e1b3e24ecb944abd8f4ed823a0b76ddc/f2cdf08069744197994ebeba4275a314/ottawa-attaque-v2-34cdfb14370d3b65b91c417651f3d57531c53ac9.m3u8";
		 * subTitle = "Exemple de Sous-titre pour Video LP+";
		 * title = "Exemple de Titre pour Video LP+";
		 * imgUrl = "http://plus.lapresse.ca/cdn/img/LaPressePlus.svg";
		 * bigImageUrl= "http://plus.lapresse.ca/cdn/img/LaPressePlus.svg";
		 */

		movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, description);
		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
		movieMetadata.addImage(new WebImage(Uri.parse(thumbnailUrl)));
		movieMetadata.addImage(new WebImage(Uri.parse(thumbnailUrl)));

		return new MediaInfo.Builder(url)
		        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
		        .setContentType("video/m3u8")
		        .setMetadata(movieMetadata)
		        .build();
	}

}