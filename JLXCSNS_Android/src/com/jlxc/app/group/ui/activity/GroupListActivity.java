package com.jlxc.app.group.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

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
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 圈子列表
 * */
public class GroupListActivity extends BaseActivityWithTopBar {

	// 圈子的相关信息
	private final static String GROUP_TYPE = "group_type";
	private final static String GROUP_NAME = "group_name";
	private final static String GROUP_MEMBER = "group_member";
	private final static String GROUP_COVER_IMG = "group_cover_image";
	private final static String GROUP_UNREAD_COUNT = "group_unread_msg";
	// 圈子类别
	private final static String GROUP_TYPE_SCHOOL = "school";
	private final static String GROUP_TYPE_ATTENTION = "attention";
	private final static String GROUP_TYPE_CREATE = "create";

	// 动态listview
	@ViewInject(R.id.listview_my_group)
	private PullToRefreshListView groupListView;
	// 动态列表适配器
	private HelloHaAdapter<HashMap<String, String>> groupAdapter = null;
	// 用户关注的圈子信息数据
	private List<HashMap<String, String>> groupList = new ArrayList<HashMap<String, String>>();
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;

	@Override
	public int setLayoutId() {
		return R.layout.activity_group_list;
	}

	@Override
	protected void setUpView() {
		// 设置标头
		setBarText("我的圈子");
		// 初始化
		initialize();
		newsListViewSet();
	}

	/**
	 * 数据的初始化
	 * */
	private void initialize() {

		itemViewClickListener = new ItemViewClick();
		// 获取显示图片的实例
		imgLoader = ImageLoader.getInstance();
		// 显示图片的配置
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
		initData();
	}

	//
	private void initData() {
		// 初始化校内圈子
		HashMap<String, String> schoolGroup = new HashMap<String, String>();
		schoolGroup.put(GROUP_TYPE, GROUP_TYPE_SCHOOL);
		schoolGroup.put(GROUP_NAME, "知乎学园");
		schoolGroup.put(GROUP_MEMBER, "128");
		schoolGroup.put(GROUP_COVER_IMG, "知乎学园");
		schoolGroup.put(GROUP_UNREAD_COUNT, "20");
		groupList.add(schoolGroup);

		// 假数据
		HashMap<String, String> fakeGroup = new HashMap<String, String>();
		fakeGroup.put(GROUP_TYPE, GROUP_TYPE_ATTENTION);
		fakeGroup.put(GROUP_NAME, "tfboys粉丝大集合");
		fakeGroup.put(GROUP_MEMBER, "128");
		fakeGroup
				.put(GROUP_COVER_IMG,
						"http://img4.duitang.com/uploads/item/201407/15/20140715095327_GBB4d.jpeg");
		fakeGroup.put(GROUP_UNREAD_COUNT, "20");
		groupList.add(fakeGroup);
	}

	/**
	 * listView 的设置
	 * */
	private void newsListViewSet() {
		// 设置刷新模式
		groupListView.setMode(Mode.DISABLED);
		/**
		 * 刷新监听
		 * */
		groupListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {

			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {

			}
		});

		/**
		 * adapter的设置
		 * */
		groupAdapter = new HelloHaAdapter<HashMap<String, String>>(
				GroupListActivity.this, R.layout.group_listitem_layout,
				groupList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					HashMap<String, String> item) {
				LogUtils.i("type=" + item.get(GROUP_TYPE));
				if (item.get(GROUP_TYPE).equals(GROUP_TYPE_SCHOOL)) {
					// 校内圈子
					helper.setImageResource(R.id.img_group_icon,
							R.drawable.my_group_icon);
					helper.setText(R.id.txt_group_name, item.get(GROUP_NAME));
					helper.setText(R.id.txt_group_member_count,
							item.get(GROUP_MEMBER) + "个小伙伴");
					helper.setText(R.id.txt_group_unread_news_count,
							item.get(GROUP_UNREAD_COUNT));
				} else {
					// 关注的圈子
					imgLoader.displayImage(item.get(GROUP_COVER_IMG),
							(ImageView) helper.getView(R.id.img_group_icon),
							options);
					helper.setText(R.id.txt_group_name, item.get(GROUP_NAME));
					helper.setText(R.id.txt_group_member_count,
							item.get(GROUP_MEMBER) + "人关注");
					helper.setText(R.id.txt_group_unread_news_count,
							item.get(GROUP_UNREAD_COUNT));
				}

				// 设置事件监听
				final int postion = helper.getPosition();
				OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(View view) {
						itemViewClickListener.onClick(view, postion,
								view.getId());
					}
				};
				helper.setOnClickListener(R.id.layout_group_Listitem_rootview,
						listener);
			}
		};

		// 设置不可点击
		groupAdapter.setItemsClickEnable(false);
		groupListView.setAdapter(groupAdapter);
	}

	/**
	 * 获取动态数据
	 * */
	private void getGroupListData(int userID, int desPage, String lastTime) {
		String path = JLXCConst.NEWS_LIST + "?" + "user_id=" + userID
				+ "&page=" + desPage + "&frist_time=" + lastTime;

		HttpManager.get(path, new JsonRequestCallBack<String>(
				new LoadDataHandler<String>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							// 获取圈子列表
							List<JSONObject> JSONList = (List<JSONObject>) jResult
									.get("list");
							jsonToGroupData(JSONList);
						}

						if (status == JLXCConst.STATUS_FAIL) {
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil
								.show(GroupListActivity.this, "网络抽筋了，请检查(→_→)");
					}

				}, null));
	}

	/**
	 * 数据处理
	 */
	private void jsonToGroupData(List<JSONObject> dataList) {
		for (JSONObject newsObj : dataList) {

		}
	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.layout_group_Listitem_rootview:
				if (groupAdapter.getItem(postion).get(GROUP_TYPE)
						.equals(GROUP_TYPE_SCHOOL)) {
					// 跳转至校内容部分
					Intent intentToGroupNews = new Intent();
					intentToGroupNews.setClass(GroupListActivity.this,
							GroupNewsActivity.class);
					// 传递是否是圈子还是校园
					intentToGroupNews.putExtra(GroupNewsActivity.INTENT_KEY,
							true);
					// 传递名称
					intentToGroupNews.putExtra(
							GroupNewsActivity.INTENT_KEY_GROUP_NAME,
							groupAdapter.getItem(postion).get(GROUP_NAME));
					startActivityWithRight(intentToGroupNews);
				} else {
					// 跳转至圈子内容部分
					Intent intentToGroupNews = new Intent();
					intentToGroupNews.setClass(GroupListActivity.this,
							GroupNewsActivity.class);
					intentToGroupNews.putExtra(GroupNewsActivity.INTENT_KEY,
							false);
					// 传递名称
					intentToGroupNews.putExtra(
							GroupNewsActivity.INTENT_KEY_GROUP_NAME,
							groupAdapter.getItem(postion).get(GROUP_NAME));
					startActivityWithRight(intentToGroupNews);
				}
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
	 */
	private interface ListItemClickHelp {
		void onClick(View view, int postion, int viewID);
	}
}
