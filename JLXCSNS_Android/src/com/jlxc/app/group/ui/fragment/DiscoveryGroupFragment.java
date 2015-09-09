package com.jlxc.app.group.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.adapter.MultiItemTypeSupport;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.discovery.model.PersonModel;
import com.jlxc.app.discovery.model.RecommendItemData;
import com.jlxc.app.discovery.ui.avtivity.ContactsUserActivity;
import com.jlxc.app.discovery.ui.avtivity.SearchUserActivity;
import com.jlxc.app.group.view.GroupMenuPopWindow;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DiscoveryGroupFragment extends BaseFragment {

	// 圈子的相关信息
	private final static String GROUP_NAME = "group_name";
	private final static String GROUP_MEMBER = "group_member";
	private final static String GROUP_COVER_IMG = "group_cover_image";
	private final static String GROUP_UNREAD_COUNT = "group_unread_msg";
	// 圈子类别
	private final static String GROUP_TYPE_SCHOOL = "school";
	private final static String GROUP_TYPE_ATTENTION = "attention";
	private final static String GROUP_TYPE_CREATE = "create";

	// 上下文信息
	private Context mContext;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;
	// 标头
	@ViewInject(R.id.tv_discovey_group_title)
	private TextView titleTextView;
	// 创建新的圈子
	@ViewInject(R.id.btn_create_new_group)
	private ImageButton createNewGroup;
	// 圈子分类
	@ViewInject(R.id.image_group_menu)
	private ImageView groupMenu;
	// 搜索框按钮
	@ViewInject(R.id.tv_discovey_search)
	private TextView searchTextView;
	// 推荐的圈子
	@ViewInject(R.id.listview_discovey_group)
	private PullToRefreshListView discoveryGroupListView;
	// 提示信息
	@ViewInject(R.id.tv_group_prompt)
	private TextView recommendPrompt;
	// 用户关注的圈子信息数据
	private List<HashMap<String, String>> groupList = new ArrayList<HashMap<String, String>>();
	// 适配器
	private HelloHaAdapter<HashMap<String, String>> diacoveryGroupAdapter = null;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 菜单窗口
	private GroupMenuPopWindow menuPopWindow;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_diacovery_group_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	public void setUpViews(View rootView) {

		init();
		listHeadSet();
		newsListViewSet();
		// 首次更新数据
		// getRecommentData("参数", "参数");
	}

	/**
	 * 点击事件监听
	 * */
	@OnClick(value = { R.id.btn_create_new_group, R.id.tv_search_group,
			R.id.image_group_menu })
	private void clickEvent(View view) {
		switch (view.getId()) {
		// 话题菜单
		case R.id.image_group_menu:
			menuPopWindow.showPopupWindow(groupMenu);

			// 设置背景颜色变暗
			WindowManager.LayoutParams lp = getActivity().getWindow()
					.getAttributes();
			lp.alpha = .3f;
			getActivity().getWindow().setAttributes(lp);
			break;

		// 创建页面
		case R.id.btn_create_new_group:
			break;

		// 搜索页面
		case R.id.tv_search_group:
			break;

		}
	}

	/**
	 * 频道菜单的初始化
	 * */
	private void initPopupWindow() {
		menuPopWindow = new GroupMenuPopWindow(mContext);
		menuPopWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getActivity().getWindow()
						.getAttributes();
				lp.alpha = 1.0f;
				getActivity().getWindow().setAttributes(lp);
			}
		});
	}

	/**
	 * 初始化
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();

		itemViewClickListener = new ItemViewClick();
		// 获取显示图片的实例
		imgLoader = ImageLoader.getInstance();
		// 显示图片的配置
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		initPopupWindow();
		// 提示信息初始化
		// recommendPrompt.setText("一大波童鞋即将来袭  (•ิ _ •ิ )");

		// 假数据
		for (int i = 0; i < 10; i++) {
			HashMap<String, String> fakeGroup = new HashMap<String, String>();
			fakeGroup.put(GROUP_NAME, "tfboys粉丝大集合");
			fakeGroup.put(GROUP_MEMBER, "128");
			fakeGroup
					.put(GROUP_COVER_IMG,
							"http://img4.duitang.com/uploads/item/201407/15/20140715095327_GBB4d.jpeg");
			fakeGroup.put(GROUP_UNREAD_COUNT, "20");
			groupList.add(fakeGroup);
		}
	}

	/**
	 * listvew head的初始化
	 * */
	private void listHeadSet() {
		// 添加顶部布局与初始化事件
		View header = View.inflate(mContext, R.layout.discovery_group_head,
				null);
		discoveryGroupListView.getRefreshableView().addHeaderView(header);
	}

	/**
	 * listView 的设置
	 * */
	private void newsListViewSet() {
		// 设置刷新模式
		discoveryGroupListView.setMode(Mode.PULL_FROM_START);
		// 刷新监听
		discoveryGroupListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// 下拉
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// 上拉
					}
				});

		/**
		 * adapter的设置
		 * */
		diacoveryGroupAdapter = new HelloHaAdapter<HashMap<String, String>>(
				mContext, R.layout.discovery_group_item, groupList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					HashMap<String, String> item) {
				// 数据绑定
				imgLoader.displayImage(item.get(GROUP_COVER_IMG),
						(ImageView) helper.getView(R.id.img_group_cover),
						options);
				helper.setText(R.id.text_group_name, item.get(GROUP_NAME));
				helper.setText(R.id.text_group_member_count,
						item.get(GROUP_MEMBER) + "人关注");
				// 设置事件监听
				final int postion = helper.getPosition();
				OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(View view) {
						itemViewClickListener.onClick(view, postion,
								view.getId());
					}
				};
				helper.setOnClickListener(R.id.layout_group_item_rootview,
						listener);
			}
		};

		// 设置不可点击
		diacoveryGroupAdapter.setItemsClickEnable(false);
		discoveryGroupListView.setAdapter(diacoveryGroupAdapter);
	}

	/**
	 * 获取发现部分的数据
	 * */
	private void getRecommentData(String userId, String page) {
		String path = JLXCConst.RECOMMEND_FRIENDS_LIST + "?" + "user_id="
				+ userId + "&page=" + page + "&size=";

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
							// 获取数据列表
							List<JSONObject> JPersonList = (List<JSONObject>) jResult
									.get("list");
							JsonToItemData(JPersonList);
							discoveryGroupListView.onRefreshComplete();
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							discoveryGroupListView.onRefreshComplete();
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络抽筋了,请检查 =_=");
						discoveryGroupListView.onRefreshComplete();
					}

				}, null));
	}

	/**
	 * 数据解析
	 * */
	private void JsonToItemData(List<JSONObject> dataList) {

	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int position, int viewID) {
			switch (viewID) {
			case R.id.layout_group_item_rootview:
				// 跳转到圈子内容页面
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
