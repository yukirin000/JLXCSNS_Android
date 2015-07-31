package com.jlxc.app.base.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.view.ImageLoadingDialog;
import com.jlxc.app.base.ui.view.ItemDialog;
import com.jlxc.app.base.ui.view.ItemDialog.ClickCallBack;
import com.jlxc.app.base.ui.view.TouchImageView;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.ImageModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

@SuppressLint("NewApi")
public class BigImgLookActivity extends BaseActivity {

	// 传递单张图片
	public static final String INTENT_KEY = "Single_Image";
	// 传递图片model列表
	public static final String INTENT_KEY_IMG_MODEl_LIST = "Model_Path_List";
	// 传递图片URL列表
	public static final String INTENT_KEY_IMG_LIST = "Url_Path_List";
	// 传递当前图片的位置
	public static final String INTENT_KEY_INDEX = "Current_Index";
	// 图片路径
	private List<String> imageUrlList = new ArrayList<String>();
	// 图片缩略图路径
	private List<String> imageSubUrlList = new ArrayList<String>();
	// 所有的imageview
	private List<TouchImageView> imageViewList = new ArrayList<TouchImageView>();
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
	// 是否是长按操作
	private boolean isLoneClick = false;
	// 加载动画
	private Dialog loadingDialog;
	// 保存dialog
	private ItemDialog downDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	}

	@Override
	public int setLayoutId() {
		return R.layout.big_image_lookover;
	}

	@Override
	protected void loadLayout(View v) {
	}

	@Override
	protected void setUpView() {
		init();
		initIndicator();
		createPageImageView();
		setViewPageAdpter();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		loadingDialog = ImageLoadingDialog
				.createLoadingDialog(BigImgLookActivity.this);
		Intent intent = this.getIntent();
		if (intent.hasExtra(INTENT_KEY)) {
			// 传递的是单张图片url
			if (intent.hasExtra(INTENT_KEY)) {
				imageUrlList.add(intent.getStringExtra(INTENT_KEY));
				imageSubUrlList = imageUrlList;
			} else {
				LogUtils.e("未传递图片地址");
			}
		} else if (intent.hasExtra(INTENT_KEY_IMG_MODEl_LIST)) {
			// 传递图片model列表
			List<ImageModel> imgModList = (List<ImageModel>) intent
					.getSerializableExtra(INTENT_KEY_IMG_MODEl_LIST);
			for (int index = 0; index < imgModList.size(); index++) {
				imageUrlList.add(imgModList.get(index).getURL());
				imageSubUrlList.add(imgModList.get(index).getSubURL());
			}
			if (intent.hasExtra(INTENT_KEY_INDEX)) {
				currentPage = intent.getIntExtra(INTENT_KEY_INDEX, 0);
			} else {
				LogUtils.w("未传递所点击图片的位置，默认为0.");
			}
		} else if (intent.hasExtra(INTENT_KEY_IMG_LIST)) {
			// 传递图片地址list
			imageUrlList = (List<String>) intent
					.getSerializableExtra(INTENT_KEY_IMG_LIST);
			imageSubUrlList = imageUrlList;
			if (intent.hasExtra(INTENT_KEY_INDEX)) {
				currentPage = intent.getIntExtra(INTENT_KEY_INDEX, 0);
			} else {
				LogUtils.w("未传递所点击图片的位置，默认为0.");
			}
		} else {
			LogUtils.e("未传递图片地址,图片数为：" + imageUrlList.size());
		}

		BitmapManager bmpManager = BitmapManager.getInstance();
		bitmapUtils = bmpManager.getHeadPicBitmapUtils(BigImgLookActivity.this,
				R.drawable.image_download_fail, true, true);
		loadImageCallBack = new BigImgLoadCallBack();
	}

	/**
	 * 初始化引导图标 动态创建多个小圆点，然后组装到线性布局里
	 */
	private void initIndicator() {
		if (imageUrlList.size() > 1) {
			for (int index = 0; index < imageUrlList.size(); index++) {
				ImageView dotImage = new ImageView(this);
				dotImage.setLayoutParams(new LayoutParams(10, 10));
				if (index == currentPage) {
					dotImage.setBackgroundResource(R.drawable.dot_select);
				} else {
					dotImage.setBackgroundResource(R.drawable.dot_notselect);
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
	}

	/**
	 * viewpage初始化
	 * */
	private void createPageImageView() {
		for (int index = 0; index < imageUrlList.size(); index++) {
			TouchImageView tcView = new TouchImageView(this);
			tcView.setClickable(true);
			bitmapUtils.display(tcView, imageSubUrlList.get(index),
					new DefaultBitmapLoadCallBack<View>());
			imageViewList.add(tcView);
			tcView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!isLoneClick) {
						finish();
					}
				}
			});

			tcView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					
					isLoneClick = true;
					imageLongClick();
					return true;
				}
			});
		}
	}

	/**
	 * 初始化PagerAdapter
	 * */
	@SuppressLint("ClickableViewAccessibility")
	private void setViewPageAdpter() {

		bigImageAdapter = new BigImageAdapter(imageViewList);
		viewPager.setAdapter(bigImageAdapter);
		viewPager.setOnPageChangeListener(new BigImageListener());
		viewPager.setCurrentItem(currentPage);
	}

	/**
	 * 长按操作
	 * */
	private void imageLongClick() {
		List<String> menuList = new ArrayList<String>();
		menuList.add("保存到手机");
		downDialog = new ItemDialog(BigImgLookActivity.this, menuList);
		downDialog.setClickCallBack(new ClickCallBack() {

			@Override
			public void Onclick(View view,int which) {
				String imagePath = imageUrlList.get(currentPage);
				int nameIndex = imagePath.lastIndexOf("/");
				String imageName = imagePath.substring(nameIndex + 1);
				download(imagePath, imageName);
				downDialog.cancel();
			}
		});
		downDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				isLoneClick = false;
			}
		});
		downDialog.show();
	}

	/**
	 * 下载图片
	 * */
	private void download(String Url, String imageName) {
		HttpUtils http = new HttpUtils();
		http.download(Url, "/sdcard/helloha/" + imageName, true, true,
				new RequestCallBack<File>() {
					@Override
					public void onStart() {
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
					}

					@Override
					public void onFailure(HttpException error, String msg) {
					}

					@Override
					public void onSuccess(ResponseInfo<File> responseInfo) {
						ToastUtil.show(BigImgLookActivity.this, "已保存至"
								+ responseInfo.result.getPath());
					}
				});
	}

	/**
	 * 适配器，负责装配 、销毁 数据 和 组件 。
	 */
	private class BigImageAdapter extends PagerAdapter {

		private List<TouchImageView> mList;

		public BigImageAdapter(List<TouchImageView> list) {
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
			if (position == currentPage) {
				bitmapUtils.display(mList.get(position),
						imageUrlList.get(position), loadImageCallBack);
			}
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
			bitmapUtils.display(imageViewList.get(position),
					imageUrlList.get(position), loadImageCallBack);
			tipList.get(currentPage).setBackgroundResource(
					R.drawable.dot_notselect);
			tipList.get(position).setBackgroundResource(R.drawable.dot_select);
			currentPage = position;
		}
	}

	/**
	 * 加载图片时的回调函数
	 * */
	public class BigImgLoadCallBack extends
			DefaultBitmapLoadCallBack<ImageView> {

		// 开始加载
		@Override
		public void onLoadStarted(ImageView container, String uri,
				BitmapDisplayConfig config) {
			loadingDialog.show();
			super.onLoadStarted(container, uri, config);
		}

		// 加载过程中
		@Override
		public void onLoading(ImageView container, String uri,
				BitmapDisplayConfig config, long total, long current) {
		}

		// 加载完成时
		@Override
		public void onLoadCompleted(ImageView container, String uri,
				Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
			loadingDialog.cancel();
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
