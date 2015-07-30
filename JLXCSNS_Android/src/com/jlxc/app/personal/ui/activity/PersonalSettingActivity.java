package com.jlxc.app.personal.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.NewVersionCheckManager;
import com.jlxc.app.base.manager.NewVersionCheckManager.VersionCallBack;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.DataCleanManager;
import com.jlxc.app.base.utils.HttpCacheUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonalSettingActivity extends BaseActivityWithTopBar{

	@ViewInject(R.id.clear_btn)
	private Button clearButton;
	
	@OnClick({R.id.account_btn,R.id.clear_btn,R.id.check_btn})
	private void clickMethod(View view) {
		switch (view.getId()) {
			//退出
		case R.id.account_btn:
			Intent intent = new Intent(this, AccountSettingActivity.class);
			startActivityWithRight(intent);
			break;
			//清除内存
		case R.id.clear_btn:
			clearCache();
			break;
			//版本检查
		case R.id.check_btn:
			checkVersion();
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
		try {
			String cacheString = DataCleanManager.getTotalCacheSize(this);
			clearButton.setText("清除缓存"+cacheString);			
		} catch (Exception e) {
		}
	}

	//清除缓存
	private void clearCache(){
		try {
			DataCleanManager.clearAllCache(this);
			ToastUtil.show(this, "清除成功^_^");
			String cacheString = DataCleanManager.getTotalCacheSize(this);
			//清除缓存
			HttpCacheUtils.clearHttpCache();
			clearButton.setText("清除缓存"+cacheString);	
		} catch (Exception e) {
			ToastUtil.show(this, "清除成功>_<");
			e.printStackTrace();
		}
	}
	
	//检测版本
	private void checkVersion() {
		
		showLoading("版本检测中....", true);
		new NewVersionCheckManager(this, this).checkNewVersion(true, new VersionCallBack() {
			@Override
			public void finish() {
				hideLoading();
			}
		});
	} 
}
