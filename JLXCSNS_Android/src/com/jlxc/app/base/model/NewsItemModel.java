package com.jlxc.app.base.model;

import java.util.ArrayList;
import java.util.List;

import com.jlxc.app.base.utils.LogUtils;

/**
 * 将每条动态拆分成五个部分
 * */
public class NewsItemModel {

	// item的种类数
	public static final int TYPE_COUNT = 5;
	// 表示头部
	public static final int TITLE = 0;
	// 表示动态主体
	public static final int BODY = 1;
	// 表示互动操作相关（评论、点赞等）
	public static final int OPERATE = 2;
	// 点赞部分
	public static final int LIKELIST = 3;
	// 评论部分
	public static final int REPLY = 4;
	// 当前的item类型
	private int styleType;

	public int getItemType() {
		return styleType;
	}

	/**
	 * 设置item的类型
	 * */
	public void setItemType(int type) {
		switch (type) {
		case NewsItemModel.TITLE:
		case NewsItemModel.BODY:
		case NewsItemModel.OPERATE:
		case NewsItemModel.LIKELIST:
		case NewsItemModel.REPLY:
			styleType = type;
			break;

		default:
			LogUtils.e("items type error");
			break;
		}
	}

	/**
	 * 动态的头部
	 * */
	public static class TitleItem extends NewsItemModel {

		// 动态发布者的头像缩略图
		private String headSubImage;
		// 动态发布者的头像
		private String headImage;
		// 动态发布者的名字
		private String userName;
		// 发布的时间
		private String sendTime;
		// 显示的标签
		private String userTag;

		public String getHeadSubImage() {
			return headSubImage;
		}

		public void setHeadSubImage(String headSubImage) {
			this.headSubImage = headSubImage;
		}

		public String getHeadImage() {
			return headImage;
		}

		public void setHeadImage(String userHeadImage) {
			this.headImage = userHeadImage;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userNameStr) {
			this.userName = userNameStr;
		}

		public String getSendTime() {
			return sendTime;
		}

		public void setSendTime(String sendTime) {
			this.sendTime = sendTime;
		}

		public String getUserTag() {
			return userTag;
		}

		public void setUserTag(String userTag) {
			this.userTag = userTag;
		}

	}

	/**
	 * 动态的主体
	 * */
	public static class BodyItem extends NewsItemModel {

		// 动态的文字内容
		private String newsContent;
		// 动态的图片列表
		private List<ImageModel> newsImageList = new ArrayList<ImageModel>();
		// 发布的位置
		private String location;

		public String getNewsContent() {
			return newsContent;
		}

		public void setNewsContent(String newsContent) {
			this.newsContent = newsContent;
		}

		public List<ImageModel> getNewsImageListList() {
			return newsImageList;
		}

		public void setImageNewsList(List<ImageModel> imageList) {
			this.newsImageList = imageList;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

	}

	/**
	 * 动态的操作部分
	 * */
	public static class OperateItem extends NewsItemModel {

		// 评论数
		private String replyCount;
		// 点赞数
		private String likeCount;
		// 是否已赞
		private boolean isLike = false;

		public String getReplyCount() {
			return replyCount;
		}

		public void setReplyCount(String replyCount) {
			this.replyCount = replyCount;
		}

		public String getLikeCount() {
			return likeCount;
		}

		public void setLikeCount(String likeCount) {
			this.likeCount = likeCount;
		}

		public boolean getIsLike() {
			return isLike;
		}

		public void setIsLike(String isLike) {
			if (isLike.equals("1")) {
				this.isLike = true;
			} else {
				this.isLike = false;
			}
		}
	}

	/**
	 * 已赞的人的头像部分
	 * */
	public static class LikeListItem extends NewsItemModel {

		// 点赞的人头像
		private List<LikeModel> likeList = new ArrayList<LikeModel>();

		public List<LikeModel> getLikeHeadListimage() {
			return likeList;
		}

		public void setLikeHeadListimage(List<LikeModel> list) {
			this.likeList = list;
		}
	}

	/**
	 * 评论列表部分
	 * */
	public static class ReplyItem extends NewsItemModel {

		// 评论列表
		private List<CommentModel> replyList = new ArrayList<CommentModel>();// 评论列表

		public List<CommentModel> getReplyList() {
			return replyList;
		}

		public void setReplyList(List<CommentModel> replyList) {
			this.replyList = replyList;
		}
	}

}
