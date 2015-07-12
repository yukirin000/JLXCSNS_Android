package com.jlxc.app.base.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.view.PinchImageView;
import com.jlxc.app.base.utils.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


public class BigImgLookActivity extends BaseActivity {
	public static final String INTENT_KEY = "filePath";
	@ViewInject(R.id.img_big_look)
	private PinchImageView img_big_look;
	private String imagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imagePath = getIntent().getStringExtra(INTENT_KEY);
//		FileUtil.loadBigImage(BigImgLookActivity.this, imagePath, img_big_look);
		BitmapManager bmpManager = BitmapManager.getInstance();
		bmpManager.getHeadPicBitmapUtils(BigImgLookActivity.this,
				R.drawable.image_download_fail, true, true).display(img_big_look,imagePath);
		
		LogUtils.i(imagePath, 1);
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
		
	}

	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finishWithBottom();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
}
