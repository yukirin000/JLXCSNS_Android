package com.jlxc.app.base.model;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.utils.JLXCConst;

public class NetworkImage {

	// 图片id
	private String imageId;
	// 图片的类型
	private String imageType;
	// 缩略图地址
	private String subURL;
	// 地址
	private String URL;
	// 尺寸
	private String imageStize;
	// 发布的时间
	private String addDate;
	// 宽度
	private String imageWidth;
	// 高度
	private String imageHheight;

	// 内容注入
	public void setContentWithJson(JSONObject object) {
		setImageId(object.getString("id"));
		setImageType(object.getString("type"));
		setSubURL(JLXCConst.ATTACHMENT_ADDR + object.getString("sub_url"));
		setURL(JLXCConst.ATTACHMENT_ADDR + object.getString("url"));
		setImageStize(object.getString("size"));
		setAddDate(object.getString("add_date"));
		setImageWidth(object.getString("width"));
		setImageHheight(object.getString("height"));
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getSubURL() {
		return subURL;
	}

	public void setSubURL(String subURL) {
		this.subURL = subURL;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getImageStize() {
		return imageStize;
	}

	public void setImageStize(String imageStize) {
		this.imageStize = imageStize;
	}

	public String getAddDate() {
		return addDate;
	}

	public void setAddDate(String addDate) {
		this.addDate = addDate;
	}

	public String getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(String imageWidth) {
		this.imageWidth = imageWidth;
	}

	public String getImageHheight() {
		return imageHheight;
	}

	public void setImageHheight(String imageHheight) {
		this.imageHheight = imageHheight;
	}

}
