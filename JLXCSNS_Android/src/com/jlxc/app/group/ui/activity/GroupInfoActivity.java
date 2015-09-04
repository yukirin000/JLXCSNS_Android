package com.jlxc.app.group.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GroupInfoActivity extends BaseActivityWithTopBar {

	// 创建者
	@ViewInject(R.id.layout_group_info_create_creator)
	private LinearLayout groupCreator;
	// 成员
	@ViewInject(R.id.layout_group_info_member)
	private LinearLayout groupMumber;
	// 操作按钮
	@ViewInject(R.id.btn_group_operate)
	private Button groupOperate;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;

	@Override
	public int setLayoutId() {
		return R.layout.activity_group_info;
	}

	@Override
	protected void setUpView() {
		initImageLoader();
		initWidget();
	}

	/**
	 * 初始化图片加载工具
	 * */
	private void initImageLoader() {
		// 获取显示图片的实例
		imgLoader = ImageLoader.getInstance();
		// 显示图片的配置
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	/**
	 * 控件初始化
	 * */
	private void initWidget() {
		setBarText("圈子信息");
		// 添加圈子设置按钮
		ImageView groupInfo = addRightImgBtn(R.layout.right_image_button,
				R.id.layout_top_btn_root_view, R.id.img_btn_right_top);
		groupInfo.setImageResource(R.drawable.setting_btn);
		groupInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intentGroupSet = new Intent(GroupInfoActivity.this,
						GroupManageActivity.class);
				startActivityWithRight(intentGroupSet);

			}
		});

		// 点击创建人item
		groupCreator.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});

		// 查看所有所有成员
		groupMumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});

		// 操作按钮事件
		groupOperate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});
	}
}
