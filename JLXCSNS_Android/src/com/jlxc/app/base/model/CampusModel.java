package com.jlxc.app.base.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class CampusModel {

	// 校园动态列表
	private List<NewsModel> campusNewsList;
	// 校园动态列表
	private List<SchoolPersonModel> personList;

	@SuppressWarnings("unchecked")
	public void setContentWithJson(JSONObject object) {
		// 校园动态的转换
		List<JSONObject> JNewsObj = (List<JSONObject>) object.get("list");
		List<NewsModel> newsList = new ArrayList<NewsModel>();
		for (JSONObject newsObject : JNewsObj) {
			NewsModel newsTemp = new NewsModel();
			newsTemp.setContentWithJson(newsObject);
			newsList.add(newsTemp);
		}
		setCampusNewsList(newsList);

		// 校园的人转换
		List<JSONObject> JPersonObj = (List<JSONObject>) object.get("info");
		List<SchoolPersonModel> personList = new ArrayList<SchoolPersonModel>();
		for (JSONObject personObject : JPersonObj) {
			SchoolPersonModel personTemp = new SchoolPersonModel();
			personTemp.setContentWithJson(personObject);
			personList.add(personTemp);
		}
		setPersonList(personList);
	}

	public List<NewsModel> getCampusNewsList() {
		return campusNewsList;
	}

	public void setCampusNewsList(List<NewsModel> campusNewsList) {
		this.campusNewsList = campusNewsList;
	}

	public List<SchoolPersonModel> getPersonList() {
		return personList;
	}

	public void setPersonList(List<SchoolPersonModel> personList) {
		this.personList = personList;
	}

}
