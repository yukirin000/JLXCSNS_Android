package com.jlxc.app.news.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.DataToItem;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CommentListItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.OperateItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.R.anim;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class NewsListFragment extends BaseFragment {

	// 回传数据键值
	public final static String INTENT_KEY_LIKE_LIST = "like_list";
	public final static String INTENT_KEY_COMMENT_LIST = "comment_list";

	// 动态显示的评论数量
	private int NEWS_COMMENT_NUM = 3;
	// 最多点赞数
	private int MAX_LIKE_COUNT = 10;
	// 用户实例
	private UserModel userModel;
	// 动态listview
	@ViewInject(R.id.news_listview)
	private PullToRefreshListView newsListView;
	// 原始数据源
	private List<NewsModel> newsList = new ArrayList<NewsModel>();
	// item数据源
	private List<ItemModel> itemDataList = null;
	// 动态列表适配器
	private HelloHaAdapter<ItemModel> newsAdapter = null;
	// 动态的图片适配器
	private HelloHaAdapter<ImageModel> newsGVAdapter;
	// 使支持多种item
	private MultiItemTypeSupport<ItemModel> multiItemTypeSupport = null;
	// 上下文信息
	private Context mContext;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 评论部分的控件
	private List<Map<String, Integer>> commentViewList;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 当前的数据页
	private int currentPage = 1;
	// 是否是最后一页数据
	private String lastPage = "0";
	// 时间戳
	private String latestTimesTamp = "";
	// 是否下拉
	private boolean isPullDowm = false;
	// 是否正在请求数据
	private boolean isRequestData = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private ImageGridViewItemClick imageItemClickListener;
	// 点击点赞头像监听
	private LikeGridViewItemClick likeItemClickListener;
	// 点赞操作类
	private LikeCancel likeOperate;
	// 点赞头像gridview
	private NoScrollGridView likeGridView;
	// 当前操作的动态
	public static NewsModel currentNews;
	// 当前操作的位置
	public int indexAtNewsList = 0;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_mian_news_layout;
	}

	@Override
	public void loadLayout(View rootView) {
		init();
		multiItemTypeSet();
		newsListViewSet();
		// 将评论view存储在map
		commentViewList = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> commentMap = new HashMap<String, Integer>();
		commentMap.put("NAME", R.id.txt_comment_nameA);
		commentMap.put("CONTENT", R.id.txt_comment_contentA);
		commentViewList.add(commentMap);

		commentMap = new HashMap<String, Integer>();
		commentMap.put("NAME", R.id.txt_comment_nameB);
		commentMap.put("CONTENT", R.id.txt_comment_contentB);
		commentViewList.add(commentMap);

		commentMap = new HashMap<String, Integer>();
		commentMap.put("NAME", R.id.txt_comment_nameC);
		commentMap.put("CONTENT", R.id.txt_comment_contentC);
		commentViewList.add(commentMap);
	}

	/**
	 * listView 支持多种item的设置
	 * */
	private void multiItemTypeSet() {
		multiItemTypeSupport = new MultiItemTypeSupport<ItemModel>() {

			@Override
			public int getLayoutId(int position, ItemModel itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case ItemModel.NEWS_TITLE:
					layoutId = R.layout.mian_news_item_title_layout;
					break;
				case ItemModel.NEWS_BODY:
					layoutId = R.layout.main_news_item_body_layout;
					break;
				case ItemModel.NEWS_OPERATE:
					layoutId = R.layout.mian_news_item_operate_layout;
					break;
				case ItemModel.NEWS_LIKELIST:
					layoutId = R.layout.mian_news_item_likelist_layout;
					break;
				case ItemModel.NEWS_COMMENT:
					layoutId = R.layout.mian_news_item_comment_layout;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return ItemModel.NEWS_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, ItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case ItemModel.NEWS_TITLE:
					itemtype = ItemModel.NEWS_TITLE;
					break;
				case ItemModel.NEWS_BODY:
					itemtype = ItemModel.NEWS_BODY;
					break;
				case ItemModel.NEWS_OPERATE:
					itemtype = ItemModel.NEWS_OPERATE;
					break;
				case ItemModel.NEWS_LIKELIST:
					itemtype = ItemModel.NEWS_LIKELIST;
					break;
				case ItemModel.NEWS_COMMENT:
					itemtype = ItemModel.NEWS_COMMENT;
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
		newsListView.setMode(Mode.BOTH);
		/**
		 * 刷新监听
		 * */
		newsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				currentPage = 1;
				isPullDowm = true;
				getNewsData(String.valueOf(userModel.getUid()),
						String.valueOf(currentPage), "");
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (lastPage.equals("1")) {
					newsListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							newsListView.onRefreshComplete();
						}
					}, 1000);
					ToastUtil.show(mContext, "没有数据了,哦哦");
				} else {
					isPullDowm = false;
					getNewsData(String.valueOf(userModel.getUid()),
							String.valueOf(currentPage), latestTimesTamp);
				}
			}
		});

		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<ItemModel>(mContext, itemDataList,
				multiItemTypeSupport) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					ItemModel item) {

				switch (helper.layoutId) {
				case R.layout.mian_news_item_title_layout:
					setTitleItemView(helper, item);
					break;
				case R.layout.main_news_item_body_layout:
					setBodyItemView(helper, item);
					break;
				case R.layout.mian_news_item_operate_layout:
					setOperateItemView(helper, item);
					break;
				case R.layout.mian_news_item_likelist_layout:
					setLikeListItemView(helper, item);
					break;
				case R.layout.mian_news_item_comment_layout:
					setComentItemView(helper, item);
					break;

				default:
					break;
				}
			}
		};
		/**
		 * 设置底部自动刷新
		 * */
		newsListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (lastPage.equals("1")) {
							newsListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									newsListView.onRefreshComplete();
								}
							}, 1000);
							ToastUtil.show(mContext, "没有数据了,哦哦");
						} else {
							newsListView.setMode(Mode.PULL_FROM_END);
							newsListView.setRefreshing(true);
							isPullDowm = false;
							getNewsData(String.valueOf(userModel.getUid()),
									String.valueOf(currentPage),
									latestTimesTamp);
						}
					}
				});
		// 快速滑动时不加载图片
		newsListView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils,
				false, true));
		// 设置不可点击
		newsAdapter.setItemsClickEnable(false);
		newsListView.setAdapter(newsAdapter);
	}

	@Override
	public void setUpViews(View rootView) {

	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();
		// userModel = UserManager.getInstance().getUser();
		/********** 测试用户 ************/
		userModel = new UserModel();
		userModel.setUid(21);
		userModel.setName("朱旺");
		userModel
				.setHead_sub_image("http://192.168.1.100/jlxc_php/Uploads/2015-07-01/191435720077_sub.png");
		UserManager.getInstance().setUser(userModel);

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		initBitmapUtils();
		newsList = getLastData();
		itemDataList = DataToItem.newsDataToItems(newsList);

		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
		LogUtils.i("screenWidth=" + screenWidth + " screenHeight="
				+ screenHeight);
	}

	/***
	 * 测试数据，上次缓存的数据
	 * */
	private List<NewsModel> getLastData() {
		List<NewsModel> lastDataList = new ArrayList<NewsModel>();
		String path1 = "http://192.168.1.100/jlxc_php/Uploads/2015-06-30/401435667218_sub.png";
		String path2 = "http://192.168.1.100/jlxc_php/Uploads/2015-07-01/191435720077_sub.png";
		String path3 = "http://192.168.1.100/jlxc_php/Uploads/2015-07-01/191435720077.png";

		NewsModel newsData = new NewsModel();
		newsData.setUserName("朱旺");
		newsData.setUserHeadSubImage(path1);
		newsData.setSendTime("12:11");
		newsData.setUserSchool("罗湖中学");
		newsData.setNewsContent("哈哈哈哈哈哈哈哈哈");
		// 内容图片
		List<ImageModel> imgList = new ArrayList<ImageModel>();
		ImageModel tmpImg = new ImageModel();
		tmpImg.setSubURL(path2);
		tmpImg.setURL(path3);
		tmpImg.setImageWidth("563");
		tmpImg.setImageHheight("374");
		imgList.add(tmpImg);
		newsData.setImageNewsList(imgList);
		newsData.setLocation("北京");

		newsData.setCommentQuantity("15");
		newsData.setLikeQuantity("20");
		newsData.setTimesTamp("1436366057");
		latestTimesTamp = "1436366057";

		// 点赞
		List<LikeModel> lkList = new ArrayList<LikeModel>();
		LikeModel tmphead = new LikeModel();
		tmphead.setHeadSubImage(path2);
		lkList.add(tmphead);
		newsData.setLikeHeadListimage(lkList);

		// 评论列表
		List<CommentModel> cmtList = new ArrayList<CommentModel>();
		CommentModel tempCmt = new CommentModel();
		tempCmt.setCommentContent("哈哈哈哈哈0");
		tempCmt.setPublishName("朱旺:");
		cmtList.add(tempCmt);
		//
		tempCmt = new CommentModel();
		tempCmt.setCommentContent("哈哈哈哈哈1");
		tempCmt.setPublishName("朱旺:");
		cmtList.add(tempCmt);
		//
		tempCmt = new CommentModel();
		tempCmt.setCommentContent("哈哈哈哈哈2");
		tempCmt.setPublishName("朱旺:");
		cmtList.add(tempCmt);
		newsData.setCommentList(cmtList);

		lastDataList.add(newsData);
		return lastDataList;
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		/*
		 * bitmapUtils = BitmapManager.getInstance().getBitmapUtils(mContext,
		 * false, false);
		 */
		bitmapUtils = new BitmapUtils(mContext);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.img_mian_news_user_head);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.img_mian_news_user_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间，标签
		helper.setText(R.id.txt_main_news_user_name, titleData.getUserName());
		helper.setText(R.id.txt_main_news_publish_time,
				TimeHandle.getShowTimeFormat(titleData.getSendTime()));
		helper.setText(R.id.txt_main_news_user_tag, titleData.getUserTag());

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.img_mian_news_user_head, listener);
		helper.setOnClickListener(R.id.txt_main_news_user_name, listener);
		helper.setOnClickListener(R.id.layout_news_title_rootview, listener);
	}

	/**
	 * 设置新闻主体item
	 * */
	private void setBodyItemView(HelloHaBaseAdapterHelper helper, ItemModel item) {
		BodyItem bodyData = (BodyItem) item;
		List<ImageModel> pictureList = bodyData.getNewsImageListList();

		// 绑定图片显示
		if (pictureList.size() == 0) {
			// 没有图片的情况
			helper.setVisible(R.id.gv_mian_news_body_image, false);
			helper.setVisible(R.id.iv_mian_news_body_picture, false);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			helper.setVisible(R.id.gv_mian_news_body_image, false);
			helper.setVisible(R.id.iv_mian_news_body_picture, true);
			ImageView imgView = helper.getView(R.id.iv_mian_news_body_picture);
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
			helper.setImageUrl(R.id.iv_mian_news_body_picture, bitmapUtils,
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
			helper.setVisible(R.id.gv_mian_news_body_image, true);
			helper.setVisible(R.id.iv_mian_news_body_picture, false);
			NoScrollGridView bodyGridView = (NoScrollGridView) helper
					.getView(R.id.gv_mian_news_body_image);
			newsGVAdapter = new HelloHaAdapter<ImageModel>(mContext,
					R.layout.mian_news_body_gridview_item_layout, pictureList) {
				@Override
				protected void convert(HelloHaBaseAdapterHelper helper,
						ImageModel item) {
					// 设置显示图片的imageView大小
					int desSize = (screenWidth - 20) / 3;
					ImageView imgView = helper
							.getView(R.id.iv_mian_body_gridview_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = desSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_mian_body_gridview_item,
							bitmapUtils, item.getSubURL(),
							new NewsBitmapLoadCallBack());
				}
			};
			bodyGridView.setAdapter(newsGVAdapter);

			/**
			 * 点击图片事件
			 * */
			bodyGridView.setOnItemClickListener(imageItemClickListener);
		}

		// 创建点击事件监听对象
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};

		// 设置 文字内容
		if (bodyData.getNewsContent().equals("")) {
			helper.setVisible(R.id.txt_main_news_content, false);
		} else {
			helper.setVisible(R.id.txt_main_news_content, true);
			helper.setText(R.id.txt_main_news_content,
					bodyData.getNewsContent());
			helper.setOnClickListener(R.id.txt_main_news_content, listener);
		}
		// 设置地理位置
		if (bodyData.getLocation().equals("")) {
			helper.setVisible(R.id.txt_main_news_location, false);
		} else {
			helper.setVisible(R.id.txt_main_news_location, true);
			helper.setText(R.id.txt_main_news_location, bodyData.getLocation());
		}
		// 父布局监听
		helper.setOnClickListener(R.id.layout_news_body_rootview, listener);
	}

	/**
	 * 设置操作部分item
	 * */
	private void setOperateItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		OperateItem opData = (OperateItem) item;
		helper.setText(R.id.btn_mian_reply, "评论 " + opData.getReplyCount());
		if (opData.getIsLike()) {
			helper.setText(R.id.btn_mian_like, "已赞 " + opData.getLikeCount());
		} else {
			helper.setText(R.id.btn_mian_like, "点赞 " + opData.getLikeCount());
		}
		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.btn_mian_reply, listener);
		helper.setOnClickListener(R.id.btn_mian_like, listener);
		helper.setOnClickListener(R.id.layout_news_detail_rootview, listener);
	}

	/**
	 * 设置点赞部分item
	 * */
	private void setLikeListItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		LikeListItem lkData = (LikeListItem) item;
		List<LikeModel> lkImageList = lkData.getLikeHeadListimage();

		// 点赞头像的显示
		HelloHaAdapter<LikeModel> likeGVAdapter = new HelloHaAdapter<LikeModel>(
				mContext, R.layout.mian_news_like_gridview_item_layout,
				lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				// 设置头像imageview的尺寸
				ImageView imgView = helper
						.getView(R.id.iv_mian_like_gridview_item);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 12;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定图片
				if (helper.getPosition() < MAX_LIKE_COUNT) {
					bitmapUtils.configDefaultBitmapMaxSize(30, 30);
					helper.setImageUrl(R.id.iv_mian_like_gridview_item,
							bitmapUtils, item.getHeadSubImage(),
							new NewsBitmapLoadCallBack());
				} else if (10 == helper.getPosition()) {
					helper.setImageResource(R.id.iv_mian_like_gridview_item,
							R.drawable.ic_launcher);
				}
			}
		};
		likeGridView = (NoScrollGridView) helper
				.getView(R.id.gv_mian_Like_list);
		likeGridView.setAdapter(likeGVAdapter);
		likeGridView.setOnItemClickListener(likeItemClickListener);
	}

	/**
	 * 设置回复评论item
	 * */
	private void setComentItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		CommentListItem itemData = (CommentListItem) item;
		// 所有的评论数据
		List<CommentModel> commentList = itemData.getCommentList();

		// 显示三条评论
		for (int iCount = 0; iCount < NEWS_COMMENT_NUM; ++iCount) {
			if (iCount < commentList.size()) {
				// 设为显示
				helper.setVisible(commentViewList.get(iCount).get("NAME")
						.intValue(), true);
				helper.setVisible(commentViewList.get(iCount).get("CONTENT")
						.intValue(), true);
				// 绑定数据
				helper.setText(commentViewList.get(iCount).get("NAME")
						.intValue(), commentList.get(iCount).getPublishName()
						+ ":");
				helper.setText(commentViewList.get(iCount).get("CONTENT")
						.intValue(), commentList.get(iCount)
						.getCommentContent());
			} else {
				// 设为隐藏
				helper.setVisible(commentViewList.get(iCount).get("NAME")
						.intValue(), false);
				helper.setVisible(commentViewList.get(iCount).get("CONTENT")
						.intValue(), false);
			}

		}
		// 显示加载更多评论的按钮
		if (commentList.size() < NEWS_COMMENT_NUM) {
			helper.setVisible(R.id.btn_more_comment, false);
		} else {
			helper.setVisible(R.id.btn_more_comment, true);
		}

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		for (int iCount = 0; iCount < NEWS_COMMENT_NUM; ++iCount) {
			helper.setOnClickListener(commentViewList.get(iCount).get("NAME")
					.intValue(), listener);
			helper.setOnClickListener(commentViewList.get(iCount)
					.get("CONTENT").intValue(), listener);
		}
	}

	/**
	 * 获取动态数据
	 * */
	private void getNewsData(String userID, String desPage, String lastTime) {
		if (!isRequestData) {
			isRequestData = true;
			String path = JLXCConst.NEWS_LIST + "?" + "user_id=" + userID
					+ "&page=" + desPage + "&frist_time=" + lastTime;

			HttpManager.get(path, new JsonRequestCallBack<String>(
					new LoadDataHandler<String>() {

						@SuppressWarnings("unchecked")
						@Override
						public void onSuccess(JSONObject jsonResponse,
								String flag) {
							super.onSuccess(jsonResponse, flag);
							int status = jsonResponse
									.getInteger(JLXCConst.HTTP_STATUS);
							if (status == JLXCConst.STATUS_SUCCESS) {
								JSONObject jResult = jsonResponse
										.getJSONObject(JLXCConst.HTTP_RESULT);
								// 获取动态列表
								List<JSONObject> JSONList = (List<JSONObject>) jResult
										.get("list");
								lastPage = jResult.getString("is_last");
								if (lastPage.equals("0")) {
									currentPage++;
								}
								JsonToNewsModel(JSONList);
								newsListView.onRefreshComplete();
								newsListView.setMode(Mode.BOTH);
								isRequestData = false;
							}

							if (status == JLXCConst.STATUS_FAIL) {
								ToastUtil.show(mContext, jsonResponse
										.getString(JLXCConst.HTTP_MESSAGE));
								newsListView.onRefreshComplete();
								newsListView.setMode(Mode.BOTH);
								isRequestData = false;
							}
						}

						@Override
						public void onFailure(HttpException arg0, String arg1,
								String flag) {
							super.onFailure(arg0, arg1, flag);
							ToastUtil.show(mContext, "网络有毒=_=");
							newsListView.onRefreshComplete();
							newsListView.setMode(Mode.BOTH);
							isRequestData = false;
						}

					}, null));
		}
	}

	/***
	 * 点赞操作
	 */
	private void likeNetOperate(String newsId, String likeOrCancel) {
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("news_id", newsId);
		params.addBodyParameter("isLike", likeOrCancel);
		params.addBodyParameter("user_id", String.valueOf(userModel.getUid()));
		params.addBodyParameter("is_second", "0");

		HttpManager.post(JLXCConst.LIKE_OR_CANCEL, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {

						}

						if (status == JLXCConst.STATUS_FAIL) {
							// 失败则取消操作
							likeOperate.Revoked();
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						// 失败则取消操作
						likeOperate.Revoked();
						ToastUtil.show(mContext, "卧槽，竟然操作失败，检查下网络");
					}

				}, null));

	}

	/**
	 * 数据处理
	 */
	private void JsonToNewsModel(List<JSONObject> dataList) {
		List<NewsModel> newDatas = new ArrayList<NewsModel>();
		for (JSONObject newsObj : dataList) {
			NewsModel tempNews = new NewsModel();
			tempNews.setContentWithJson(newsObj);
			newDatas.add(tempNews);
		}
		if (isPullDowm) {
			// 更新时间戳
			latestTimesTamp = newDatas.get(0).getTimesTamp();
			newsList.clear();
			newsList.addAll(newDatas);
			newsAdapter.replaceAll(DataToItem.newsDataToItems(newDatas));
		} else {
			newsList.addAll(newDatas);
			newsAdapter.addAll(DataToItem.newsDataToItems(newDatas));
		}
		dataList.clear();
	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.layout_news_title_rootview:
			case R.id.img_mian_news_user_head:
			case R.id.txt_main_news_user_name:
				TitleItem titleData = (TitleItem) newsAdapter.getItem(postion);
				if (R.id.layout_news_title_rootview == viewID) {
					// 跳转到动态详情
					Intent intentToNewsDetail = new Intent(mContext,
							NewsDetailActivity.class);
					intentToNewsDetail.putExtra(
							NewsDetailActivity.INTENT_KEY_CMT_STATE,
							"CLOSE_KEY_BOARD");
					startActivityWithRightForResult(intentToNewsDetail,
							titleData.getNewsID());

				} else {
					// 跳转到用户的主页
					Intent intentUsrMain = new Intent(mContext,
							OtherPersonalActivity.class);
					intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY,
							titleData.getUserID());
					startActivityWithRight(intentUsrMain);
				}
				break;

			case R.id.layout_news_body_rootview:
			case R.id.txt_main_news_content:
			case R.id.iv_mian_news_body_picture:
				BodyItem bodyData = (BodyItem) newsAdapter.getItem(postion);
				if (R.id.iv_mian_news_body_picture == viewID) {
					// 跳转到图片详情页面
					String path = bodyData.getNewsImageListList().get(0)
							.getURL();
					Intent intentPicDetail = new Intent(mContext,
							BigImgLookActivity.class);
					intentPicDetail.putExtra("filePath", path);
					startActivity(intentPicDetail);
				} else {
					// 跳转到动态详情
					Intent intentToNewsDetail = new Intent(mContext,
							NewsDetailActivity.class);
					intentToNewsDetail.putExtra(
							NewsDetailActivity.INTENT_KEY_CMT_STATE,
							"CLOSE_KEY_BOARD");
					startActivityWithRightForResult(intentToNewsDetail,
							bodyData.getNewsID());
				}
				break;

			case R.id.btn_mian_reply:
			case R.id.btn_mian_like:
			case R.id.layout_news_detail_rootview:
				OperateItem operateData = (OperateItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_news_detail_rootview == viewID) {
					// 跳转至动态详情
					Intent intentToNewsDetail = new Intent(mContext,
							NewsDetailActivity.class);
					intentToNewsDetail.putExtra(
							NewsDetailActivity.INTENT_KEY_CMT_STATE,
							"CLOSE_KEY_BOARD");
					startActivityWithRightForResult(intentToNewsDetail,
							operateData.getNewsID());
				} else if (R.id.btn_mian_reply == viewID) {
					// 跳转至评论页面并打开评论框
					Intent intentToNewsDetail = new Intent(mContext,
							NewsDetailActivity.class);
					intentToNewsDetail.putExtra(
							NewsDetailActivity.INTENT_KEY_CMT_STATE,
							"publish_comment");
					startActivityWithRightForResult(intentToNewsDetail,
							operateData.getNewsID());
				} else {
					// 进行点赞操作
					likeOperate = new LikeCancel(view, postion);
					if (operateData.getIsLike()) {
						likeOperate.Cancel();
						likeNetOperate(operateData.getNewsID(), "0");
					} else {
						likeOperate.Like();
						likeNetOperate(operateData.getNewsID(), "1");
					}
				}
				break;

			case R.id.txt_comment_nameA:
			case R.id.txt_comment_nameC:
			case R.id.txt_comment_nameB:
			case R.id.txt_comment_contentA:
			case R.id.txt_comment_contentB:
			case R.id.txt_comment_contentC:
				CommentListItem commentData = (CommentListItem) newsAdapter
						.getItem(postion);
				for (int iCount = 0; iCount < NEWS_COMMENT_NUM; ++iCount) {
					if (viewID == commentViewList.get(iCount).get("NAME")) {
						// 跳转到用户的主页
						Intent intentUsrMain = new Intent(mContext,
								OtherPersonalActivity.class);
						intentUsrMain.putExtra(
								OtherPersonalActivity.INTENT_KEY, commentData
										.getCommentList().get(iCount)
										.getUserId());
						startActivityWithRight(intentUsrMain);
					} else if (viewID == commentViewList.get(iCount).get(
							"CONTENT")) {
						if (!commentData.getCommentList().get(iCount)
								.getUserId()
								.equals(String.valueOf(userModel.getUid()))) {
							// 跳转至评论页面并打开评论框
							Intent intentToNewsDetail = new Intent(mContext,
									NewsDetailActivity.class);
							intentToNewsDetail.putExtra(
									NewsDetailActivity.INTENT_KEY_CMT_STATE,
									"publish_Reply");
							intentToNewsDetail.putExtra(
									NewsDetailActivity.INTENT_KEY_CMT_ID,
									commentData.getCommentList().get(iCount)
											.getCommentID());
							startActivityWithRightForResult(intentToNewsDetail,
									commentData.getNewsID());
						} else {
							// 自己发布的评论跳转到动态详情
							Intent intentToNewsDetail = new Intent(mContext,
									NewsDetailActivity.class);
							intentToNewsDetail.putExtra(
									NewsDetailActivity.INTENT_KEY_CMT_STATE,
									"CLOSE_KEY_BOARD");
							startActivityWithRightForResult(intentToNewsDetail,
									commentData.getNewsID());
						}
					}
				}
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 点赞或取消
	 * 
	 * @author Alan
	 */
	private class LikeCancel {
		private View view;
		private int postion;
		private boolean isLikeOperate = false;

		public LikeCancel(View view, int postion) {
			this.view = view;
			this.postion = postion;
		}

		/**
		 * 点赞操作函数
		 * */
		public void Like() {
			int likeHeadPostion = postion + 1;
			isLikeOperate = true;
			OperateItem operateData = (OperateItem) newsAdapter
					.getItem(postion);
			String likeCount = String.valueOf((operateData.getLikeCount() + 1));
			operateData.setLikeCount(likeCount);
			operateData.setIsLike("1");

			LikeModel myModel = new LikeModel();
			myModel.setUserID(String.valueOf(userModel.getUid()));
			myModel.setHeadImage(userModel.getHead_image());
			myModel.setHeadSubImage(userModel.getHead_sub_image());

			LikeListItem likeData = (LikeListItem) newsAdapter
					.getItem(likeHeadPostion);
			likeData.getLikeHeadListimage().add(0, myModel);
			newsAdapter.notifyDataSetChanged();
		}

		/**
		 * 取消点赞
		 * */
		public void Cancel() {
			isLikeOperate = false;
			int likeHeadPostion = postion + 1;
			OperateItem operateData = (OperateItem) newsAdapter
					.getItem(postion);
			String likeCount = String.valueOf((operateData.getLikeCount() - 1));
			operateData.setLikeCount(likeCount);
			operateData.setIsLike("0");
			// 移除头像
			List<LikeModel> likeData = ((LikeListItem) newsAdapter
					.getItem(likeHeadPostion)).getLikeHeadListimage();
			for (int index = 0; index < likeData.size(); ++index) {
				if (likeData.get(index).getUserID()
						.equals(String.valueOf(userModel.getUid()))) {
					likeData.remove(index);
					break;
				} else {
					LogUtils.e("点赞数据发生了错误.");
				}
			}
			newsAdapter.notifyDataSetChanged();
		}

		/**
		 * 撤销上次操作
		 * */
		public void Revoked() {
			if (isLikeOperate) {
				this.Cancel();
			} else {
				this.Like();
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
	 * 图片gridview监听
	 */
	public class ImageGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String currentImgPath = ((ImageModel) parent.getAdapter().getItem(
					position)).getURL();
			// 跳转到图片详情页面
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra("filePath", currentImgPath);
			startActivity(intent);
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
			// 跳转到用户的主页
			Intent intentUsrMain = new Intent(mContext,
					OtherPersonalActivity.class);
			intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY,
					likeUser.getUserID());
			startActivityWithRight(intentUsrMain);
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
	 * 带返回结果的 右侧进入
	 * 
	 * @param intent
	 */
	public void startActivityWithRightForResult(Intent intent, String newsID) {
		for (int index = 0; index < newsList.size(); index++) {
			if (newsList.get(index).getNewsID().equals(newsID)) {
				currentNews = newsList.get(index);
				indexAtNewsList = index;
				break;
			}
		}
		startActivityForResult(intent, 0);
		getActivity().overridePendingTransition(R.anim.push_right_in,
				R.anim.push_right_out);
	}

	/**
	 * 上一个Activity结束时调用
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		//
		newsList.set(indexAtNewsList, currentNews);
		newsAdapter.replaceAll(DataToItem.newsDataToItems(newsList));
		super.onActivityResult(requestCode, resultCode, intent);
	}
}
