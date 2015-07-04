package com.jlxc.app.base.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.ui.fragment.FragmentPage1;
import com.jlxc.app.base.ui.fragment.FragmentPage2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity implements OnClickListener{

	//FragmentTabHost对象
	private FragmentTabHost mTabHost;
	
	private LayoutInflater layoutInflater;
		
	private Class<?> fragmentArray[] = {FragmentPage1.class,FragmentPage2.class};
	
	private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn};
	
	private String mTextviewArray[] = {"主页", "哈哈"};
	
	
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

	@Override
	public void onClick(View v) {
	}
	
	/**
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
	 * 
	 * @param intent
	 */
	public void startActivityWithRight(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/**
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
