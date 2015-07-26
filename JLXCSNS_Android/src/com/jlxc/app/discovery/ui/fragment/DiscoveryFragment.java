package com.jlxc.app.discovery.ui.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
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
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.discovery.model.PersonModel;
import com.jlxc.app.discovery.model.RecommendItemData;
import com.jlxc.app.discovery.model.RecommendItemData.RecommendInfoItem;
import com.jlxc.app.discovery.model.RecommendItemData.RecommendPhotoItem;
import com.jlxc.app.discovery.model.RecommendItemData.RecommendTitleItem;
import com.jlxc.app.discovery.ui.avtivity.ContactsUserActivity;
import com.jlxc.app.discovery.ui.avtivity.MipcaCaptureActivity;
import com.jlxc.app.discovery.ui.avtivity.SameSchoolActivity;
import com.jlxc.app.discovery.ui.avtivity.SearchUserActivity;
import com.jlxc.app.discovery.utils.DataToRecommendItem;
import com.jlxc.app.message.helper.MessageAddFriendHelper;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.personal.ui.activity.MyNewsListActivity;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class DiscoveryFragment extends BaseFragment {

	private final static int SCANNIN_GREQUEST_CODE = 1;
	
	private static final String LOOK_ALL_PHOTOS = "btn_all_photos";
	// 用户实例
	private UserModel userModel;
	// 上下文信息
	private Context mContext;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 标头
	@ViewInject(R.id.tv_discovey_title)
	private TextView titleTextView;
	// 扫一扫按钮
	@ViewInject(R.id.tv_dicovery_scan)
	private TextView sweepTextView;
	// 搜索框按钮
	@ViewInject(R.id.tv_discovey_search)
	private TextView searchTextView;
	// 推荐的人列表
	@ViewInject(R.id.listview_discovey)
	private PullToRefreshListView rcmdPersonListView;
	// 原始数据
	private List<PersonModel> personList = new ArrayList<PersonModel>();
	// item数据
	private List<RecommendItemData> itemDataList = new ArrayList<RecommendItemData>();
	// 适配器
	private HelloHaAdapter<RecommendItemData> personItemAdapter = null;
	// 使支持多种item
	private MultiItemTypeSupport<RecommendItemData> multiItemTypeRecommend = null;
	// 是否是最后一页数据
	private String lastPage = "0";
	// 当前的数据页
	private int currentPage = 1;
	// 是否下拉
	private boolean isPullDowm = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private ImageGridViewItemClick imageItemClickListener;

	/**
	 * 点击事件监听
	 * */
	@OnClick(value = { R.id.tv_dicovery_scan, R.id.tv_discovey_search })
	private void clickEvent(View view) {
		switch (view.getId()) {
		// 扫一扫页面
		case R.id.tv_dicovery_scan:
			Intent qrIntent = new Intent();
			qrIntent.setClass(getActivity(), MipcaCaptureActivity.class);
			qrIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().startActivityForResult(qrIntent, SCANNIN_GREQUEST_CODE);
			
			break;
		// 搜索页面
		case R.id.tv_discovey_search:
			Intent searchIntent = new Intent(getActivity(), SearchUserActivity.class);
			startActivityWithRight(searchIntent);
			break;
		}
	}

	@Override
	public int setLayoutId() {
		return R.layout.fragment_discovey_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	public void setUpViews(View rootView) {
		init();
		multiItemTypeSet();
		newsListViewSet();
		RecommendTitleItem titleItem = new RecommendTitleItem();
		personItemAdapter.add(titleItem);
		// 首次更新数据
		isPullDowm = true;
		getRecommentData(String.valueOf(userModel.getUid()),
				String.valueOf(currentPage));
	}

	private void init() {
		mContext = this.getActivity().getApplicationContext();
		userModel = UserManager.getInstance().getUser();

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		initBitmapUtils();

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
		bitmapUtils = new BitmapUtils(mContext);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * listView 支持多种item的设置
	 * */
	private void multiItemTypeSet() {
		multiItemTypeRecommend = new MultiItemTypeSupport<RecommendItemData>() {

			@Override
			public int getLayoutId(int position, RecommendItemData itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case RecommendItemData.RECOMMEND_TITLE:
					layoutId = R.layout.discovery_item_head;
					break;
				case RecommendItemData.RECOMMEND_INFO:
					layoutId = R.layout.discovery_item_person_info;
					break;
				case RecommendItemData.RECOMMEND_PHOTOS:
					layoutId = R.layout.discovery_item_photolist;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return RecommendItemData.RECOMMEND_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, RecommendItemData itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case RecommendItemData.RECOMMEND_TITLE:
					itemtype = RecommendItemData.RECOMMEND_TITLE;
					break;
				case RecommendItemData.RECOMMEND_INFO:
					itemtype = RecommendItemData.RECOMMEND_INFO;
					break;
				case RecommendItemData.RECOMMEND_PHOTOS:
					itemtype = RecommendItemData.RECOMMEND_PHOTOS;
					break;

				default:
					break;
				}
				return itemtype;
			}
		};
	}

	/**
	 * listView 的设置
	 * */
	private void newsListViewSet() {
		// 设置刷新模式
		rcmdPersonListView.setMode(Mode.BOTH);

		// 刷新监听
		rcmdPersonListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						userModel = UserManager.getInstance().getUser();
						currentPage = 1;
						isPullDowm = true;
						getRecommentData(String.valueOf(userModel.getUid()),
								String.valueOf(currentPage));
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (lastPage.equals("1")) {
							rcmdPersonListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									rcmdPersonListView.onRefreshComplete();
								}
							}, 1000);
							ToastUtil.show(mContext, "没有数据了,哦哦");
						} else {
							isPullDowm = false;
							getRecommentData(
									String.valueOf(userModel.getUid()),
									String.valueOf(currentPage));
						}
					}
				});

		/**
		 * adapter的设置
		 * */
		personItemAdapter = new HelloHaAdapter<RecommendItemData>(mContext,
				itemDataList, multiItemTypeRecommend) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					RecommendItemData item) {

				switch (helper.layoutId) {
				case R.layout.discovery_item_head:
					setTitleItemView(helper, item);
					break;
				case R.layout.discovery_item_person_info:
					setInfoItemView(helper, item);
					break;
				case R.layout.discovery_item_photolist:
					LogUtils.i("调用setPhotoItemView");
					setPhotoItemView(helper, item);
					break;

				default:
					break;
				}
			}
		};
		/**
		 * 设置底部自动刷新
		 * */
		rcmdPersonListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (lastPage.equals("1")) {
							rcmdPersonListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									rcmdPersonListView.onRefreshComplete();
								}
							}, 1000);
							ToastUtil.show(mContext, "没有数据了,哦哦");
						} else {
							rcmdPersonListView.setMode(Mode.PULL_FROM_END);
							rcmdPersonListView.setRefreshing(true);
							isPullDowm = false;
							getRecommentData(
									String.valueOf(userModel.getUid()),
									String.valueOf(currentPage));
						}
					}
				});
		// 快速滑动时不加载图片
		rcmdPersonListView.setOnScrollListener(new PauseOnScrollListener(
				bitmapUtils, false, true));
		// 设置不可点击
		personItemAdapter.setItemsClickEnable(false);
		rcmdPersonListView.setAdapter(personItemAdapter);
	}

	/**
	 * 设置title
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			RecommendItemData item) {
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.layout_add_contact_root_view, listener);
		helper.setOnClickListener(R.id.layout_add_campus_root_view, listener);
	}

	/**
	 * 设置基本信息
	 * */
	private void setInfoItemView(HelloHaBaseAdapterHelper helper,
			RecommendItemData item) {
		RecommendInfoItem titleData = (RecommendInfoItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.iv_recommend_head);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.iv_recommend_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户信息
		helper.setText(R.id.tv_recommend_name, titleData.getUserName());
		helper.setText(R.id.tv_recommend_tag, titleData.getRelationTag());
		helper.setText(R.id.tv_recommend_school, titleData.getUserSchool());
		
		Button addButton = helper.getView(R.id.btn_recomment_add);
		if (titleData.isAdd()) {
			addButton.setEnabled(false);
			addButton.setText("已添加");
		}else {
			addButton.setText("添加");
			addButton.setEnabled(true);
		}
		
		final int postion = helper.getPosition();
		if (1 == postion) {
			helper.setVisible(R.id.view_recommend_driver, false);
		} else {
			helper.setVisible(R.id.view_recommend_driver, true);
		}
		
		// 监听事件
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.layout_recommend_info_rootview, listener);
		helper.setOnClickListener(R.id.btn_recomment_add, listener);
	}

	/**
	 * 设置照片
	 * */
	private void setPhotoItemView(HelloHaBaseAdapterHelper helper,
			RecommendItemData item) {
		RecommendPhotoItem photoListData = (RecommendPhotoItem) item;
		// 封装数据
		List<Map<String, String>> photoInfoList = new ArrayList<Map<String, String>>();
		for (int index = 0; index < photoListData.getPhotoSubUrl().size(); index++) {
			Map<String, String> tpMap = new HashMap<String, String>();
			tpMap.put("USER_ID", photoListData.getUserId());
			tpMap.put("PHOTO_SUB_URL", photoListData.getPhotoSubUrl()
					.get(index));
			if (null != photoListData.getPhotoUrl()
					&& index < photoListData.getPhotoUrl().size()) {
				tpMap.put("PHOTO_URL", photoListData.getPhotoUrl().get(index));
			}
			photoInfoList.add(tpMap);
		}

		// 照片的尺寸,正方形显示
		final int photoSize = (screenWidth - 20) / 3;
		HelloHaAdapter<Map<String, String>> newsGVAdapter = new HelloHaAdapter<Map<String, String>>(
				mContext, R.layout.discovery_photos_gridview_item,
				photoInfoList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					Map<String, String> data) {
				String subAdd = data.get("PHOTO_SUB_URL");
				if (subAdd.equals(LOOK_ALL_PHOTOS)) {
					// 添加查看所有照片按钮
					ImageView imgView = helper
							.getView(R.id.iv_recommend_photos_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = photoSize / 2;
					laParams.height = photoSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.FIT_XY);
					helper.setImageResource(R.id.iv_recommend_photos_item,
							R.drawable.btn_moer_photo);
				} else {
					// 设置相册的尺寸的图片大小
					ImageView imgView = helper
							.getView(R.id.iv_recommend_photos_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = photoSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_recommend_photos_item,
							bitmapUtils, subAdd, new NewsBitmapLoadCallBack());
				}
			}
		};
		NoScrollGridView photoGridView = (NoScrollGridView) helper
				.getView(R.id.gv_recommend_photos);
		// 设置gridview的尺寸
		int photoCount = photoInfoList.size();
		int gridviewWidth = (int) ((photoCount - 1) * (photoSize + 4) + photoSize / 2);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
		photoGridView.setColumnWidth(photoSize);
		photoGridView.setHorizontalSpacing(4);
		photoGridView.setStretchMode(GridView.NO_STRETCH);
		photoGridView.setNumColumns(photoCount);
		photoGridView.setLayoutParams(params);

		photoGridView.setAdapter(newsGVAdapter);

		/**
		 * 点击图片事件
		 * */
		photoGridView.setOnItemClickListener(imageItemClickListener);
	}

	/**
	 * 获取推荐的人的数据
	 * */
	private void getRecommentData(String userId, String page) {
		String path = JLXCConst.RECOMMEND_FRIENDS_LIST + "?" + "user_id=" + userId/* userId */
				+ "&page=" + page + "&size=";

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
							lastPage = jResult.getString("is_last");
							if (lastPage.equals("0")) {
								currentPage++;
							}
							rcmdPersonListView.onRefreshComplete();
							rcmdPersonListView.setMode(Mode.BOTH);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							rcmdPersonListView.onRefreshComplete();
							rcmdPersonListView.setMode(Mode.BOTH);
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络有毒=_=");
						rcmdPersonListView.onRefreshComplete();
						rcmdPersonListView.setMode(Mode.BOTH);
					}

				}, null));
	}

	/**
	 * 数据解析
	 * */
	private void JsonToItemData(List<JSONObject> dataList) {
		if (isPullDowm) {
			//下拉
			personList.clear();
			for (JSONObject newsObj : dataList) {
				PersonModel tempPerson = new PersonModel();
				tempPerson.setContentWithJson(newsObj);
				if (tempPerson.getImageList().size() >= 3) {
					tempPerson.getImageList().add(LOOK_ALL_PHOTOS);
				}
				personList.add(tempPerson);
			}

		} else {
			//上拉
			HashSet<PersonModel> DuplicateSet = new HashSet<PersonModel>(
					personList);
			for (JSONObject newsObj : dataList) {
				PersonModel tempPerson = new PersonModel();
				tempPerson.setContentWithJson(newsObj);
				if (tempPerson.getImageList().size() >= 3) {
					tempPerson.getImageList().add(LOOK_ALL_PHOTOS);
				}
				if (DuplicateSet.add(tempPerson)) {
					personList.add(tempPerson);
				}
			}
		}
		itemDataList = DataToRecommendItem.dataToItems(personList);
		RecommendTitleItem titleItem = new RecommendTitleItem();
		itemDataList.add(0, titleItem);
		personItemAdapter.replaceAll(itemDataList);

		if (null != dataList) {
			dataList.clear();
		}
	}

	/**
	 * 去除重复的值
	 * */

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int position, int viewID) {
			switch (viewID) {
			case R.id.layout_add_contact_root_view:
				// 跳转到添加通讯录好友页面
				Intent intentToContacts = new Intent(mContext,
						ContactsUserActivity.class);
				startActivityWithRight(intentToContacts);
				break;

			// 添加同校好友
			case R.id.layout_add_campus_root_view:
				Intent sameSchoolIntent = new Intent(getActivity(), SameSchoolActivity.class);
				startActivityWithRight(sameSchoolIntent);
				break;
				
			// 点击推荐的人
			case R.id.layout_recommend_info_rootview:
				RecommendInfoItem currentInfoItem = (RecommendInfoItem) personItemAdapter
						.getItem(position);
				JumpToHomepage(JLXCUtils.stringToInt(currentInfoItem
						.getUserID()));
				break;

			// 点击添加按钮
			case R.id.btn_recomment_add:
				RecommendInfoItem addInfoItem = (RecommendInfoItem) personItemAdapter
				.getItem(position);
				IMModel imModel = new IMModel();
				imModel.setAvatarPath(addInfoItem.getHeadImage());
				imModel.setTargetId(JLXCConst.JLXC+addInfoItem.getUserID());
				imModel.setTitle(addInfoItem.getUserName());
				addFriend(imModel, position);
				
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

	/**
	 * 图片gridview监听
	 */
	public class ImageGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			@SuppressWarnings("unchecked")
			HelloHaAdapter<Map<String, String>> photosAdapter = (HelloHaAdapter<Map<String, String>>) parent
					.getAdapter();
			Map<String, String> currentMap = photosAdapter.getItem(position);
			String currentImgPath = currentMap.get("PHOTO_SUB_URL");
			if (!currentImgPath.equals(LOOK_ALL_PHOTOS)) {
				// 跳转到图片详情页面
				List<String> imageList = new ArrayList<String>();
				for (int index = 0; index < photosAdapter.getCount() - 1; index++) {
					imageList.add(photosAdapter.getItem(index).get(
							"PHOTO_SUB_URL"));
				}
				jumpToBigImage(BigImgLookActivity.INTENT_KEY_IMG_LIST,
						imageList, position);
			} else {
				// 跳转到动态列表
				Intent intent = new Intent(mContext, MyNewsListActivity.class);
				intent.putExtra(MyNewsListActivity.ITNET_KEY_UID,
						currentMap.get("USER_ID"));
				startActivity(intent);
			}
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
	 * 跳转查看大图
	 */
	private void jumpToBigImage(String intentKey, Object path, int index) {
		if (intentKey.equals(BigImgLookActivity.INTENT_KEY)) {
			// 单张图片跳转
			String pathUrl = (String) path;
			Intent intentPicDetail = new Intent(mContext,
					BigImgLookActivity.class);
			intentPicDetail.putExtra(BigImgLookActivity.INTENT_KEY, pathUrl);
			startActivity(intentPicDetail);
		} else if (intentKey
				.equals(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST)) {
			// 传递model列表
			@SuppressWarnings("unchecked")
			List<ImageModel> mdPath = (List<ImageModel>) path;
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			startActivity(intent);
		} else if (intentKey.equals(BigImgLookActivity.INTENT_KEY_IMG_LIST)) {
			// 传递String列表
			@SuppressWarnings("unchecked")
			List<String> mdPath = (List<String>) path;
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			startActivity(intent);
		} else {
			LogUtils.e("未传递图片地址");
		}
	}

	/**
	 * 加载图片时的回调函数
	 * */
	public class NewsBitmapLoadCallBack extends
			DefaultBitmapLoadCallBack<ImageView> {
		private final ImageView iView;

		public NewsBitmapLoadCallBack() {
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
	
	//添加好友
	private void addFriend(final IMModel imModel, final int index) {

		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", UserManager.getInstance().getUser().getUid()+"");
		params.addBodyParameter("friend_id", imModel.getTargetId().replace(JLXCConst.JLXC, "")+"");
		
		showLoading(getActivity() ,"添加中^_^", false);
		HttpManager.post(JLXCConst.Add_FRIEND, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						
						hideLoading();
						int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
						ToastUtil.show(getActivity(),jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
						
						if (status == JLXCConst.STATUS_SUCCESS) {
							//添加好友
							MessageAddFriendHelper.addFriend(imModel);
							//更新
							RecommendInfoItem recommendItemData = (RecommendInfoItem) itemDataList.get(index);
							recommendItemData.setAdd("1");
							personItemAdapter.replaceAll(itemDataList);
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						ToastUtil.show(getActivity(),
								"网络异常");
					}
				}, null));
	}
}
