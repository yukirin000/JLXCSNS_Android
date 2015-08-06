package com.jlxc.app.login.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.ui.activity.MainTabActivity;
import com.jlxc.app.base.utils.LogUtils;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

public class LaunchActivity extends BaseActivity {

	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_launch;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		    
		//如果后台有程序
		if (ActivityManager.getActivityStack().size()>1) {
			UserModel userModel = UserManager.getInstance().getUser();
			if (null != userModel.getUsername() && null != userModel.getLogin_token()) {
				startActivity(new Intent(LaunchActivity.this, MainTabActivity.class));
			} else {
				startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
			}
			finish();
			return;
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				UserModel userModel = UserManager.getInstance().getUser();
				if (null != userModel.getUsername() && null != userModel.getLogin_token()) {
					startActivity(new Intent(LaunchActivity.this, MainTabActivity.class));
				} else {
					startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
				}
				finish();
			}
		}, 2000);
	}

	@Override
	protected void loadLayout(View v) {
		// TODO Auto-generated method stub
		
	}

}
