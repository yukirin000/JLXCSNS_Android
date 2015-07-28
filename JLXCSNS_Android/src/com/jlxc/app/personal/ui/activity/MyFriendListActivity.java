package com.jlxc.app.personal.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.string;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

public class MyFriendListActivity extends BaseActivityWithTopBar {

	private final static String INTENT_KEY_USER_ID = "user_id";

	// 点赞的人的listview
	@ViewInject(R.id.listview_my_friend_list)
	private PullToRefreshListView friendListView;
	// 数据源
	private List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
	// 适配器
	private HelloHaAdapter<Map<String, String>> friendAdapter;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 当前的数据页
	private int currentPage = 1;
	// 用户的ID
	private String userID;
	// 是否是最后一页数据
	private String lastPage = "0";

	@Override
	public int setLayoutId() {
		return R.layout.activity_my_friend_list;
	}

	@Override
	protected void setUpView() {
		init();
		listviewSet();
		getFriends(String.valueOf(currentPage), String.valueOf(20));
	}

	/**
	 * 初始化
	 * */
	private void init() {
		initBitmapUtils();
		// 获取当前的用户id
		Intent intent = this.getIntent();
		if (null != intent && intent.hasExtra(INTENT_KEY_USER_ID)) {
			userID = intent.getStringExtra(INTENT_KEY_USER_ID);
		}

		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
		LogUtils.i("screenWidth=" + screenWidth + " screenHeight="
				+ screenHeight);
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = new BitmapUtils(MyFriendListActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * 数据绑定初始化
	 * */
	private void listviewSet() {
		// 设置刷新模式
		friendListView.setMode(Mode.PULL_FROM_END);

		friendAdapter = new HelloHaAdapter<Map<String, String>>(
				MyFriendListActivity.this, R.layout.my_friend_list_item_layout,
				dataList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					Map<String, String> item) {
				ImageView imgView = helper.getView(R.id.iv_my_friend_list_head);
				// 设置头像尺寸
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 7;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				helper.setImageUrl(R.id.iv_my_friend_list_head, bitmapUtils,
						JLXCConst.ATTACHMENT_ADDR + item.get("head_sub_image"),
						new HeadBitmapLoadCallBack());
			
				// 绑定昵称与学校
				helper.setText(R.id.tv_my_friend_name, item.get("name"));
				helper.setText(R.id.tv_my_friend_school, item.get("school"));
			}
		};

		/**
		 * 刷新监听
		 * */
		friendListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 下拉
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (lastPage.equals("1")) {
					friendListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							friendListView.onRefreshComplete();
						}
					}, 1000);
					ToastUtil.show(MyFriendListActivity.this, "没有数据了,哦哦");
				} else {
					getFriends(String.valueOf(currentPage), String.valueOf(20));
				}
			}
		});

		/**
		 * 设置底部自动刷新
		 * */
		friendListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (lastPage.equals("1")) {
							friendListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									friendListView.onRefreshComplete();
								}
							}, 1000);
							ToastUtil.show(MyFriendListActivity.this,
									"没有数据了,哦哦");
						} else {
							friendListView.setRefreshing(true);
							getFriends(String.valueOf(currentPage),
									String.valueOf(20));
						}
					}
				});

		// 快速滑动时不加载图片
		friendListView.setOnScrollListener(new PauseOnScrollListener(
				bitmapUtils, false, true));
		friendListView.setAdapter(friendAdapter);

		// 单击
		friendListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 跳转至个人主页
				Intent intentUsrMain = new Intent(MyFriendListActivity.this,
						OtherPersonalActivity.class);
				intentUsrMain.putExtra(
						OtherPersonalActivity.INTENT_KEY,
						JLXCUtils.stringToInt(friendAdapter.getItem(
								position - 1).get("uid")));
				startActivityWithRight(intentUsrMain);
			}
		});
	}

	private void JsonToPersonData(JSONArray jPersonList) {
		List<Map<String, String>> List = new ArrayList<Map<String, String>>();
		for (int index = 0; index < jPersonList.size(); index++) {
			JSONObject jsonObject = jPersonList.getJSONObject(index);
			Map<String, String> friedMap = new HashMap<String, String>();
			friedMap.put("uid", jsonObject.getString("uid"));
			friedMap.put("name", jsonObject.getString("name"));
			friedMap.put("head_sub_image",
					jsonObject.getString("head_sub_image"));
			friedMap.put("school", jsonObject.getString("school"));
			friedMap.put("head_image", jsonObject.getString("head_image"));
			friedMap.put("friend_remark", jsonObject.getString("friend_remark"));
			List.add(friedMap);
		}

		dataList.addAll(List);
		friendAdapter.addAll(dataList);
	}

	/**
	 * 获取朋友信息
	 * */
	public void getFriends(String page, String size) {
		String path = JLXCConst.GET_FRIENDS_LIST + "?" + "user_id="
				+ UserManager.getInstance().getUser().getUid() + "&page="
				+ page + "&size=" + size;

		HttpManager.get(path, new JsonRequestCallBack<String>(
				new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							JSONArray jsonArray = jResult
									.getJSONArray(JLXCConst.HTTP_LIST);
							lastPage = jResult.getString("is_last");
							if (lastPage.equals("0")) {
								currentPage++;
							}
							JsonToPersonData(jsonArray);
							friendListView.onRefreshComplete();
						}

						if (status == JLXCConst.STATUS_FAIL) {
							friendListView.onRefreshComplete();
							ToastUtil.show(MyFriendListActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						friendListView.onRefreshComplete();
						ToastUtil.show(MyFriendListActivity.this, "网络好像有点问题");
					}

				}, null));
	}

	/**
	 * 加载图片时的回调函数
	 * */
	public class HeadBitmapLoadCallBack extends
			DefaultBitmapLoadCallBack<ImageView> {
		private final ImageView iView;

		public HeadBitmapLoadCallBack() {
			this.iView = null;
		}

		// 开始加载
		@Override
		public void onLoadStarted(ImageView container, String uri,
				BitmapDisplayConfig config) {
			//
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
			container.setImageBitmap(bitmap);
		}
	}
}
