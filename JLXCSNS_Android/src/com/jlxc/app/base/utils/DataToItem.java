package com.jlxc.app.base.utils;

import java.util.LinkedList;
import java.util.List;

import com.jlxc.app.news.model.CampusPersonModel;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CampusHeadItem;
import com.jlxc.app.news.model.ItemModel.CommentItem;
import com.jlxc.app.news.model.ItemModel.CommentListItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.OperateItem;
import com.jlxc.app.news.model.ItemModel.SubCommentItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.SubCommentModel;

/**
 * 将新闻的数据进行转换
 * */
public class DataToItem {
	// 将数据转换为动态item形式的数据
	public static List<ItemModel> newsDataToItems(List<NewsModel> orgDataList) {
		LinkedList<ItemModel> itemList = new LinkedList<ItemModel>();
		for (NewsModel newsMd : orgDataList) {
			itemList.add(createNewsTitle(newsMd));
			itemList.add(createBody(newsMd));
			itemList.add(createOperate(newsMd));
			itemList.add(createLikeList(newsMd));
			itemList.add(createCommentList(newsMd));
		}
		return itemList;
	}

	// 将数据转换为校园item形式的数据
	public static List<ItemModel> campusDataToItems(List<NewsModel> newsData,
			List<CampusPersonModel> personData) {
		LinkedList<ItemModel> itemList = new LinkedList<ItemModel>();
		if (personData.size() > 0) {
			itemList.addFirst(createCampusHead(personData));
		}

		for (NewsModel newsMd : newsData) {
			itemList.add(createNewsTitle(newsMd));
			itemList.add(createBody(newsMd));
			itemList.add(createOperate(newsMd));
			itemList.add(createLikeList(newsMd));
		}
		return itemList;
	}

	/**
	 * 将动态详细数据转换为item类型数据
	 * */
	public static List<ItemModel> newsDetailToItems(NewsModel newsData) {
		LinkedList<ItemModel> itemList = new LinkedList<ItemModel>();
		itemList.add(createNewsTitle(newsData));
		itemList.add(createBody(newsData));
		itemList.add(createLikeList(newsData));
		List<CommentModel> cmtList = newsData.getCommentList();
		for (CommentModel cmtMd : cmtList) {
			itemList.add(createComment(cmtMd));
			List<SubCommentModel> subCmtList = cmtMd.getSubCommentList();
			for (SubCommentModel subCmtMd : subCmtList) {
				itemList.add(createSubComment(subCmtMd, newsData.getNewsID()));
			}
		}
		return itemList;
	}

	// 提取新闻中的头部信息
	private static ItemModel createNewsTitle(NewsModel news) {
		TitleItem item = new TitleItem();
		try {
			item.setItemType(ItemModel.NEWS_TITLE);

			item.setNewsID(news.getNewsID());
			item.setHeadImage(news.getUserHeadImage());
			item.setHeadSubImage(news.getUserHeadSubImage());
			item.setUserName(news.getUserName());
			item.setSendTime(news.getSendTime());
			item.setUserTag(news.getUserSchool());
			item.setIsLike(news.getIsLike());
		} catch (Exception e) {
			LogUtils.e("createNewsTitle error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的操作信息
	private static ItemModel createOperate(NewsModel news) {
		OperateItem item = new OperateItem();
		try {
			item.setItemType(ItemModel.NEWS_OPERATE);

			item.setNewsID(news.getNewsID());
			item.setIsLike(news.getIsLike());
			item.setReplyCount(news.getCommentQuantity());
			item.setLikeCount(news.getLikeQuantity());
		} catch (Exception e) {
			LogUtils.e("createOperate error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的主体信息
	private static ItemModel createBody(NewsModel news) {
		BodyItem item = new BodyItem();
		try {
			item.setItemType(ItemModel.NEWS_BODY);

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
			item.setItemType(ItemModel.NEWS_LIKELIST);

			item.setNewsID(news.getNewsID());
			item.setLikeHeadListimage(news.getLikeHeadListimage());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的评论列表信息
	private static ItemModel createCommentList(NewsModel news) {
		CommentListItem item = new CommentListItem();
		try {
			item.setNewsID(news.getNewsID());
			item.setItemType(ItemModel.NEWS_COMMENT);
			item.setCommentList(news.getCommentList());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (ItemModel) item;
	}

	// 提取新闻中的评论信息
	public static ItemModel createComment(CommentModel cmt) {
		CommentItem item = new CommentItem();
		try {
			item.setItemType(ItemModel.NEWS_DETAIL_COMMENT);
			item.setComment(cmt);
		} catch (Exception e) {
			LogUtils.e("create comment error.");
		}
		return (ItemModel) item;
	}

	// 提取校园头部
	private static ItemModel createCampusHead(List<CampusPersonModel> personData) {
		CampusHeadItem item = new CampusHeadItem();
		try {
			item.setItemType(ItemModel.CAMPUS_HEAD);
			// 校园头部动态ID
			item.setPersonList(personData);
		} catch (Exception e) {
			LogUtils.e("create Campus Head error.");
		}
		return (ItemModel) item;
	}

	// 提取动态数据中的子评论信息
	public static ItemModel createSubComment(SubCommentModel cmt, String newsID) {
		SubCommentItem item = new SubCommentItem();
		try {
			item.setItemType(ItemModel.NEWS_DETAIL_SUB_COMMENT);
			item.setNewsID(newsID);
			item.setSubCommentModel(cmt);
		} catch (Exception e) {
			LogUtils.e("create subcomment item error.");
		}
		return (ItemModel) item;
	}
}
