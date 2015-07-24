package com.jlxc.app.base.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.view.PinchImageView;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.news.model.ImageModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

public class BigImgLookActivity extends BaseActivity {
	// 传递单张图片
	public static final String INTENT_KEY = "Single_Image";
	// 传递图片列表
	public static final String INTENT_KEY_IMG_LIST = "Url_Path_List";
	// 传递当前图片的位置
	public static final String INTENT_KEY_INDEX = "Current_Index";
	// 图片路径
	private List<String> imageUrlList = new ArrayList<String>();
	// 所有的imageview
	private List<PinchImageView> imageViewList = new ArrayList<PinchImageView>();
	// bitmap
	private BitmapUtils bitmapUtils;
	// 回调对象
	private BitmapLoadCallBack<ImageView> loadImageCallBack;
	//
	@ViewInject(R.id.viewpager_big_image)
	private ViewPager viewPager;
	// 提示性点点数组
	private List<ImageView> tipList = new ArrayList<ImageView>();
	// 当前展示的页码
	private int currentPage = 0;
	// 存放点点的容器
	@ViewInject(R.id.tipsBox)
	LinearLayout tipsBoxLayout;
	// viewpage适配器
	private BigImageAdapter bigImageAdapter;

	@SuppressWarnings("unchecked")
	private void init() {
		Intent intent = this.getIntent();
		if (intent.hasExtra(INTENT_KEY)) {
			// 传递的是单张图片
			if (intent.hasExtra(INTENT_KEY)) {
				imageUrlList.add(intent.getStringExtra(INTENT_KEY));
			} else {
				LogUtils.e("未传递图片地址");
			}
		} else {
			// 传递图片列表
			if (intent.hasExtra(INTENT_KEY_IMG_LIST)) {
				List<ImageModel> imgModList = (List<ImageModel>) intent
						.getSerializableExtra(INTENT_KEY_IMG_LIST);
				for (int index = 0; index < imgModList.size(); index++) {
					imageUrlList.add(imgModList.get(index).getURL());
				}
			} else {
				LogUtils.e("未传递图片地址");
			}
			if (intent.hasExtra(INTENT_KEY_INDEX)) {
				currentPage = intent.getIntExtra(INTENT_KEY_INDEX, 0);
			} else {
				LogUtils.e("未传递所点击图片的位置，默认为0.");
			}
		}

		BitmapManager bmpManager = BitmapManager.getInstance();
		bitmapUtils = bmpManager.getHeadPicBitmapUtils(BigImgLookActivity.this,
				R.drawable.image_download_fail, true, true);
		loadImageCallBack = new BigImgLoadCallBack();
	}

	@Override
	public int setLayoutId() {
		return R.layout.big_image_lookover;
	}

	@Override
	protected void loadLayout(View v) {
		init();
		initIndicator();
		setViewPageAdpter();
	}

	@Override
	protected void setUpView() {

	}

	/**
	 * 初始化引导图标 动态创建多个小圆点，然后组装到线性布局里
	 */
	private void initIndicator() {
		for (int index = 0; index < imageUrlList.size(); index++) {
			ImageView dotImage = new ImageView(this);
			dotImage.setLayoutParams(new LayoutParams(10, 10));
			if (index == currentPage) {
				dotImage.setBackgroundColor(Color.parseColor("#AAAAAA"));
			} else {
				dotImage.setBackgroundColor(Color.parseColor("#444444"));
			}
			tipList.add(dotImage);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			params.leftMargin = 5;
			params.rightMargin = 5;
			tipsBoxLayout.addView(dotImage, params);
		}
	}

	/**
	 * 初始化PagerAdapter
	 * */
	@SuppressLint("ClickableViewAccessibility")
	private void setViewPageAdpter() {
		for (int index = 0; index < imageUrlList.size(); index++) {
			PinchImageView imageView = new PinchImageView(this);
			imageViewList.add(imageView);
			imageView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					finish();
					return true;
				}
			});
		}
		bigImageAdapter = new BigImageAdapter(imageViewList);
		viewPager.setAdapter(bigImageAdapter);
		viewPager.setCurrentItem(currentPage);
		viewPager.setOnPageChangeListener(new BigImageListener());
	}

	/**
	 * 适配器，负责装配 、销毁 数据 和 组件 。
	 */
	private class BigImageAdapter extends PagerAdapter {

		private List<PinchImageView> mList;

		public BigImageAdapter(List<PinchImageView> list) {
			mList = list;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mList.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			bitmapUtils.display(imageViewList.get(position),
					imageUrlList.get(position), loadImageCallBack);
			container.addView(mList.get(position));
			return mList.get(position);
		}
	}

	/**
	 * 动作监听器
	 */
	private class BigImageListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == 0) {
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			tipList.get(currentPage).setBackgroundColor(
					Color.parseColor("#AAAAAA"));
			currentPage = position;
			tipList.get(position).setBackgroundColor(
					Color.parseColor("#444444"));
		}
	}

	/**
	 * 加载图片时的回调函数
	 * */
	public class BigImgLoadCallBack extends
			DefaultBitmapLoadCallBack<ImageView> {
		private final ImageView iView;

		public BigImgLoadCallBack() {
			this.iView = null;
		}

		// 开始加载
		@Override
		public void onLoadStarted(ImageView container, String uri,
				BitmapDisplayConfig config) {

			super.onLoadStarted(container, uri, config);
		}

		// 加载过程中
		@Override
		public void onLoading(ImageView container, String uri,
				BitmapDisplayConfig config, long total, long current) {
			LogUtils.i("加载中....");
		}

		// 加载完成时
		@Override
		public void onLoadCompleted(ImageView container, String uri,
				Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
			LogUtils.i("加载完成");
			container.setImageBitmap(bitmap);
		}
	}

	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
