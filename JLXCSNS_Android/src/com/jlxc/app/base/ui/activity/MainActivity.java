package com.jlxc.app.base.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.ui.fragment.FragmentPage1;
import com.jlxc.app.base.ui.fragment.FragmentPage2;
import com.jlxc.app.base.utils.SlideMenu;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity implements OnClickListener{

	//�?�?FragmentTabHost对象
	private FragmentTabHost mTabHost;
	
	//�?�?�?�?�?�?
	private LayoutInflater layoutInflater;
		
	//�?�???��????��?????Fragment??????
	private Class fragmentArray[] = {FragmentPage1.class,FragmentPage2.class};
	
	//�?�???��????��????��???????��??
	private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn};
	
	//Tab???项�?��?????�?
	private String mTextviewArray[] = {"�?�?", "�????", "好�??", "广�??", "??��??"};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstence().pushActivity(this);
        setContentView(R.layout.activity_main);
    	
		ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
		menuImg.setOnClickListener(this);
		initTab();

	}
    
    public void initTab() {
    	//�?�????�?�?对象
		layoutInflater = LayoutInflater.from(this);
				
		//�?�????TabHost对象�?�????TabHost
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);	
		
		//�????fragment???�????
		int count = fragmentArray.length;	
				
		for(int i = 0; i < count; i++){	
			//为�??�?�?Tab??????设置??��????????�???????�?
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			//�?Tab??????添�??�?Tab???项�?�中
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			//设置Tab???????????????
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}
	} 

	@Override
	public void onClick(View v) {
	}
	
	/**
	 * �?Tab??????设置??��????????�?
	 */
	private View getTabItemView(int index){
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
	
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);
		
		int i = mTabHost.getCurrentTab();
		
		
		
		return view;
	}
	

	/**
	 * ????????��?��?????
	 * 
	 * @param intent
	 */
	public void startActivityWithRight(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/**
	 * ????????��??��??�?
	 */
	public void finishWithRight() {
		ActivityManager.getInstence().popActivity(this);
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	public void next(View view) {
		Log.i("--", "fff");
		Intent intent = new Intent(this, NextActivity.class);
		startActivityWithRight(intent);
	}
}
