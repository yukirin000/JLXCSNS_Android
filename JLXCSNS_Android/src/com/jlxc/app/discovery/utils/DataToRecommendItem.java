package com.jlxc.app.discovery.utils;

import java.util.LinkedList;
import java.util.List;

import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.discovery.model.PersonModel;
import com.jlxc.app.discovery.model.RecommendItemData;
import com.jlxc.app.discovery.model.RecommendItemData.RecommendInfoItem;
import com.jlxc.app.discovery.model.RecommendItemData.RecommendPhotoItem;

public class DataToRecommendItem {
	// 将数据转换为item形式的数据
	public static List<RecommendItemData> dataToItems(
			List<PersonModel> personList) {
		LinkedList<RecommendItemData> itemList = new LinkedList<RecommendItemData>();
		for (PersonModel personMd : personList) {
			itemList.add(createRecommendInfo(personMd));
			if (personMd.getImageList().size() > 0) {
				itemList.add(createPhotoItem(personMd));
			}
		}
		return itemList;
	}

	// 提取基本信息
	private static RecommendItemData createRecommendInfo(PersonModel person) {
		RecommendInfoItem item = new RecommendInfoItem();
		try {
			item.setItemType(RecommendItemData.RECOMMEND_TITLE);

			item.setUserID(person.getUerId());
			item.setUserName(person.getUserName());
			item.setUserSchool(person.getUserSchool());
			item.setHeadImage(person.getHeadImage());
			item.setHeadSubImage(person.getHeadSubImage());
			item.setAdd("0");
			item.setRelationTag(person.getType());
		} catch (Exception e) {
			LogUtils.e("createNewsTitle error.");
		}
		return item;
	}

	// 提取照片信息
	private static RecommendItemData createPhotoItem(PersonModel personMd) {
		RecommendPhotoItem item = new RecommendPhotoItem();
		item.setPhotoUrl(personMd.getImageList());
		return item;
	}
}
