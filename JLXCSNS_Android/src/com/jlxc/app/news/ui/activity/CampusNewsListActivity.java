package com.jlxc.app.news.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;

public class CampusNewsListActivity extends BaseActivityWithTopBar {

	//intent key 
//	public static String SCHOOL_CODE = "schoolCode";
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_campus_news_list;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		setBarText("学校中发生的事~");
	}

}
