package com.jlxc.app.demo.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.JLXCBaseActivity;
import com.jlxc.app.demo.ui.fragment.FragmentPage1;
import com.jlxc.app.demo.ui.fragment.FragmentPage2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainActivity extends JLXCBaseActivity{

	//FragmentTabHost对象
	private FragmentTabHost mTabHost;
	
	private LayoutInflater layoutInflater;
		
	private Class<?> fragmentArray[] = {FragmentPage1.class,FragmentPage2.class};
	
	private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn};
	
	private String mTextviewArray[] = {"主页", "哈哈"};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//		ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
//		menuImg.setOnClickListener(this);
		initTab();

	}
    
    public void initTab() {
    	
		layoutInflater = LayoutInflater.from(this);
				
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);	
		
		int count = fragmentArray.length;	
				
		for(int i = 0; i < count; i++){	
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}
	} 
	
	/**
	 */
	@SuppressLint("InflateParams") private View getTabItemView(int index){
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
	
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);
		
//		int i = mTabHost.getCurrentTab();
		return view;
	}
	
	public void next(View view) {
		Log.i("--", "fff");
		Intent intent = new Intent(this, NextActivity.class);
		startActivityWithRight(intent);
	}

	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_main;
	}

	@Override
	protected void loadLayout(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		
	}
}
