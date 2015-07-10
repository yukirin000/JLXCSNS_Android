package com.jlxc.app.base.utils;

import java.util.LinkedList;
import java.util.List;

import com.jlxc.app.base.model.NewsItemModel;
import com.jlxc.app.base.model.NewsItemModel.BodyItem;
import com.jlxc.app.base.model.NewsItemModel.LikeListItem;
import com.jlxc.app.base.model.NewsItemModel.OperateItem;
import com.jlxc.app.base.model.NewsItemModel.CommentItem;
import com.jlxc.app.base.model.NewsItemModel.TitleItem;
import com.jlxc.app.base.model.NewsModel;

/**
 * 将新闻的数据进行转换
 * */
public class NewsToItem {
	public static List<NewsItemModel> newsToItems(List<NewsModel> orgDataList) {
		LinkedList<NewsItemModel> itemList = new LinkedList<NewsItemModel>();
		for (NewsModel newsMd : orgDataList) {
			itemList.add(createTitle(newsMd));
			itemList.add(createBody(newsMd));
			itemList.add(createOperate(newsMd));
			itemList.add(createLikeList(newsMd));
			itemList.add(createComment(newsMd));
		}
		return itemList;
	}

	// 提取新闻中的头部信息
	private static NewsItemModel createTitle(NewsModel news) {
		TitleItem item = new TitleItem();
		try {
			item.setItemType(NewsItemModel.TITLE);

			item.setHeadImage(news.getUserHeadImage());
			item.setHeadSubImage(news.getUserHeadSubImage());
			item.setUserName(news.getUserName());
			item.setSendTime(news.getSendTime());
			item.setUserTag(news.getUserSchool());
		} catch (Exception e) {
			LogUtils.e("createTitle error.");
		}
		return (NewsItemModel) item;
	}

	// 提取新闻中的操作信息
	private static NewsItemModel createOperate(NewsModel news) {
		OperateItem item = new OperateItem();
		try {
			item.setItemType(NewsItemModel.OPERATE);

			item.setReplyCount(news.getCommentQuantity());
			item.setLikeCount(news.getLikeQuantity());
		} catch (Exception e) {
			LogUtils.e("createOperate error.");
		}
		return (NewsItemModel) item;
	}

	// 提取新闻中的主题信息
	private static NewsItemModel createBody(NewsModel news) {
		BodyItem item = new BodyItem();
		try {
			item.setItemType(NewsItemModel.BODY);

			item.setNewsContent(news.getNewsContent());
			item.setImageNewsList(news.getImageNewsList());
			item.setLocation(news.getLocation());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (NewsItemModel) item;
	}

	// 提取新闻中的点赞信息
	private static NewsItemModel createLikeList(NewsModel news) {
		LikeListItem item = new LikeListItem();
		try {
			item.setItemType(NewsItemModel.LIKELIST);

			item.setLikeHeadListimage(news.getLikeHeadListimage());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (NewsItemModel) item;
	}

	// 提取新闻中的评论信息
	private static NewsItemModel createComment(NewsModel news) {
		CommentItem item = new CommentItem();
		try {
			item.setItemType(NewsItemModel.COMMENT);
			item.setCommentList(news.getCommentList());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (NewsItemModel) item;
	}
}
