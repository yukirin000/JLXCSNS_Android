package com.jlxc.app.group.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.group.model.GroupCatagoryModel;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GroupInfoActivity extends BaseActivityWithTopBar {

	public static String INTENT_KEY = "topicId";

	// 图片
	@ViewInject(R.id.group_image_view)
	private ImageView topicImageView;
	// 圈子名
	@ViewInject(R.id.group_name_text_view)
	private TextView topicNameTextView;
	// 圈子内容数量
	@ViewInject(R.id.group_news_count_text_view)
	private TextView topicCountTextView;
	// 创建者姓名
	@ViewInject(R.id.create_name_text_view)
	private TextView createNameTextView;
	// 创建者头像
	@ViewInject(R.id.create_head_image_view)
	private ImageView createImageView;	
	// 成员数量
	@ViewInject(R.id.member_count_text_view)
	private TextView memberCountTextView;
	// 话题详情
	@ViewInject(R.id.group_description_text_view)
	private TextView topicDescTextView;
	
	
	// 操作按钮 关注或者取消关注
	@ViewInject(R.id.btn_group_operate)
	private Button groupOperate;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;
	// 话题id
	private int topicId;

	@OnClick({R.id.layout_group_info_create_creator,R.id.layout_group_info_member,R.id.btn_group_operate})
	private void clickEvent(View view){
		switch (view.getId()) {
		// 点击创建人item
		case R.id.layout_group_info_create_creator:
			
			break;
		// 查看所有所有成员			
		case R.id.layout_group_info_member:
					
			break;	
		// 操作按钮事件			
		case R.id.btn_group_operate:
			
			break;
		default:
			break;
		}
		
	}
	
	@Override
	public int setLayoutId() {
		return R.layout.activity_group_info;
	}

	@Override
	protected void setUpView() {
		
		//获取intent
//		Intent intent = getIntent();
//		intent.getIntExtra(INTENT_KEY, 0);
		topicId = 7;
		
		initImageLoader();
		initWidget();
		//数据获取
		getGroupInfoData();
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

	}
	
	/**
	 * 获取群组数据
	 * */
	private void getGroupInfoData(){
		//获取群组详情
		String path = JLXCConst.GET_TOPIC_DETAIL+"?topic_id="+topicId+"&user_id="+UserManager.getInstance().getUser().getUid();
		HttpManager.get(path, new JsonRequestCallBack<String>(
				new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						hideLoading();
						int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT);
							handleResult(jResult);
								
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(GroupInfoActivity.this, "获取失败。。");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						hideLoading();
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(GroupInfoActivity.this, "获取失败。。");
					}

				}, null));
		
	}
	//处理结果
	private void handleResult(JSONObject result) {
		JSONObject content = result.getJSONObject("content");
		//名字
		topicNameTextView.setText(content.getString("topic_name"));
		//内容
		topicCountTextView.setText("共产生了"+result.getString("news_count")+"条内容");
		//图片
		ImageLoader.getInstance().displayImage(JLXCConst.ATTACHMENT_ADDR+content.getString("topic_cover_image"), topicImageView, options);
		//创建者
		createNameTextView.setText(content.getString("name"));
		//创建者头像		
		ImageLoader.getInstance().displayImage(JLXCConst.ATTACHMENT_ADDR+content.getString("head_sub_image"), createImageView, options);
		//成员数量
		memberCountTextView.setText(result.getString("member_count"));
		//圈子介绍
		topicDescTextView.setText(content.getString("topic_detail"));
		//加入状态 
		int joinState = content.getInteger("join_state");
		if (joinState == 1) {
			groupOperate.setText("取消关注");
		}else {
			groupOperate.setText("关注");
		}
	}
}
