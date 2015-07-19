package com.jlxc.app.personal.ui.activity;

import android.content.Intent;
import android.view.View;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonalSettingActivity extends BaseActivityWithTopBar{

	@OnClick({R.id.account_btn})
	private void clickMethod(View view) {
		switch (view.getId()) {
		case R.id.account_btn:
			Intent intent = new Intent(this, AccountSettingActivity.class);
			startActivityWithRight(intent);
			break;
		default:
			break;
		}
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_personal_setting;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		
	}

}
