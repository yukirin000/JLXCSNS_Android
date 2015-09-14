package com.jlxc.app.news.ui.activity;

import android.content.Intent;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.group.ui.fragment.CampusNewsFragment;

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
		Intent intent = getIntent();
		
		if (intent.hasExtra(CampusNewsFragment.INTENT_SCHOOL_CODE_KEY)) {
			String schoolCode = intent.getStringExtra(CampusNewsFragment.INTENT_SCHOOL_CODE_KEY);
			if (!schoolCode.equals(UserManager.getInstance().getUser().getSchool_code())) {
				setBarText("别人学校中发生的事~");		
			}
		}
		
	}

}
