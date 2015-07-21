package com.jlxc.app.discovery.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class PersonModel {

	// 用户的id
	private String uerId;
	// 用户名
	private String userName;
	// 用户所在的学校
	private String userSchool;
	// 学校代码
	private String schoolCoed;
	// 用户的头像
	private String headImage;
	// 头像的缩略图
	private String headSubImage;
	// 照片列表
	private List<String> imageList;
	// 关系来源
	private String type;

	@SuppressWarnings("unchecked")
	public void setContentWithJson(JSONObject object) {

		if (object.containsKey("uid")) {
			setUerId(object.getString("uid"));
		}
		if (object.containsKey("name")) {
			setUserName(object.getString("name"));
		}
		if (object.containsKey("school")) {
			setUserSchool(object.getString("school"));
		}
		if (object.containsKey("school_code")) {
			setSchoolCoed(object.getString("school_code"));
		}
		if (object.containsKey("head_image")) {
			setHeadImage(object.getString("head_image"));
		}
		if (object.containsKey("head_sub_image")) {
			setHeadSubImage(object.getString("head_sub_image"));
		}

		// 图片的转换
		if (object.containsKey("images")) {
			List<JSONObject> JImageObj = (List<JSONObject>) object
					.get("images");
			List<String> imgList = new ArrayList<String>();
			for (JSONObject imgObject : JImageObj) {
				imgList.add(imgObject.getString("sub_url"));
			}
			setImageList(imgList);
		}

		// 类型转换
		if (object.containsKey("type")) {
			JSONObject JTypeObj = (JSONObject) object.get("type");
			setType(JTypeObj.getString("content"));
		}
	}

	public String getUerId() {
		return uerId;
	}

	public void setUerId(String uerId) {
		this.uerId = uerId;
	}

	public String getUserSchool() {
		return userSchool;
	}

	public void setUserSchool(String userSchool) {
		this.userSchool = userSchool;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSchoolCoed() {
		return schoolCoed;
	}

	public void setSchoolCoed(String schoolCoed) {
		this.schoolCoed = schoolCoed;
	}

	public String getHeadImage() {
		return headImage;
	}

	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}

	public String getHeadSubImage() {
		return headSubImage;
	}

	public void setHeadSubImage(String headSubImage) {
		this.headSubImage = headSubImage;
	}

	public List<String> getImageList() {
		return imageList;
	}

	public void setImageList(List<String> imageList) {
		this.imageList = imageList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}