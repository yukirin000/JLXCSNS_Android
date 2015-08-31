package com.jlxc.app.news.ui.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
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
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.ConfigUtils;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CampusPersonModel;
import com.jlxc.app.news.model.NewsConstants;
import com.jlxc.app.news.ui.activity.CampusAllPersonActivity;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampusHomeFragment extends BaseFragment {

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
	// 上下文信息
	private Context mContext;
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
	//学校位置
	private TextView schoolLocationTextView;
	//学校名字
	private TextView schoolNameTextView;
	//学校学生数量
	private TextView studentCountTextView;
	//未读的消息数量
	private TextView unreadNewsTextView;
	
	
	@Override
	public int setLayoutId() {
		return R.layout.fragment_campus_home_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	public void setUpViews(View rootView) {
		init();
		initBoradcastReceiver();
		newsListViewSet();
		// 进入本页面时请求数据
		getCampusData();
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		
		itemViewClickListener = new ItemViewClick();
		mContext = this.getActivity().getApplicationContext();
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
	 * 初始化广播信息
	 * */
	private void initBoradcastReceiver() {
		LocalBroadcastManager mLocalBroadcastManager;
		mLocalBroadcastManager = LocalBroadcastManager
				.getInstance(getActivity());
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(JLXCConst.BROADCAST_NEWS_LIST_REFRESH);
		// 注册广播
		mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
				myIntentFilter);
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
				// TODO Auto-generated method stub
				
			}

		});


		List<String> tmpList = new ArrayList<String>();
		tmpList.add("");
		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<String>(mContext, R.layout.campus_home_layout, tmpList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					String item) {
	
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
		//学校名字
		schoolNameTextView = helper.getView(R.id.tv_campus_head_name);
		//学校位置
		schoolLocationTextView = helper.getView(R.id.school_location_textview);
		//学校位置	
		studentCountTextView = helper.getView(R.id.student_count_text_view);
		//未读
		unreadNewsTextView = helper.getView(R.id.unread_news_count_text_view);
		
		// 头像的尺寸,正方形显示
		final int headImageSize = screenWidth / 6;
		personGVAdapter = new HelloHaAdapter<CampusPersonModel>(
				mContext, R.layout.campus_head_person_gridview_item_layout, personList) {

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

				// 显示校园所有的人的头像
				imgLoader.displayImage(item.getHeadSubImage(),
						(ImageView) helper
								.getView(R.id.iv_campus_person_gridview_item),
						options);
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
		helper.setOnClickListener(R.id.tv_campus_person_title, listener);
		
		//未读新消息
		helper.setOnClickListener(R.id.unread_news_layout, listener);
	}
	
	private void schoolHomeRelayout(JSONObject homeJsonObject){
		
		//学校名字
		schoolNameTextView.setText(UserManager.getInstance().getUser().getSchool());
		//学校位置
		if (homeJsonObject.containsKey("school")) {
			JSONObject schoolObject = homeJsonObject.getJSONObject("school");
			String locationString = schoolObject.getString("city_name")+"●"+schoolObject.getString("district_name");
			schoolLocationTextView.setText(locationString);
		}
		if (homeJsonObject.containsKey("student_count")) {
			//学校人数
			studentCountTextView.setText(JLXCUtils.stringToInt(homeJsonObject.getString("student_count"))+"人");	
		}
		if (homeJsonObject.containsKey("unread_news_count")) {
			//新闻未读tv			
			int unreadCount = JLXCUtils.stringToInt(homeJsonObject.getString("unread_news_count"));
			if (unreadCount > 0) {
				unreadNewsTextView.setVisibility(View.VISIBLE);
				unreadNewsTextView.setText(unreadCount+"");	
			}else {
				unreadNewsTextView.setVisibility(View.GONE);
			}
		}
		
		//学校的人
		if (homeJsonObject.containsKey("info")) {
			@SuppressWarnings("unchecked")
			List<JSONObject> JPersonList = (List<JSONObject>) homeJsonObject.get("info");
			//清空
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
			case R.id.tv_campus_person_title:
				// 跳转到所有好友列表页面
				Intent personIntent = new Intent(mContext,
						CampusAllPersonActivity.class);
				personIntent.putExtra("School_Code", UserManager.getInstance()
						.getUser().getSchool_code());
				startActivityWithRight(personIntent);
				break;
			case R.id.unread_news_layout:
				// 未读新消息布局
				
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
		//上一次查询时间处理
		String lastRefeshTime = ConfigUtils.getStringConfig(ConfigUtils.LAST_REFRESH__SCHOOL_HOME_NEWS_DATE);
		if (null == lastRefeshTime || lastRefeshTime.length() < 1) {
			lastRefeshTime = "";
			ConfigUtils.saveConfig(ConfigUtils.LAST_REFRESH__SCHOOL_HOME_NEWS_DATE, System.currentTimeMillis()/1000+"");
		}
		String path = JLXCConst.SCHOOL_HOME_DATA + "?" + "user_id=" + UserManager.getInstance().getUser().getUid() +
				"&school_code=" + UserManager.getInstance().getUser().getSchool_code()
				+ "&last_time="+lastRefeshTime;
		
		LogUtils.i("校园请求数据：" + path);
		HttpManager.get(path, new JsonRequestCallBack<String>(
				new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT);
							//处理数据
							schoolHomeRelayout(jResult);
							campusListView.onRefreshComplete();
							isRequestData = false;
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							campusListView.onRefreshComplete();
							isRequestData = false;
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络抽筋了，请检查(→_→)");
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
		Intent intentUsrMain = new Intent(mContext, OtherPersonalActivity.class);
		intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY, userID);
		startActivityWithRight(intentUsrMain);
	}


	/**
	 * 广播接收处理
	 * */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent resultIntent) {
			String action = resultIntent.getAction();
			if (action.equals(JLXCConst.BROADCAST_NEWS_LIST_REFRESH)) {
				if (resultIntent
						.hasExtra(NewsConstants.NEWS_LISTVIEW_REFRESH)) {
					// 点击table栏进行刷新
					getCampusData();
				}
			}
		}
	};

	// 平滑滚动到顶
//	private void smoothToTop() {
//		int firstVisiblePosition = campusListView.getRefreshableView()
//				.getFirstVisiblePosition();
//		if (0 == firstVisiblePosition) {
//			// 已经在顶部
//			if (!campusListView.isRefreshing()) {
//				campusListView.setRefreshing(true);
//			}
//		} else {
//			if (firstVisiblePosition < 20) {
//				campusListView.getRefreshableView().smoothScrollToPosition(0);
//			} else {
//				campusListView.getRefreshableView().setSelection(20);
//				campusListView.getRefreshableView().smoothScrollToPosition(0);
//			}
//			campusListView.getRefreshableView().clearFocus();
//		}
//	}
}
