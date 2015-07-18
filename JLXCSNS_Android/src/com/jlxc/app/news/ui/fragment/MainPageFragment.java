package com.jlxc.app.news.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class MainPageFragment extends BaseFragment {

	// 上下文信息
	private Context mContext;
	// 主页viewpager
	@ViewInject(R.id.viewpager_main)
	private ViewPager mainPager;
	// 主页viewpager
	@ViewInject(R.id.tv_news_guid)
	private TextView newsTitleTextView;
	// 校园
	@ViewInject(R.id.tv_campus_guid)
	private TextView campusTitleTextView;
	// 所有的
	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	// 偏移图片
	@ViewInject(R.id.img_cursor)
	private ImageView imageCursor;
	// 发布按钮
	@ViewInject(R.id.img_publish_news)
	private ImageView imagePublish;
	// 当前页卡编号
	private int currIndex;
	// 横线图片宽度
	private int cursorWidth;
	// 图片移动的偏移量
	private int offset;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_main_page;
	}

	@Override
	public void loadLayout(View rootView) {
		init();
		InitImage();
		InitViewPager();
	}

	@Override
	public void setUpViews(View rootView) {

	}

	/**
	 * 初始化函数
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
		LogUtils.i("screenWidth=" + screenWidth + " screenHeight="
				+ screenHeight);
	}

	/*
	 * 初始化图片的位移像素
	 */
	public void InitImage() {
		cursorWidth = 60;
		int cursorheight = 10;
		int publishBtnWidth = 60;
		// 设置发布按钮的尺寸
		LayoutParams pbtnParams = (LayoutParams) imagePublish.getLayoutParams();
		pbtnParams.width = publishBtnWidth;
		pbtnParams.height = publishBtnWidth;
		imagePublish.setLayoutParams(pbtnParams);
		imagePublish.setScaleType(ImageView.ScaleType.FIT_XY);

		offset = ((screenWidth - publishBtnWidth) / 2 - cursorWidth) / 2;
		// 设置游标的尺寸与位置
		LayoutParams cursorParams = (LayoutParams) imageCursor
				.getLayoutParams();
		cursorParams.width = cursorWidth;
		cursorParams.height = cursorheight;
		cursorParams.leftMargin = offset;
		imageCursor.setLayoutParams(cursorParams);
		imageCursor.setScaleType(ImageView.ScaleType.FIT_XY);
	}

	/*
	 * 初始化ViewPager
	 */
	public void InitViewPager() {
		// 获取分辨率宽度
		int TitleViewWisth = screenWidth / 4;

		newsTitleTextView.setWidth(TitleViewWisth);
		campusTitleTextView.setWidth(TitleViewWisth);
		newsTitleTextView.setOnClickListener(new ViewClickListener(0));
		campusTitleTextView.setOnClickListener(new ViewClickListener(1));

		mFragmentList.add(new NewsListFragment());
		mFragmentList.add(new CampusFragment());

		mainPager.setAdapter(new MainFragmentPagerAdapter(getFragmentManager(),
				mFragmentList));
		mainPager.setCurrentItem(0);
		mainPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 头标点击监听
	 */
	private class ViewClickListener implements OnClickListener {
		private int index = 0;

		public ViewClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			mainPager.setCurrentItem(index);
		}
	}

	/**
	 * ViewPager的适配器
	 * */
	class MainFragmentPagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> fragmentList;

		public MainFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
			super(fm);
			fragmentList = list;
		}

		@Override
		public Fragment getItem(int index) {
			return fragmentList.get(index);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}
	}

	/**
	 * 监听选项卡改变事件
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + cursorWidth;// 页卡1 -> 页卡2 偏移量
		float lastpostion = 0;

		public void onPageScrollStateChanged(int index) {
			currIndex = index;
		}

		// CurrentTab:当前页面序号
		// OffsetPercent:当前页面偏移的百分比
		// offsetPixel:当前页面偏移的像素位置
		public void onPageScrolled(int CurrentTab, float OffsetPercent,
				int offsetPixel) {
			// 下标的移动动画
			Animation animation = new TranslateAnimation(one * lastpostion, one
					* (CurrentTab + OffsetPercent), 0, 0);

			lastpostion = OffsetPercent + CurrentTab;
			// True:图片停在动画结束位置
			animation.setFillAfter(true);
			animation.setDuration(300);
			imageCursor.startAnimation(animation);
		}

		public void onPageSelected(int arg0) {

		}
	}
}
