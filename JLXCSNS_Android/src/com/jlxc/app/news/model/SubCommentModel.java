package com.jlxc.app.news.model;

import com.alibaba.fastjson.JSONObject;

public class SubCommentModel {

	private String subID;
	private String commentName;
	private String topCommentId;
	private String replyUid;
	private String replyCommentId;
	private String replyName;
	private String addData;
	private String UserId;
	private String commentContent;

	// 内容注入
	public void setContentWithJson(JSONObject object) {
		setSubID(object.getString("id"));
		setCommentName(object.getString("name"));
		setTopCommentId(object.getString("top_comment_id"));
		setReplyUid(object.getString("reply_uid"));
		setReplyCommentId(object.getString("reply_comment_id"));
		setReplyName(object.getString("reply_name"));
		setAddData(object.getString("add_date"));
		setUserId(object.getString("user_id"));
		setCommentContent(object.getString("comment_content"));
	}

	public String getSubID() {
		return subID;
	}

	public void setSubID(String subID) {
		this.subID = subID;
	}

	public String getCommentName() {
		return commentName;
	}

	public void setCommentName(String commentName) {
		this.commentName = commentName;
	}

	public String getTopCommentId() {
		return topCommentId;
	}

	public void setTopCommentId(String topCommentId) {
		this.topCommentId = topCommentId;
	}

	public String getReplyUid() {
		return replyUid;
	}

	public void setReplyUid(String replyUid) {
		this.replyUid = replyUid;
	}

	public String getReplyCommentId() {
		return replyCommentId;
	}

	public void setReplyCommentId(String replyCommentId) {
		this.replyCommentId = replyCommentId;
	}

	public String getReplyName() {
		return replyName;
	}

	public void setReplyName(String replyName) {
		this.replyName = replyName;
	}

	public String getAddData() {
		return addData;
	}

	public void setAddData(String addData) {
		this.addData = addData;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
}
