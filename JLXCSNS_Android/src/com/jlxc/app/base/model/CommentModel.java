package com.jlxc.app.base.model;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.utils.JLXCConst;

public class CommentModel {

	// 评论的id
	private String commentID;
	// 评论者的名字
	private String submitterName;
	// 评论者的头像全图
	private String headImage;
	// 评论者的头像缩略图
	private String headSubImage;
	// 发布的日期
	private String addDate;
	// 发布者的id
	private String userId;
	// 评论的内容
	private String commentContent;
	// 评论的点赞量
	private String likeQuantity;

	// 内容注入
	public void setContentWithJson(JSONObject object) {
		setCommentID(object.getString("id"));
		setSubmitterName(object.getString("name"));
		setHeadImage(JLXCConst.ATTACHMENT_ADDR + object.getString("head_image"));
		setHeadSubImage(JLXCConst.ATTACHMENT_ADDR
				+ object.getString("head_sub_image"));
		setAddDate(object.getString("add_date"));
		setUserId(object.getString("user_id"));
		setCommentContent(object.getString("comment_content"));
		setLikeQuantity(object.getString("like_quantity"));
	}

	public String getCommentID() {
		return commentID;
	}

	public void setCommentID(String commentID) {
		this.commentID = commentID;
	}

	public String getSubmitterName() {
		return submitterName;
	}

	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}

	public String getHeadImage() {
		return headImage;
	}

	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}

	public String getAddDate() {
		return addDate;
	}

	public void setAddDate(String addDate) {
		this.addDate = addDate;
	}

	public String getHeadSubImage() {
		return headSubImage;
	}

	public void setHeadSubImage(String headSubImage) {
		this.headSubImage = headSubImage;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public String getLikeQuantity() {
		return likeQuantity;
	}

	public void setLikeQuantity(String likeQuantity) {
		this.likeQuantity = likeQuantity;
	}

}
