package com.jlxc.app.base.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.utils.JLXCConst;

public class NewsModel {

	// 用户id
	private String uid;
	// 用户的名字
	private String userName;
	// 用户的学校
	private String userSchool;
	// 用户的头像
	private String userHeadImage;
	// 用户的头像缩略图
	private String userHeadSubImage;
	// 动态的ID
	private String newsID;
	// 动态内容
	private String newsContent;
	// 发布的位置
	private String location;
	// 动态的评论量
	private String commentQuantity;
	// 动态的浏览量
	private String browseQuantity;
	// 动态的点赞量
	private String likeQuantity;
	// 发布的时间
	private String sendTime;
	// 添加的图片列表
	private List<ImageModel> imageNewsList = new ArrayList<ImageModel>();
	// 用户是否点赞
	private String isLike;
	// 评论列表
	private List<CommentModel> replyList = new ArrayList<CommentModel>();
	// 点赞的人列表
	private List<LikeModel> likeHeadListimage = new ArrayList<LikeModel>();
	// 发布动态的时间戳
	private String timesTamp;
	// 关系类型
	private Map<String, String> TYPE = new HashMap<String, String>();

	// 内容注入
	@SuppressWarnings("unchecked")
	public void setContentWithJson(JSONObject object) {

		setUid(object.getString("uid"));
		setUserName(object.getString("name"));
		setUserSchool(object.getString("school"));
		setUserHeadImage(JLXCConst.ATTACHMENT_ADDR
				+ object.getString("head_image"));
		setUserHeadSubImage(JLXCConst.ATTACHMENT_ADDR
				+ object.getString("head_sub_image"));
		setNewsID(object.getString("add_date"));
		setNewsContent(object.getString("content_text"));
		setLocation(object.getString("location"));
		setCommentQuantity(object.getString("comment_quantity"));
		setBrowseQuantity(object.getString("browse_quantity"));
		setLikeQuantity(object.getString("like_quantity"));
		setSendTime(object.getString("add_date"));
		setIsLike(object.getString("is_like"));
		setImageNewsList((List<ImageModel>) object.get("images"));
		setReplyList((List<CommentModel>) object.get("comments"));
		setLikeHeadListimage((List<LikeModel>) object.get("likes"));
		setTimesTamp(object.getString("add_time"));

		Map<String, String> tempMap = new HashMap<String, String>();
		JSONObject typeObject = (JSONObject) object.get("type");
		tempMap.put("type", typeObject.getString("type"));
		tempMap.put("fid", typeObject.getString("fid"));
		tempMap.put("content", typeObject.getString("content"));
		setTYPE(tempMap);

	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserSchool() {
		return userSchool;
	}

	public void setUserSchool(String userSchool) {
		this.userSchool = userSchool;
	}

	public String getUserHeadImage() {
		return userHeadImage;
	}

	public void setUserHeadImage(String userHeadImage) {
		this.userHeadImage = userHeadImage;
	}

	public String getUserHeadSubImage() {
		return userHeadSubImage;
	}

	public void setUserHeadSubImage(String userHeadSubImage) {
		this.userHeadSubImage = userHeadSubImage;
	}

	public String getNewsID() {
		return newsID;
	}

	public void setNewsID(String newsID) {
		this.newsID = newsID;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getNewsContent() {
		return newsContent;
	}

	public void setNewsContent(String newsContent) {
		this.newsContent = newsContent;
	}

	public String getCommentQuantity() {
		return commentQuantity;
	}

	public void setCommentQuantity(String commentQuantity) {
		this.commentQuantity = commentQuantity;
	}

	public String getBrowseQuantity() {
		return browseQuantity;
	}

	public void setBrowseQuantity(String browseQuantity) {
		this.browseQuantity = browseQuantity;
	}

	public String getLikeQuantity() {
		return likeQuantity;
	}

	public void setLikeQuantity(String likeQuantity) {
		this.likeQuantity = likeQuantity;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public List<ImageModel> getImageNewsList() {
		return imageNewsList;
	}

	public void setImageNewsList(List<ImageModel> imageNewsList) {
		this.imageNewsList = imageNewsList;
	}

	public String getIsLike() {
		return isLike;
	}

	public void setIsLike(String isLike) {
		this.isLike = isLike;
	}

	public List<LikeModel> getLikeHeadListimage() {
		return likeHeadListimage;
	}

	public void setLikeHeadListimage(List<LikeModel> likeHeadListimage) {
		this.likeHeadListimage = likeHeadListimage;
	}

	public List<CommentModel> getReplyList() {
		return replyList;
	}

	public void setReplyList(List<CommentModel> replyList) {
		this.replyList = replyList;
	}

	public String getTimesTamp() {
		return timesTamp;
	}

	public void setTimesTamp(String timesTamp) {
		this.timesTamp = timesTamp;
	}

	public Map<String, String> getTYPE() {
		return TYPE;
	}

	public void setTYPE(Map<String, String> tYPE) {
		TYPE = tYPE;
	}

}
