package com.jlxc.app.base.model;

import java.util.ArrayList;
import java.util.List;

import com.jlxc.app.base.utils.LogUtils;

/**
 * 将每条动态拆分成五个部分
 * */
public class ItemModel {

	// 动态item的种类数
	public static final int NEWS_ITEM_TYPE_COUNT = 5;
	// 校园item的种类数
	public static final int CAMPUS_ITEM_TYPE_COUNT = 5;
	// 表示头部
	public static final int TITLE = 0;
	// 表示动态主体
	public static final int BODY = 1;
	// 表示互动操作相关（评论、点赞等）
	public static final int OPERATE = 2;
	// 点赞部分
	public static final int LIKELIST = 3;
	// 评论部分
	public static final int COMMENT = 4;
	// 校园的头部
	public static final int CAMPUS_HEAD = 4;
	// 动态的id
	private String newsID = "";
	// 当前的item类型
	private int itemType;

	public int getItemType() {
		return itemType;
	}

	/**
	 * 设置item的类型
	 * */
	public void setItemType(int type) {
		switch (type) {
		case ItemModel.TITLE:
		case ItemModel.BODY:
		case ItemModel.OPERATE:
		case ItemModel.LIKELIST:
		case ItemModel.COMMENT:
			itemType = type;
			break;

		default:
			LogUtils.e("items type error");
			break;
		}
	}

	public String getNewsID() {
		return newsID;
	}

	public void setNewsID(String newsID) {
		this.newsID = newsID;
	}

	/**
	 * 动态的头部
	 * */
	public static class TitleItem extends ItemModel {

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
	public static class BodyItem extends ItemModel {

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
	public static class OperateItem extends ItemModel {

		// 评论数
		private int replyCount;
		// 点赞数
		private int likeCount;
		// 是否已赞
		private boolean isLike = false;

		public int getReplyCount() {
			return replyCount;
		}

		public void setReplyCount(String replyCount) {
			try {
				this.replyCount = Integer.parseInt(replyCount);
			} catch (Exception e) {
				LogUtils.e("评论数据格式错误.");
			}
		}

		public int getLikeCount() {
			return likeCount;
		}

		public void setLikeCount(String likeCount) {
			try {
				this.likeCount = Integer.parseInt(likeCount);
			} catch (Exception e) {
				LogUtils.e("点赞数据格式错误.");
			}
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
	public static class LikeListItem extends ItemModel {

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
	public static class CommentItem extends ItemModel {

		// 评论列表
		private List<CommentModel> commentList = new ArrayList<CommentModel>();// 评论列表

		public List<CommentModel> getCommentList() {
			return commentList;
		}

		public void setCommentList(List<CommentModel> cmtList) {
			this.commentList = cmtList;
		}
	}

	/**
	 * 校园的头部
	 * */
	public static class CampusHeadItem extends ItemModel {

		private String schoolName;
		// 学校的人列表
		private List<SchoolPersonModel> personList = new ArrayList<SchoolPersonModel>();

		public List<SchoolPersonModel> getPersonList() {
			return personList;
		}

		public void setPersonList(List<SchoolPersonModel> List) {
			this.personList = List;
		}

		public String getSchoolName() {
			return schoolName;
		}

		public void setSchoolName(String schoolName) {
			this.schoolName = schoolName;
		}
	}

}
