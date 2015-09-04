package com.jlxc.app.group.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.ConfigUtils;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CampusPersonModel;
import com.jlxc.app.news.ui.activity.CampusAllPersonActivity;
import com.jlxc.app.news.ui.activity.CampusNewsListActivity;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampusInfoActivity extends BaseActivityWithTopBar {

	// 动态listview
	@ViewInject(R.id.campus_listview)
	private PullToRefreshListView campusListView;
	private HelloHaAdapter<String> newsAdapter;
	// 学校的人
	private List<CampusPersonModel> personList;
	// 学校的人的头像
	private GridView personHeadGridView;
	// 学校的人的adapter
	private HelloHaAdapter<CampusPersonModel> personGVAdapter;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;
	// 屏幕的尺寸
	private int screenWidth = 0;
	// 时间戳
	private String latestTimesTamp = "";
	// 是否下拉
	private boolean isPullDowm = true;
	// 是否正在请求数据
	private boolean isRequestData = false;
	// 学校位置
	private TextView schoolLocationTextView;
	// 学校名字
	private TextView schoolNameTextView;
	// 学校学生数量
	private TextView studentCountTextView;
	// 未读的消息数量
	private TextView unreadNewsTextView;

	@Override
	public int setLayoutId() {
		return R.layout.activity_campus_home_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	protected void setUpView() {

		init();
		newsListViewSet();
		// 进入本页面时请求数据
		getCampusData();
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {

		itemViewClickListener = new ItemViewClick();
		// 图片加载初始化
		imgLoader = ImageLoader.getInstance();
		// 显示图片的配置
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;

		personList = new ArrayList<CampusPersonModel>();

	}

	/**
	 * listView 的设置
	 * */
	private void newsListViewSet() {
		// 设置刷新模式
		campusListView.setMode(Mode.PULL_FROM_START);
		/**
		 * 刷新监听
		 * */
		campusListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (!isRequestData) {
					isRequestData = true;
					isPullDowm = true;
					getCampusData();
				}
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {

			}

		});

		List<String> tmpList = new ArrayList<String>();
		tmpList.add("");
		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<String>(CampusInfoActivity.this,
				R.layout.campus_home_layout, tmpList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, String item) {

				setCampusHeadView(helper);
			}
		};

		// 设置不可点击
		newsAdapter.setItemsClickEnable(false);
		campusListView.setAdapter(newsAdapter);
	}

	/**
	 * 设置头部item
	 * */
	private void setCampusHeadView(HelloHaBaseAdapterHelper helper) {
		// 学校名字
		schoolNameTextView = helper.getView(R.id.tv_campus_head_name);
		// 学校位置
		schoolLocationTextView = helper.getView(R.id.school_location_textview);
		// 学校人数
		studentCountTextView = helper.getView(R.id.student_count_text_view);
		// 未读
		unreadNewsTextView = helper.getView(R.id.unread_news_count_text_view);

		// 头像的尺寸,正方形显示
		final int headImageSize = screenWidth / 6;
		personGVAdapter = new HelloHaAdapter<CampusPersonModel>(
				CampusInfoActivity.this,
				R.layout.campus_head_person_gridview_item_layout, personList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					CampusPersonModel item) {
				ImageView imgView = helper
						.getView(R.id.iv_campus_person_gridview_item);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = headImageSize;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

				if (null != item.getHeadSubImage()
						&& item.getHeadSubImage().length() > 0) {
					// 显示校园所有的人的头像
					imgLoader
							.displayImage(
									item.getHeadSubImage(),
									(ImageView) helper
											.getView(R.id.iv_campus_person_gridview_item),
									options);
				} else {
					helper.setImageResource(
							R.id.iv_campus_person_gridview_item,
							R.drawable.default_avatar);
				}
			}
		};
		personHeadGridView = (GridView) helper.getView(R.id.gv_school_person);
		// 绑定适配器
		personHeadGridView.setAdapter(personGVAdapter);
		PersonGridViewItemClick personItemClickListener = new PersonGridViewItemClick();
		personHeadGridView.setOnItemClickListener(personItemClickListener);

		// 查看所有的校友
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());

			}
		};
		helper.setOnClickListener(R.id.campus_person_layout, listener);

		// 未读新消息
		helper.setOnClickListener(R.id.unread_news_layout, listener);
	}

	private void schoolHomeRelayout(JSONObject homeJsonObject) {

		// 学校名字
		schoolNameTextView.setText(UserManager.getInstance().getUser()
				.getSchool());
		// 学校位置
		if (homeJsonObject.containsKey("school")) {
			JSONObject schoolObject = homeJsonObject.getJSONObject("school");
			String locationString = schoolObject.getString("city_name") + " ▪ "
					+ schoolObject.getString("district_name");
			schoolLocationTextView.setText(locationString);
		}
		if (homeJsonObject.containsKey("student_count")) {
			// 学校人数
			studentCountTextView.setText(JLXCUtils.stringToInt(homeJsonObject
					.getString("student_count")) + "人");
		}
		if (homeJsonObject.containsKey("unread_news_count")) {
			// 新闻未读tv
			int unreadCount = JLXCUtils.stringToInt(homeJsonObject
					.getString("unread_news_count"));
			if (unreadCount > 0) {
				if (unreadCount > 99) {
					unreadCount = 99;
				}
				unreadNewsTextView.setVisibility(View.VISIBLE);
				unreadNewsTextView.setText(unreadCount + "");
			} else {
				unreadNewsTextView.setVisibility(View.GONE);
			}
		}

		// 学校的人
		if (homeJsonObject.containsKey("info")) {
			@SuppressWarnings("unchecked")
			List<JSONObject> JPersonList = (List<JSONObject>) homeJsonObject
					.get("info");
			// 清空
			personList.clear();
			// 解析校园的人
			for (JSONObject personObj : JPersonList) {
				CampusPersonModel tempPerson = new CampusPersonModel();
				tempPerson.setContentWithJson(personObj);
				personList.add(tempPerson);
			}
		}
		personGVAdapter.replaceAll(personList);
		// 头像的尺寸,正方形显示
		final int headImageSize = screenWidth / 6;
		// 设置gridview的尺寸
		int photoCount = personList.size();
		int gridviewWidth = (int) (photoCount * (headImageSize + 4));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
		personHeadGridView.setColumnWidth(headImageSize);
		personHeadGridView.setHorizontalSpacing(4);
		personHeadGridView.setStretchMode(GridView.NO_STRETCH);
		personHeadGridView.setNumColumns(photoCount);
		personHeadGridView.setLayoutParams(params);
	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.campus_person_layout:
				// 跳转到所有好友列表页面
				Intent personIntent = new Intent(CampusInfoActivity.this,
						CampusAllPersonActivity.class);
				personIntent.putExtra("School_Code", UserManager.getInstance()
						.getUser().getSchool_code());
				startActivityWithRight(personIntent);
				break;
			case R.id.unread_news_layout:
				// 未读新消息隐藏
				unreadNewsTextView.setVisibility(View.GONE);
				Intent intent = new Intent(CampusInfoActivity.this,
						CampusNewsListActivity.class);
				// intent.putExtra(CampusNewsListActivity.SCHOOL_CODE,
				// UserManager.getInstance().getUser().getSchool_code());
				startActivityWithRight(intent);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * listview点击事件接口,用于区分不同view的点击事件
	 * 
	 * @author Alan
	 * 
	 */
	private interface ListItemClickHelp {
		void onClick(View view, int postion, int viewID);
	}

	/**
	 * 获取学校动态的数据
	 * */
	private void getCampusData() {
		// 上一次查询时间处理
		String lastRefeshTime = ConfigUtils
				.getStringConfig(ConfigUtils.LAST_REFRESH__SCHOOL_HOME_NEWS_DATE);
		if (null == lastRefeshTime || lastRefeshTime.length() < 1) {
			lastRefeshTime = "";
			ConfigUtils.saveConfig(
					ConfigUtils.LAST_REFRESH__SCHOOL_HOME_NEWS_DATE,
					System.currentTimeMillis() / 1000 + "");
		}
		// 1441074913
		String path = JLXCConst.SCHOOL_HOME_DATA + "?" + "user_id="
				+ UserManager.getInstance().getUser().getUid()
				+ "&school_code="
				+ UserManager.getInstance().getUser().getSchool_code()
				+ "&last_time=" + lastRefeshTime;
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
							// 处理数据
							schoolHomeRelayout(jResult);
							campusListView.onRefreshComplete();
							isRequestData = false;
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(CampusInfoActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
							campusListView.onRefreshComplete();
							isRequestData = false;
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(CampusInfoActivity.this,
								"网络抽筋了，请检查(→_→)");
						campusListView.onRefreshComplete();
						isRequestData = false;
					}

				}, null));
	}

	/**
	 * 学校的人gridview监听
	 */
	public class PersonGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CampusPersonModel personModel = (CampusPersonModel) parent
					.getAdapter().getItem(position);
			// 跳转到用户的主页
			JumpToHomepage(JLXCUtils.stringToInt(personModel.getUserId()));
		}
	}

	/**
	 * 跳转至用户的主页
	 */
	private void JumpToHomepage(int userID) {
		Intent intentUsrMain = new Intent(CampusInfoActivity.this,
				OtherPersonalActivity.class);
		intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY, userID);
		startActivityWithRight(intentUsrMain);
	}

}
