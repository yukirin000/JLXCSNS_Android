package com.jlxc.app.group.utils;

import java.util.LinkedList;
import java.util.List;

import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.group.model.GroupItemModel;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsBodyItem;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsOperateItem;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsTitleItem;
import com.jlxc.app.news.model.NewsModel;
/**
 * 将数据转换成items类型的数据
 * */
public class NewsToItemData {

	// 将数据转换为动态item形式的数据
	public static List<GroupItemModel> newsToItem(
			List<NewsModel> orgDataList) {
		LinkedList<GroupItemModel> itemList = new LinkedList<GroupItemModel>();
		for (NewsModel newsMd : orgDataList) {
			itemList.add(createTitle(newsMd, GroupItemModel.GROUP_TITLE));
			itemList.add(createBody(newsMd, GroupItemModel.GROUP_BODY));
			itemList.add(createOperate(newsMd, GroupItemModel.GROUP_OPERATE));
		}
		return itemList;
	}

	// 提取新闻中的头部信息
	private static GroupItemModel createTitle(NewsModel news, int Type) {
		GroupNewsTitleItem item = new GroupNewsTitleItem();
		try {
			item.setItemType(Type);

			item.setNewsID(news.getNewsID());
			item.setUserName(news.getUserName());
			item.setUserID(news.getUid());
			item.setSchool(news.getUserSchool());
			item.setUserHeadImage(news.getUserHeadImage());
			item.setUserSubHeadImage(news.getUserHeadSubImage());
		} catch (Exception e) {
			LogUtils.e("createNewsTitle error.");
		}
		return item;
	}

	// 提取新闻中的主体信息
	private static GroupItemModel createBody(NewsModel news, int Type) {
		GroupNewsBodyItem item = new GroupNewsBodyItem();
		try {
			item.setItemType(Type);

			item.setNewsID(news.getNewsID());
			item.setNewsContent(news.getNewsContent());
			item.setImageNewsList(news.getImageNewsList());
			item.setLocation(news.getLocation());
		} catch (Exception e) {
			LogUtils.e("createBody error.");
		}
		return (GroupItemModel) item;
	}

	// 提取新闻中的操作信息
	private static GroupItemModel createOperate(NewsModel news, int Type) {
		GroupNewsOperateItem item = new GroupNewsOperateItem();
		try {
			item.setItemType(Type);

			item.setNewsID(news.getNewsID());
			item.setIsLike(news.getIsLike());
			item.setReplyCount(news.getCommentQuantity());
			item.setLikeCount(news.getLikeQuantity());
			item.setTime(news.getSendTime());
		} catch (Exception e) {
			LogUtils.e("createOperate error.");
		}
		return (GroupItemModel) item;
	}

}
