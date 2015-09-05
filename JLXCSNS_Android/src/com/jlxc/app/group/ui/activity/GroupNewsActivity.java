package com.jlxc.app.group.ui.activity;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.group.ui.fragment.CampusNewsFragment;
import com.jlxc.app.group.ui.fragment.GroupNewsFragment;
import com.jlxc.app.news.ui.activity.PublishNewsActivity;
import com.lidroid.xutils.view.annotation.ViewInject;

public class GroupNewsActivity extends BaseActivityWithTopBar {

	public static final String INTENT_KEY_GROUP_TYPE = "is_campus";
	public static final String INTENT_KEY_VISITOR_TYPE = "is_student_view";
	public static final String INTENT_KEY_GROUP_NAME = "group_name";
	// 发布按钮
	@ViewInject(R.id.img_group_publish_btn)
	private ImageView publishBtn;
	// 查看的是否是校园
	private boolean isCampus = false;
	// 查看者是否是本校的学生
	private boolean isStudent = false;
	// 圈子/校园的名称
	private String groupName;
	// 添加圈子信息按钮
	private ImageView groupInfo;

	@Override
	public int setLayoutId() {
		return R.layout.activity_group_news;
	}

	@Override
	protected void setUpView() {
		init();
		widgetInit();
		fragmetInit();
	}

	/**
	 * 初始化
	 * */
	private void init() {
		Intent intent = this.getIntent();
		// 获取是否是圈子
		if (intent.hasExtra(INTENT_KEY_GROUP_TYPE)) {
			isCampus = intent.getBooleanExtra(INTENT_KEY_GROUP_TYPE, false);
		} else {
			LogUtils.e("未传递圈子类型");
		}
		// 获取是否是圈子
		if (intent.hasExtra(INTENT_KEY_VISITOR_TYPE)) {
			isStudent = intent.getBooleanExtra(INTENT_KEY_VISITOR_TYPE, false);
		} else {
			LogUtils.e("未传查看着的类型");
		}
		// 获取圈子名称
		if (intent.hasExtra(INTENT_KEY_GROUP_NAME)) {
			groupName = intent.getStringExtra(INTENT_KEY_GROUP_NAME);
		} else {
			LogUtils.e("未传递圈子名称");
		}
	}

	/**
	 * 判断是校园还是圈子选取不同的fragment
	 * */
	private void fragmetInit() {
		if (isCampus) {
			// 添加为校园 listview的fragment
			CampusNewsFragment framentCampus = CampusNewsFragment.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.layout_fragment_container, framentCampus)
					.commit();
			//非本校学生查看校园，则隐藏主页发布按钮
			if (!isStudent) {
				groupInfo.setVisibility(View.GONE);
				publishBtn.setVisibility(View.GONE);
			}
		} else {
			// 添加为圈子listview的fragment
			GroupNewsFragment framentGroup = GroupNewsFragment.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.layout_fragment_container, framentGroup).commit();
		}
	}

	/**
	 * 控件的一些初始化
	 * */
	private void widgetInit() {
		// 设置标头
		setBarText(groupName);
		// 添加圈子信息按钮
		groupInfo = addRightImgBtn(R.layout.right_image_button,
				R.id.layout_top_btn_root_view, R.id.img_btn_right_top);
		groupInfo.setImageResource(R.drawable.campus_person_icon);

		// 添加点事件跳转至圈子资料
		groupInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isCampus) {
					// 跳转至校园主页
					Intent intentCampusInfo = new Intent(
							GroupNewsActivity.this, CampusHomeActivity.class);
					intentCampusInfo.putExtra(CampusHomeActivity.INTENT_KEY,
							true);
					startActivityWithRight(intentCampusInfo);
				} else {
					// 跳转至圈子主页
					Intent intentGroupInfo = new Intent(GroupNewsActivity.this,
							GroupInfoActivity.class);
					startActivityWithRight(intentGroupInfo);
				}
			}
		});
		// 点击发布按钮事件
		publishBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intentPublish = new Intent(GroupNewsActivity.this,
						PublishNewsActivity.class);
				startActivityWithRight(intentPublish);
			}
		});
	}
}
