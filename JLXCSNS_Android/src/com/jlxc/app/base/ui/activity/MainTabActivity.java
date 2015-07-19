package com.jlxc.app.base.ui.activity;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ErrorCode;

import com.jlxc.app.R;
import com.jlxc.app.base.helper.RongCloudEvent;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.login.ui.activity.LoginActivity;
import com.jlxc.app.message.ui.fragment.MessageMainFragment;
import com.jlxc.app.news.ui.fragment.CampusFragment;
import com.jlxc.app.news.ui.fragment.MainPageFragment;
import com.jlxc.app.news.ui.fragment.NewsListFragment;
import com.jlxc.app.personal.ui.activity.AccountSettingActivity;
import com.jlxc.app.personal.ui.fragment.PersonalFragment;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class MainTabActivity extends BaseActivity {

	// FragmentTabHost对象
	@ViewInject(android.R.id.tabhost)
	private FragmentTabHost mTabHost;

	private LayoutInflater layoutInflater;

	private Class<?> fragmentArray[] = { MainPageFragment.class, MessageMainFragment.class,
			PersonalFragment.class };

	private int mImageViewArray[] = { R.drawable.tab_home_btn,R.drawable.tab_home_btn,
			R.drawable.tab_message_btn };

	private String mTextviewArray[] = { "主页", "消息", "我" };
	
	public void initTab() {

		layoutInflater = LayoutInflater.from(this);

		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i])
					.setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}
	}
	
	//初始化融云
	private void initRong(){
		String token = "";
		UserModel userModel = UserManager.getInstance().getUser();
		if (null != userModel.getIm_token() && userModel.getIm_token().length()>0) {
			token = userModel.getIm_token();
		}
		RongIM.connect(token, new ConnectCallback() {

			@Override 
			public void onError(ErrorCode arg0) {
				Toast.makeText(MainTabActivity.this, "connect onError", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess(String arg0) {
				Toast.makeText(MainTabActivity.this, "connect onSuccess", Toast.LENGTH_SHORT).show();
				RongCloudEvent.getInstance().setOtherListener();
			}

			@Override
			public void onTokenIncorrect() {
				Toast.makeText(MainTabActivity.this, "token error", Toast.LENGTH_SHORT).show();
			}

		});
	}

	/**
	 */
	@SuppressLint("InflateParams")
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);

		return view;
	}

	@Override
	public int setLayoutId() {
		return R.layout.activity_main;
	}

	@Override
	protected void loadLayout(View v) {

	}

	@Override
	protected void setUpView() {
		initTab();
		initRong();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
	        alterDialog.setMessage("确定退出该账号吗？");
	        alterDialog.setCancelable(true);

	        alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                if (RongIM.getInstance() != null)
	                    RongIM.getInstance().logout();
	                ActivityManager.getInstence().exitApplication();
	                killThisPackageIfRunning(MainTabActivity.this, "com.jlxc.app");
	                Process.killProcess(Process.myPid());
	            }
	        });
	        alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.cancel();
	            }
	        });
	        alterDialog.show();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
	
	public static void killThisPackageIfRunning(final Context context, String packageName) {
        android.app.ActivityManager activityManager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
    }
}
