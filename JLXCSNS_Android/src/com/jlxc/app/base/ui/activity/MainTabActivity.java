package com.jlxc.app.base.ui.activity;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.demo.ui.fragment.FragmentPage1;
import com.jlxc.app.personal.ui.fragment.PersonalFragment;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainTabActivity extends BaseActivity {

	// FragmentTabHost对象
	@ViewInject(android.R.id.tabhost)
	private FragmentTabHost mTabHost;

	private LayoutInflater layoutInflater;

	private Class<?> fragmentArray[] = { FragmentPage1.class,
			PersonalFragment.class };

	private int mImageViewArray[] = { R.drawable.tab_home_btn,
			R.drawable.tab_message_btn };

	private String mTextviewArray[] = { "主页", "我" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initTab();
	}

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

	}
}
