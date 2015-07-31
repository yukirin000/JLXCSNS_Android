package com.jlxc.app.base.ui.view.gallery.imageloader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.view.gallery.bean.ImageFloder;
import com.jlxc.app.base.ui.view.gallery.imageloader.ListImageDirPopupWindow.OnImageDirSelected;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.news.ui.activity.PublishNewsActivity;
import com.lidroid.xutils.view.annotation.ViewInject;

public class GalleyActivity extends BaseActivityWithTopBar implements
		OnImageDirSelected {

	public static final String PHOTO_PATH_LIST = "select_result";
	// 所有的图片
	private List<String> mImgs;
	// 其余相册
	@ViewInject(R.id.layout_galley_bottom_ly)
	private RelativeLayout mBottomLy;
	// 当前的相册
	@ViewInject(R.id.tv_galley_choose_dir)
	private TextView mChooseDir;
	// 照片列表
	@ViewInject(R.id.gridView_galley)
	private GridView mGirdView;
	// 适配器
	private GalleyAdapter mAdapter;
	// 临时的辅助类，用于防止同一个文件夹的多次扫描
	private HashSet<String> mDirPaths = new HashSet<String>();
	// 扫描拿到所有的图片文件夹
	private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();
	int totalCount = 0;
	// 存储文件夹中的图片数量
	private int mPicsSize;
	// 默认显示的文件夹
	private File defaultImgDir;
	// 图片最多的文件夹
	private File mImgDir;
	// 屏幕高
	private int mScreenHeight;
	// 弹出框
	private ListImageDirPopupWindow mListImageDirPopupWindow;

	@Override
	public int setLayoutId() {
		return R.layout.gallery_activity_main;
	}

	@Override
	protected void setUpView() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;

		getImages();
		initEvent();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			hideLoading();
			// 为View绑定数据
			data2View();
			// 初始化展示文件夹的popupWindw
			initListDirPopupWindw();
		}
	};

	/**
	 * 为View绑定数据
	 */
	private void data2View() {
		if (mImgDir == null) {
			Toast.makeText(getApplicationContext(), "擦，一张图片没扫描到",
					Toast.LENGTH_SHORT).show();
			return;
		}

		mImgs = Arrays.asList(defaultImgDir.list());
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		mAdapter = new GalleyAdapter(getApplicationContext(), mImgs,
				R.layout.gallery_grid_item, defaultImgDir.getAbsolutePath());
		mGirdView.setAdapter(mAdapter);
	};

	/**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindw() {
		mListImageDirPopupWindow = new ListImageDirPopupWindow(
				LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				mImageFloders, LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.gallery_list_dir, null));

		mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		// 显示进度条
		showLoading("正在加载...", true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				String firstImage = null;
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = GalleyActivity.this
						.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);

				Log.e("TAG", mCursor.getCount() + "");
				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));

					// 拿到第一张图片的路径
					if (firstImage == null)
						firstImage = path;
					// 获取该图片的父路径名
					File parentFile = new File(path).getParentFile();
					if (parentFile == null)
						continue;
					String dirPath = parentFile.getAbsolutePath();
					ImageFloder imageFloder = null;
					// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						mDirPaths.add(dirPath);
						// 初始化imageFloder
						imageFloder = new ImageFloder();
						imageFloder.setDir(dirPath);
						imageFloder.setFirstImagePath(path);
					}

					int picSize = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg"))
								return true;
							return false;
						}
					}).length;
					totalCount += picSize;

					imageFloder.setCount(picSize);
					mImageFloders.add(imageFloder);

					if (picSize > mPicsSize) {
						mPicsSize = picSize;
						mImgDir = parentFile;
					}
					// 默认显示的文件夹
					if (imageFloder.getDir().contains("/Camera")) {
						if (imageFloder.getCount() > 0) {
							defaultImgDir = parentFile;
						}
					}
				}
				mCursor.close();

				// 扫描完成，辅助的HashSet也就可以释放内存了
				mDirPaths = null;
				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(0x110);
			}
		}).start();

	}

	/**
	 * 事件监听
	 * */
	private void initEvent() {

		// 为底部的布局设置点击事件，弹出popupWindow
		mBottomLy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListImageDirPopupWindow
						.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = .3f;
				getWindow().setAttributes(lp);
			}
		});

		TextView finishView = addRightBtn("完成");
		finishView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击完成时
				List<String> list = mAdapter.getSelectedImageList();
				if (list.size() > 0) {
					Intent intentFinish = new Intent();
					intentFinish.putExtra(PHOTO_PATH_LIST, (Serializable) list);
					setResult(Activity.RESULT_OK, intentFinish);
					finish();
				}
			}
		});
	}

	/**
	 * 切换文件目录
	 * */
	@Override
	public void selected(ImageFloder floder) {

		defaultImgDir = new File(floder.getDir());
		mImgs = Arrays.asList(defaultImgDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".jpg") || filename.endsWith(".png")
						|| filename.endsWith(".jpeg"))
					return true;
				return false;
			}
		}));

		// 文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		mAdapter = new GalleyAdapter(getApplicationContext(), mImgs,
				R.layout.gallery_grid_item, defaultImgDir.getAbsolutePath());
		mGirdView.setAdapter(mAdapter);
		mChooseDir.setText(floder.getName().replace("/", ""));
		mListImageDirPopupWindow.dismiss();
	}
}
