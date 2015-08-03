package com.jlxc.app.news.ui.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

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
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.ui.view.NoScrollGridView.OnTouchInvalidPositionListener;
import com.jlxc.app.base.ui.view.RoundImageView;
import com.jlxc.app.base.utils.HttpCacheUtils;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CampusPersonModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.OperateItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.NewsConstants;
import com.jlxc.app.news.ui.activity.CampusAllPersonActivity;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
import com.jlxc.app.news.ui.fragment.NewsListFragment.NewsBitmapLoadCallBack;
import com.jlxc.app.news.utils.DataToItem;
import com.jlxc.app.news.utils.NewsOperate;
import com.jlxc.app.news.utils.NewsOperate.LikeCallBack;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

public class CampusFragment extends BaseFragment {

	// 最多点赞数
	private int MAX_LIKE_COUNT = 10;
	// 最多点赞数默认为8
	private int maxLikeCount = 8;
	// 点赞列表的宽度
	private int likeheadGVWidth;
	// 点赞头像的尺寸
	private int headImageSize;
	// 点赞头像的间距
	private int likeHeadSpace = 4;
	// 动态listview
	@ViewInject(R.id.campus_listview)
	private PullToRefreshListView campusListView;
	// 原始数据源
	private List<NewsModel> newsList = new ArrayList<NewsModel>();
	// item数据源
	private List<ItemModel> itemDataList = null;
	// 动态列表适配器
	private HelloHaAdapter<ItemModel> newsAdapter = null;
	// 学校的人
	private List<CampusPersonModel> personList;
	// 动态的图片适配器
	private HelloHaAdapter<ImageModel> newsGVAdapter;
	// 学校的人的头像
	private GridView personHeadGridView;
	// 使支持多种item
	private MultiItemTypeSupport<ItemModel> multiItemTypeCampus = null;
	// 上下文信息
	private Context mContext;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 当前的数据页
	private int pageIndex = 1;
	// 是否是最后一页数据
	private boolean islastPage = false;
	// 时间戳
	private String latestTimesTamp = "";
	// 是否下拉
	private boolean isPullDowm = true;
	// 是否正在请求数据
	private boolean isRequestData = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private ImageGridViewItemClick imageItemClickListener;
	// 点击头像监听
	private LikeGridViewItemClick likeItemClickListener;
	// 点赞操作类
	// 当前点赞对应的gridview的adpter
	private HelloHaAdapter<LikeModel> curntAdapter = null;
	// 对动态的操作
	private NewsOperate newsOPerate;
	// 当前点赞对应的icon
	private TextView likeIcon = null;
	// 所有点赞的对应的icon
	private TextView allLikeIcon = null;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_campus_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	public void setUpViews(View rootView) {
		init();
		initBoradcastReceiver();
		multiItemTypeSet();
		newsListViewSet();
		// 获取上次缓存的数据
		setLastData(UserManager.getInstance().getUser().getUid(), UserManager
				.getInstance().getUser().getSchool_code());
		// 进入本页面时请求数据
		getCampusData(
				String.valueOf(UserManager.getInstance().getUser().getUid()),
				String.valueOf(pageIndex), UserManager.getInstance().getUser()
						.getSchool_code(), "");
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		newsOPerate = new NewsOperate(mContext);
		initBitmapUtils();

		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;

		// 计算显示的头像个数
		if (screenWidth <= NewsConstants.SMALL_PIX) {
			maxLikeCount = 8;
		} else if (screenWidth > NewsConstants.SMALL_PIX
				&& screenWidth <= NewsConstants.MIDDLE_PIX) {
			maxLikeCount = 9;
		} else if (screenWidth > NewsConstants.MIDDLE_PIX) {
			maxLikeCount = 10;
		}
		int leftSpace = (int) getResources().getDimension(
				R.dimen.news_item_margins_left);
		int rightSpace = (int) getResources().getDimension(
				R.dimen.news_item_margins_right);
		headImageSize = (screenWidth - (leftSpace + rightSpace + maxLikeCount
				* likeHeadSpace))
				/ (maxLikeCount + 2);
		likeheadGVWidth = (headImageSize + likeHeadSpace) * maxLikeCount
				- likeHeadSpace;
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
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = BitmapManager.getInstance().getBitmapUtils(mContext,
				true, true);

		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
	}

	/***
	 * 上次缓存的数据
	 * */
	@SuppressWarnings("unchecked")
	private void setLastData(int userID, String schoolCode) {
		String path = JLXCConst.SCHOOL_NEWS_LIST + "?" + "user_id=" + userID
				+ "&page=" + 1 + "&school_code=" + schoolCode + "&frist_time=";

		JSONObject JObject = HttpCacheUtils.getHttpCache(path);
		if (null != JObject) {
			JSONObject jResult = JObject.getJSONObject(JLXCConst.HTTP_RESULT);
			if (null != jResult) {
				// 获取数据列表
				List<JSONObject> JNewsList = (List<JSONObject>) jResult
						.get(JLXCConst.HTTP_LIST);
				List<JSONObject> JPersonList = (List<JSONObject>) jResult
						.get("info");

				if (null != JNewsList && null != JPersonList) {
					JsonToItemData(JNewsList, JPersonList);
				}
			}
		}
	}

	/**
	 * listView 支持多种item的设置
	 * */
	private void multiItemTypeSet() {
		multiItemTypeCampus = new MultiItemTypeSupport<ItemModel>() {

			@Override
			public int getLayoutId(int position, ItemModel itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case ItemModel.CAMPUS_TITLE:
					layoutId = R.layout.campus_news_item_title_layout;
					break;
				case ItemModel.CAMPUS_BODY:
					layoutId = R.layout.campus_news_item_body_layout;
					break;
				case ItemModel.CAMPUS_OPERATE:
					layoutId = R.layout.campus_news_item_operate_layout;
					break;
				case ItemModel.CAMPUS_LIKELIST:
					layoutId = R.layout.campus_news_item_likelist_layout;
					break;
				case ItemModel.CAMPUS_HEAD:
					layoutId = R.layout.campus_head_item_layout;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return ItemModel.CAMPUS_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, ItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case ItemModel.CAMPUS_TITLE:
					itemtype = ItemModel.CAMPUS_TITLE;
					break;
				case ItemModel.CAMPUS_BODY:
					itemtype = ItemModel.CAMPUS_BODY;
					break;
				case ItemModel.CAMPUS_OPERATE:
					itemtype = ItemModel.CAMPUS_OPERATE;
					break;
				case ItemModel.CAMPUS_LIKELIST:
					itemtype = ItemModel.CAMPUS_LIKELIST;
					break;
				case ItemModel.CAMPUS_HEAD:
					itemtype = ItemModel.CAMPUS_HEAD;
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
		campusListView.setMode(Mode.BOTH);
		/**
		 * 刷新监听
		 * */
		campusListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (!isRequestData) {
					isRequestData = true;
					pageIndex = 1;
					isPullDowm = true;
					getCampusData(
							String.valueOf(UserManager.getInstance().getUser()
									.getUid()), String.valueOf(pageIndex),
							UserManager.getInstance().getUser()
									.getSchool_code(), "");
				}
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (!islastPage && !isRequestData) {
					isRequestData = true;
					isPullDowm = false;
					getCampusData(
							String.valueOf(UserManager.getInstance().getUser()
									.getUid()), String.valueOf(pageIndex),
							UserManager.getInstance().getUser()
									.getSchool_code(), latestTimesTamp);
				}
			}
		});

		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<ItemModel>(mContext, itemDataList,
				multiItemTypeCampus) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					ItemModel item) {

				switch (helper.layoutId) {
				case R.layout.campus_news_item_title_layout:
					setTitleItemView(helper, item);
					break;
				case R.layout.campus_news_item_body_layout:
					setBodyItemView(helper, item);
					break;
				case R.layout.campus_news_item_operate_layout:
					setOperateItemView(helper, item);
					break;
				case R.layout.campus_news_item_likelist_layout:
					setLikeListItemView(helper, item);
					break;
				case R.layout.campus_head_item_layout:
					setCampusHeadView(helper, item);
					break;

				default:
					break;
				}
			}
		};
		/**
		 * 设置底部自动刷新
		 * */
		campusListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (!islastPage && !isRequestData) {
							isRequestData = true;
							campusListView.setMode(Mode.PULL_FROM_END);
							campusListView.setRefreshing(true);
							isPullDowm = false;
							getCampusData(
									String.valueOf(UserManager.getInstance()
											.getUser().getUid()),
									String.valueOf(pageIndex), UserManager
											.getInstance().getUser()
											.getSchool_code(), latestTimesTamp);
						}
					}
				});
		// 快速滑动时不加载图片
		campusListView.setOnScrollListener(new PauseOnScrollListener(
				bitmapUtils, false, true));
		// 设置不可点击
		newsAdapter.setItemsClickEnable(false);
		campusListView.setAdapter(newsAdapter);
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		RoundImageView imgView = helper.getView(R.id.img_campus_user_head);
		imgView.setRectAdius(5);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 7;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		helper.setImageUrl(R.id.img_campus_user_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间，标签
		helper.setText(R.id.txt_campus_user_name, titleData.getUserName());
		helper.setText(R.id.txt_campus_publish_time,
				TimeHandle.getShowTimeFormat(titleData.getSendTime()));

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.img_campus_user_head, listener);
		helper.setOnClickListener(R.id.txt_campus_user_name, listener);
		helper.setOnClickListener(R.id.layout_campus_title_rootview, listener);
	}

	/**
	 * 设置新闻主题item
	 * */
	private void setBodyItemView(HelloHaBaseAdapterHelper helper, ItemModel item) {
		BodyItem bodyData = (BodyItem) item;
		List<ImageModel> pictureList = bodyData.getNewsImageListList();
		// 绑定图片显示
		if (pictureList.size() == 0) {
			// 没有图片的情况
			helper.setVisible(R.id.gv_campus_news_body_image, false);
			helper.setVisible(R.id.iv_campus_news_body_picture, false);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			helper.setVisible(R.id.gv_campus_news_body_image, false);
			helper.setVisible(R.id.iv_campus_news_body_picture, true);
			ImageView imgView = helper
					.getView(R.id.iv_campus_news_body_picture);
			ImageModel imageModel = pictureList.get(0);
			LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
			if (imageModel.getImageHheight() >= imageModel.getImageWidth()) {
				laParams.height = screenWidth * 4 / 5;
				laParams.width = (int) ((imageModel.getImageWidth()
						* screenWidth * 4) / (5.0 * imageModel
						.getImageHheight()));
			} else {
				laParams.height = (int) ((imageModel.getImageHheight()
						* screenWidth * 4) / (5.0 * imageModel.getImageWidth()));
				laParams.width = screenWidth * 4 / 5;
			}
			imgView.setLayoutParams(laParams);
			imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
					screenWidth * 4 / 5);
			helper.setImageUrl(R.id.iv_campus_news_body_picture, bitmapUtils,
					imageModel.getURL(), new NewsBitmapLoadCallBack());

			// 设置点击事件
			final int postion = helper.getPosition();
			imgView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					itemViewClickListener.onClick(view, postion, view.getId());
				}
			});
		} else {
			// 多张图片以九宫格显示
			helper.setVisible(R.id.gv_campus_news_body_image, true);
			helper.setVisible(R.id.iv_campus_news_body_picture, false);
			NoScrollGridView bodyGridView = (NoScrollGridView) helper
					.getView(R.id.gv_campus_news_body_image);
			newsGVAdapter = new HelloHaAdapter<ImageModel>(mContext,
					R.layout.campus_news_body_gridview_item_layout, pictureList) {
				@Override
				protected void convert(HelloHaBaseAdapterHelper helper,
						ImageModel item) {
					// 设置显示图片的imageView大小
					int desSize = (screenWidth - 20) / 3;
					ImageView imgView = helper
							.getView(R.id.iv_campus_body_gridview_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = desSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_campus_body_gridview_item,
							bitmapUtils, item.getSubURL(),
							new NewsBitmapLoadCallBack());
				}
			};
			bodyGridView.setAdapter(newsGVAdapter);

			/**
			 * 点击图片事件
			 * */
			bodyGridView.setOnItemClickListener(imageItemClickListener);
			// 点击空白区域时将事件传回给父控件
			bodyGridView
					.setOnTouchInvalidPositionListener(new OnTouchInvalidPositionListener() {

						@Override
						public boolean onTouchInvalidPosition(int motionEvent) {
							return false;
						}
					});
		}

		// 设置点击事件
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.layout_campus_body_root_view, listener);

		// 设置 文字内容
		if (bodyData.getNewsContent().equals("")) {
			helper.setVisible(R.id.txt_campus_news_content, false);
		} else {
			helper.setVisible(R.id.txt_campus_news_content, true);
			helper.setText(R.id.txt_campus_news_content,
					bodyData.getNewsContent());
			helper.setOnClickListener(R.id.txt_campus_news_content, listener);
		}
		// 设置地理位置
		if (bodyData.getLocation().equals("")) {
			helper.setVisible(R.id.txt_campus_news_location, false);
		} else {
			helper.setVisible(R.id.txt_campus_news_location, true);
			helper.setText(R.id.txt_campus_news_location,
					bodyData.getLocation());
		}
	}

	/**
	 * 设置操作部分item
	 * */
	private void setOperateItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		OperateItem opData = (OperateItem) item;
		if (opData.getIsLike()) {
			helper.setText(R.id.tv_campus_news_like, "已赞 ");
			helper.setImageResource(R.id.iv_campus_news_like_background,
					R.drawable.like_have);
		} else {
			helper.setText(R.id.tv_campus_news_like, "点赞 ");
			helper.setImageResource(R.id.iv_campus_news_like_background,
					R.drawable.like_no);
		}
		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.btn_campus_reply, listener);
		helper.setOnClickListener(R.id.btn_campus_like, listener);
		helper.setOnClickListener(R.id.layout_campus_operate_root_view,
				listener);
	}

	/**
	 * 设置点赞部分item
	 * */
	private void setLikeListItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		LikeListItem lkData = (LikeListItem) item;
		List<LikeModel> lkImageList = lkData.getLikeHeadListimage();
		if (lkImageList.size() <= 0) {
			helper.getView(R.id.tv_campus_like_icon).setVisibility(View.GONE);
		} else {
			helper.getView(R.id.tv_campus_like_icon)
					.setVisibility(View.VISIBLE);
		}
		if (lkImageList.size() < maxLikeCount) {
			helper.getView(R.id.tv_campus_like_all_person).setVisibility(
					View.GONE);
		} else {
			helper.getView(R.id.tv_campus_like_all_person).setVisibility(
					View.VISIBLE);
			helper.setText(R.id.tv_campus_like_all_person,
					String.valueOf(lkData.getLikeCount()));
		}
		while (lkImageList.size() > maxLikeCount) {
			// 移除多余 的头像
			lkImageList.remove(maxLikeCount);
		}
		// 点赞头像的显示
		HelloHaAdapter<LikeModel> likeGVAdapter = new HelloHaAdapter<LikeModel>(
				mContext, R.layout.campus_news_likelist_gridview_item_layout,
				lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				// 设置头像imageview的尺寸
				RoundImageView imgView = helper
						.getView(R.id.iv_campus_like_gridview_item);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = headImageSize;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				// 圆角的角度
				imgView.setRectAdius(3);
				helper.setImageUrl(R.id.iv_campus_like_gridview_item,
						bitmapUtils, item.getHeadSubImage(),
						new NewsBitmapLoadCallBack());
			}
		};
		// 点赞头像gridview
		NoScrollGridView likeGridView = (NoScrollGridView) helper
				.getView(R.id.gv_campus_like_list);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				likeheadGVWidth, headImageSize);
		// 参数设置
		likeGridView.setHorizontalSpacing(likeHeadSpace);
		likeGridView.setNumColumns(maxLikeCount);
		likeGridView.setLayoutParams(params);

		likeGridView.setAdapter(likeGVAdapter);
		likeGridView.setOnItemClickListener(likeItemClickListener);
		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.tv_campus_like_all_person, listener);
	}

	/**
	 * 设置头部item
	 * */
	private void setCampusHeadView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		helper.setText(R.id.tv_campus_head_name, UserManager.getInstance()
				.getUser().getSchool());

		// 头像的尺寸,正方形显示
		final int headImageSize = screenWidth / 6;
		HelloHaAdapter<CampusPersonModel> personGVAdapter = new HelloHaAdapter<CampusPersonModel>(
				mContext, R.layout.campus_head_person_gridview_item_layout,
				personList) {

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
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定图片
				bitmapUtils.configDefaultBitmapMaxSize(laParams.width,
						laParams.width);
				helper.setImageUrl(R.id.iv_campus_person_gridview_item,
						bitmapUtils, item.getHeadSubImage(),
						new NewsBitmapLoadCallBack());
			}
		};
		personHeadGridView = (GridView) helper.getView(R.id.gv_school_person);
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
	}

	/**
	 * 获取学校动态的数据
	 * */
	private void getCampusData(String userID, String desPage,
			String schoolCode, String lastTime) {
		String path = JLXCConst.SCHOOL_NEWS_LIST + "?" + "user_id=" + userID
				+ "&page=" + desPage + "&school_code=" + schoolCode
				+ "&frist_time=" + lastTime;

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
							List<JSONObject> JNewsList = (List<JSONObject>) jResult
									.get("list");
							List<JSONObject> JPersonList = null;
							if (jResult.containsKey("info")) {
								JPersonList = (List<JSONObject>) jResult
										.get("info");
							}
							JsonToItemData(JNewsList, JPersonList);
							campusListView.onRefreshComplete();

							if (jResult.getString("is_last").equals("0")) {
								islastPage = false;
								pageIndex++;
								campusListView.setMode(Mode.BOTH);
							} else {
								islastPage = true;
								campusListView.setMode(Mode.PULL_FROM_START);
							}
							isRequestData = false;
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							campusListView.onRefreshComplete();
							campusListView.setMode(Mode.PULL_FROM_START);
							isRequestData = false;
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络有毒=_=");
						campusListView.onRefreshComplete();
						campusListView.setMode(Mode.BOTH);
						isRequestData = false;
					}

				}, null));
	}

	/**
	 * 数据处理
	 */
	private void JsonToItemData(List<JSONObject> JNewsList,
			List<JSONObject> JPersonList) {
		// 校园的动态
		List<NewsModel> newsList = new ArrayList<NewsModel>();
		for (JSONObject newsObj : JNewsList) {
			NewsModel tempNews = new NewsModel();
			tempNews.setContentWithJson(newsObj);
			newsList.add(tempNews);
		}

		personList = new ArrayList<CampusPersonModel>();
		if (isPullDowm) {
			// 解析校园的人
			for (JSONObject personObj : JPersonList) {
				CampusPersonModel tempPerson = new CampusPersonModel();
				tempPerson.setContentWithJson(personObj);
				personList.add(tempPerson);
			}
			this.newsList = newsList;
			if (newsList.size() > 0) {
				latestTimesTamp = newsList.get(0).getTimesTamp();
			}
			if (null != itemDataList) {
				itemDataList.clear();
			}
			itemDataList = DataToItem.campusDataToItems(newsList, personList);
			newsAdapter.replaceAll(itemDataList);
		} else {
			// 加载更多动态信息
			this.newsList.addAll(newsList);
			itemDataList.addAll(DataToItem.campusDataToItems(newsList,
					personList));
			newsAdapter.replaceAll(itemDataList);
		}
		if (null != JNewsList) {
			JNewsList.clear();
		}
		if (null != JPersonList) {
			JPersonList.clear();
		}
	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.img_campus_user_head:
			case R.id.txt_campus_user_name:
			case R.id.layout_campus_title_rootview:
				TitleItem titleData = (TitleItem) newsAdapter.getItem(postion);
				if (R.id.layout_campus_title_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(titleData, NewsConstants.KEY_BOARD_CLOSE,
							null);
				} else {
					// 跳转到用户的主页
					JumpToHomepage(JLXCUtils.stringToInt(titleData.getUserID()));
				}
				break;

			case R.id.iv_campus_news_body_picture:
			case R.id.layout_campus_body_root_view:
			case R.id.txt_campus_news_content:
				BodyItem bodyData = (BodyItem) newsAdapter.getItem(postion);
				if (R.id.iv_campus_news_body_picture == viewID) {
					String path = bodyData.getNewsImageListList().get(0)
							.getURL();
					// 跳转到图片详情页面
					jumpToBigImage(BigImgLookActivity.INTENT_KEY, path, 0);
				} else {
					// 跳转到动态详情
					jumpToNewsDetail(bodyData, NewsConstants.KEY_BOARD_CLOSE,
							null);
				}
				break;

			case R.id.btn_campus_reply:
			case R.id.btn_campus_like:
			case R.id.layout_campus_operate_root_view:
				OperateItem operateData = (OperateItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_campus_operate_root_view == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(operateData,
							NewsConstants.KEY_BOARD_CLOSE, null);
				} else if (R.id.btn_campus_reply == viewID) {
					// 跳转至评论页面并打开评论框
					jumpToNewsDetail(operateData,
							NewsConstants.KEY_BOARD_COMMENT, null);
				} else {
					// 点赞操作
					likeOperate(postion, view, operateData);
				}
				break;

			case R.id.tv_campus_person_title:
				// 跳转到所有好友列表页面
				Intent personIntent = new Intent(mContext,
						CampusAllPersonActivity.class);
				personIntent.putExtra("School_Code", UserManager.getInstance()
						.getUser().getSchool_code());
				startActivityWithRight(personIntent);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 点赞操作
	 * */
	@SuppressWarnings("unchecked")
	private void likeOperate(int postion, View view,
			final OperateItem operateData) {

		final FrameLayout oprtView = (FrameLayout) view;
		final int likeListPostion = postion + 1;
		try {
			ListView nListView = campusListView.getRefreshableView();
			View itemRootView = nListView.getChildAt(likeListPostion + 1
					- nListView.getFirstVisiblePosition());
			curntAdapter = null;
			if (null != itemRootView) {
				// 点赞头像列表可见的情况下
				NoScrollGridView likeGV = (NoScrollGridView) itemRootView
						.findViewById(R.id.gv_campus_like_list);
				curntAdapter = (HelloHaAdapter<LikeModel>) likeGV.getAdapter();
				likeIcon = (TextView) itemRootView
						.findViewById(R.id.tv_campus_like_icon);
				allLikeIcon = (TextView) itemRootView
						.findViewById(R.id.tv_campus_like_all_person);
			}
		} catch (Exception e) {
			LogUtils.e("动态点赞部分发生异常.");
		}

		newsOPerate.setLikeListener(new LikeCallBack() {

			@Override
			public void onOperateStart(boolean isLike) {
				if (isLike) {
					// 点赞操作
					if (null != curntAdapter) {
						newsOPerate.addHeadToLikeList(curntAdapter);
					}
					newsOPerate.addDataToLikeList(newsAdapter, likeListPostion);
					((TextView) oprtView.findViewById(R.id.tv_campus_news_like))
							.setText("已赞 ");
					((ImageView) oprtView
							.findViewById(R.id.iv_campus_news_like_background))
							.setImageResource(R.drawable.like_have);
					if (curntAdapter.getCount() > 0) {
						// 显示点赞图标
						likeIcon.setVisibility(View.VISIBLE);
					}
					if (curntAdapter.getCount() > maxLikeCount) {
						// 显示所有点赞的人的按钮
						allLikeIcon.setVisibility(View.VISIBLE);
						operateData.setLikeCount(String.valueOf(operateData
								.getLikeCount() + 1));
						allLikeIcon.setText(operateData.getLikeCount());
					}
					operateData.setIsLike("1");
				} else {
					// 取消点赞
					if (null != curntAdapter) {
						newsOPerate.removeHeadFromLikeList(curntAdapter);
					}
					newsOPerate.removeDataFromLikeList(newsAdapter,
							likeListPostion);

					((TextView) oprtView.findViewById(R.id.tv_campus_news_like))
							.setText("点赞 ");
					((ImageView) oprtView
							.findViewById(R.id.iv_campus_news_like_background))
							.setImageResource(R.drawable.like_no);
					if (curntAdapter.getCount() <= 0) {
						// 隐藏点赞图标
						likeIcon.setVisibility(View.GONE);
					}
					if (curntAdapter.getCount() <= maxLikeCount) {
						// 隐藏所有点赞的人的按钮
						allLikeIcon.setVisibility(View.GONE);
					}
					operateData.setIsLike("0");
				}
			}

			@Override
			public void onOperateFail(boolean isLike) {
				// 撤销上次
				newsOPerate.operateRevoked();
				if (isLike) {
					((TextView) oprtView.findViewById(R.id.tv_campus_news_like))
							.setText("点赞 ");
					((ImageView) oprtView
							.findViewById(R.id.iv_campus_news_like_background))
							.setImageResource(R.drawable.like_no);
					if (curntAdapter.getCount() <= 0) {
						// 隐藏点赞图标
						likeIcon.setVisibility(View.GONE);
					}
					if (curntAdapter.getCount() <= maxLikeCount) {
						// 隐藏所有点赞的人的按钮
						allLikeIcon.setVisibility(View.GONE);
					}
					operateData.setIsLike("0");
				} else {
					((TextView) oprtView.findViewById(R.id.tv_campus_news_like))
							.setText("已赞 ");
					((ImageView) oprtView
							.findViewById(R.id.iv_campus_news_like_background))
							.setImageResource(R.drawable.like_have);
					if (View.GONE == likeIcon.getVisibility()) {
						// 显示点赞图标
						likeIcon.setVisibility(View.VISIBLE);
					}
					if (curntAdapter.getCount() > maxLikeCount) {
						// 显示所有点赞的人的按钮
						allLikeIcon.setVisibility(View.VISIBLE);
						operateData.setLikeCount(String.valueOf(operateData
								.getLikeCount() + 1));
						allLikeIcon.setText(operateData.getLikeCount());
					}
					operateData.setIsLike("1");
				}
			}
		});
		if (operateData.getIsLike()) {
			newsOPerate.uploadLikeOperate(operateData.getNewsID(), false);
		} else {
			newsOPerate.uploadLikeOperate(operateData.getNewsID(), true);
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
	 * 图片gridview监听
	 */
	public class ImageGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			List<ImageModel> imageModelList = new ArrayList<ImageModel>();
			for (int index = 0; index < parent.getAdapter().getCount(); index++) {
				imageModelList.add((ImageModel) parent.getAdapter().getItem(
						index));
			}
			// 跳转到图片详情页面
			jumpToBigImage(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
					imageModelList, position);
		}
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
	 * 点赞gridview监听
	 */
	public class LikeGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			LikeModel likeUser = (LikeModel) parent.getAdapter().getItem(
					position);
			JumpToHomepage(JLXCUtils.stringToInt(likeUser.getUserID()));
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
	 * 跳转至用户的主页
	 */
	private void JumpToHomepage(int userID) {
		Intent intentUsrMain = new Intent(mContext, OtherPersonalActivity.class);
		intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY, userID);
		startActivityWithRight(intentUsrMain);
	}

	/***
	 * 跳转至动态相详情
	 */
	private void jumpToNewsDetail(ItemModel itemModel, int keyBoardMode,
			String commentId) {
		// 跳转到动态详情
		Intent intentToNewsDetail = new Intent(mContext,
				NewsDetailActivity.class);
		switch (keyBoardMode) {
		// 键盘关闭
		case NewsConstants.KEY_BOARD_CLOSE:
			intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_COMMENT_STATE,
					NewsConstants.KEY_BOARD_CLOSE);
			break;
		// 键盘打开等待评论
		case NewsConstants.KEY_BOARD_COMMENT:
			intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_COMMENT_STATE,
					NewsConstants.KEY_BOARD_COMMENT);
			break;
		// 键盘打开等待回复
		case NewsConstants.KEY_BOARD_REPLY:
			intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_COMMENT_STATE,
					NewsConstants.KEY_BOARD_REPLY);
			if (null != commentId) {
				intentToNewsDetail.putExtra(
						NewsConstants.INTENT_KEY_COMMENT_ID, commentId);
			} else {
				LogUtils.e("回复别人时必须要传递被评论的id.");
			}
			break;

		default:
			break;
		}
		// 当前操作的动态id
		intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_NEWS_ID,
				itemModel.getNewsID());

		// 找到当前的操作对象
		for (int index = 0; index < newsList.size(); ++index) {
			if (newsList.get(index).getNewsID().equals(itemModel.getNewsID())) {
				intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_NEWS_OBJ,
						newsList.get(index));
				break;
			}
		}

		// 带有返回参数的跳转至动态详情
		startActivityForResult(intentToNewsDetail, 0);
		getActivity().overridePendingTransition(R.anim.push_right_in,
				R.anim.push_right_out);
	}

	/**
	 * 广播接收处理
	 * */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent resultIntent) {
			String action = resultIntent.getAction();
			if (action.equals(JLXCConst.BROADCAST_NEWS_LIST_REFRESH)) {
				if (resultIntent.hasExtra(NewsConstants.OPERATE_UPDATE)) {
					// 更新动态列表
					NewsModel resultNews = (NewsModel) resultIntent
							.getSerializableExtra(NewsConstants.OPERATE_UPDATE);
					for (int index = 0; index < newsList.size(); index++) {
						if (resultNews.getNewsID().equals(
								newsList.get(index).getNewsID())) {
							newsList.set(index, resultNews);
							newsAdapter.replaceAll(DataToItem
									.campusDataToItems(newsList, personList));
							break;
						}
					}
				} else if (resultIntent.hasExtra(NewsConstants.OPERATE_DELETET)) {
					String resultID = resultIntent
							.getStringExtra(NewsConstants.OPERATE_DELETET);
					// 删除该动态
					for (int index = 0; index < newsList.size(); index++) {
						if (resultID.equals(newsList.get(index).getNewsID())) {
							newsList.remove(index);
							newsAdapter.replaceAll(DataToItem
									.campusDataToItems(newsList, personList));
							break;
						}
					}
				} else if (resultIntent
						.hasExtra(NewsConstants.OPERATE_NO_ACTION)) {
					// 无改变
				} else if (resultIntent.hasExtra(NewsConstants.PUBLISH_FINISH)) {
					if (!isRequestData) {
						// 发布了动态
						pageIndex = 1;
						isRequestData = true;
						isPullDowm = true;
						getCampusData(
								String.valueOf(UserManager.getInstance()
										.getUser().getUid()),
								String.valueOf(pageIndex), UserManager
										.getInstance().getUser()
										.getSchool_code(), "");
					}
				}
			}
		}
	};
}
