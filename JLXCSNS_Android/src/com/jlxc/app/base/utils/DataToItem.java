package com.jlxc.app.base.utils;

import java.util.LinkedList;
import java.util.List;

import com.jlxc.app.base.model.CampusModel;
import com.jlxc.app.base.model.ItemModel;
import com.jlxc.app.base.model.ItemModel.BodyItem;
import com.jlxc.app.base.model.ItemModel.LikeListItem;
import com.jlxc.app.base.model.ItemModel.OperateItem;
import com.jlxc.app.base.model.ItemModel.CommentItem;
import com.jlxc.app.base.model.ItemModel.TitleItem;
import com.jlxc.app.base.model.ItemModel.CampusHeadItem;
import com.jlxc.app.base.model.NewsModel;
import com.jlxc.app.base.model.SchoolPersonModel;

/**
 * 将新闻的数据进行转换
 * */
public class DataToItem {
	// 将数据转换为动态item形式的数据
	public static List<ItemModel> newsDataToItems(List<NewsModel> orgDataList) {
		LinkedList<ItemModel> itemList = new LinkedList<ItemModel>();
		for (NewsModel newsMd : orgDataList) {
			itemList.add(createTitle(newsMd));
			itemList.add(createBody(newsMd));
			itemList.add(createOperate(newsMd));
			itemList.add(createLikeList(newsMd));
			itemList.add(createComment(newsMd));
		}
		return itemList;
	}

	// 将数据转换为校园item形式的数据
	public static List<ItemModel> campusDataToItems(List<NewsModel> newsData,
			List<SchoolPersonModel> personData) {
		LinkedList<ItemModel> itemList = new LinkedList<ItemModel>();
		if (personData.size() > 0) {
			itemList.addFirst(createCampusHead(personData));
		}

		for (NewsModel newsMd : newsData) {
			itemList.add(createTitle(newsMd));
			itemList.add(createBody(newsMd));
			itemList.add(createOperate(newsMd));
			itemList.add(createLikeList(newsMd));
		}
		return itemList;
	}

	// 提取新闻中的头部信息
	private static ItemModel createTitle(NewsModel news) {
		TitleItem item = new TitleItem();
		try {
			item.setItemType(ItemModel.TITLE);

			item.setNewsID(news.getNewsID());
			item.setHeadImage(news.getUserHeadImage());
			item.setHeadSubImage(news.getUserHeadSubImage());
			item.setUserName(news.getUserName());
			item.setSendTime(news.getSendTime());
			item.setUserTag(news.getUserSchool());
		} catch (Exception e) {
			LogUtils.e("createTitle error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的操作信息
	private static ItemModel createOperate(NewsModel news) {
		OperateItem item = new OperateItem();
		try {
			item.setItemType(ItemModel.OPERATE);

			item.setNewsID(news.getNewsID());
			item.setIsLike(news.getIsLike());
			item.setReplyCount(news.getCommentQuantity());
			item.setLikeCount(news.getLikeQuantity());
		} catch (Exception e) {
			LogUtils.e("createOperate error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的主题信息
	private static ItemModel createBody(NewsModel news) {
		BodyItem item = new BodyItem();
		try {
			item.setItemType(ItemModel.BODY);

			item.setNewsID(news.getNewsID());
			item.setNewsContent(news.getNewsContent());
			item.setImageNewsList(news.getImageNewsList());
			item.setLocation(news.getLocation());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的点赞信息
	private static ItemModel createLikeList(NewsModel news) {
		LikeListItem item = new LikeListItem();
		try {
			item.setItemType(ItemModel.LIKELIST);

			item.setNewsID(news.getNewsID());
			item.setLikeHeadListimage(news.getLikeHeadListimage());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的评论信息
	private static ItemModel createComment(NewsModel news) {
		CommentItem item = new CommentItem();
		try {
			item.setNewsID(news.getNewsID());
			item.setItemType(ItemModel.COMMENT);
			item.setCommentList(news.getCommentList());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的评论信息
	private static ItemModel createCampusHead(List<SchoolPersonModel> personData) {
		CampusHeadItem item = new CampusHeadItem();
		try {
			item.setItemType(ItemModel.CAMPUS_HEAD);
			// 校园头部动态ID
			item.setPersonList(personData);
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}
}
