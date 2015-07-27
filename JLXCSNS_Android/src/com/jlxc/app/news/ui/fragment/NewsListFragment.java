package com.jlxc.app.news.ui.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
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
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.ui.view.NoScrollGridView.OnTouchInvalidPositionListener;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CommentListItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.OperateItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.NewsOperateModel;
import com.jlxc.app.news.ui.activity.AllLikePersonActivity;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
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
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

public class NewsListFragment extends BaseFragment {

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
	// 对动态的操作
	private NewsOperate newsOPerate;
	// 当前操作的位置
	private int indexAtNewsList = 0;
	// 当前点赞对应的gridview的adpter
	private HelloHaAdapter<LikeModel> curntAdapter = null;

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
		userModel = UserManager.getInstance().getUser();

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		newsOPerate = new NewsOperate(mContext);

		initBitmapUtils();
		/******** 首次获取数据 *********/
		currentPage = 1;
		isPullDowm = true;
		getNewsData(String.valueOf(userModel.getUid()),
				String.valueOf(currentPage), "");
		/*************************/
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

			HelloHaAdapter<ImageModel> newsGVAdapter = new HelloHaAdapter<ImageModel>(
					mContext, R.layout.mian_news_body_gridview_item_layout,
					pictureList) {
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
			// 点击空白区域时将事件传回给父控件
			bodyGridView
					.setOnTouchInvalidPositionListener(new OnTouchInvalidPositionListener() {

						@Override
						public boolean onTouchInvalidPosition(int motionEvent) {
							return false;
						}
					});
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
		helper.setOnClickListener(R.id.layout_news_operate_rootview, listener);
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
		// 点赞头像gridview
		NoScrollGridView likeGridView = (NoScrollGridView) helper
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
		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};

		// 显示加载更多评论的按钮
		if (commentList.size() < NEWS_COMMENT_NUM) {
			helper.setVisible(R.id.btn_more_comment, false);
		} else {
			helper.setVisible(R.id.btn_more_comment, true);
			helper.setOnClickListener(R.id.btn_more_comment, listener);
		}

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

		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.layout_news_title_rootview:
			case R.id.img_mian_news_user_head:
			case R.id.txt_main_news_user_name:
				TitleItem titleData = (TitleItem) newsAdapter.getItem(postion);
				if (R.id.layout_news_title_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(titleData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				} else {
					jumpToHomepage(JLXCUtils.stringToInt(titleData.getUserID()));
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
					jumpToBigImage(BigImgLookActivity.INTENT_KEY, path, 0);
				} else {
					// 跳转到动态详情
					jumpToNewsDetail(bodyData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				}
				break;

			case R.id.btn_mian_reply:
			case R.id.btn_mian_like:
			case R.id.layout_news_operate_rootview:

				final OperateItem operateData = (OperateItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_news_operate_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(operateData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				} else if (R.id.btn_mian_reply == viewID) {
					// 跳转至评论页面并打开评论框
					jumpToNewsDetail(operateData,
							NewsOperateModel.KEY_BOARD_COMMENT, null);
				} else {
					// 点赞操作
					likeOperate(postion, view, operateData);
				}
				break;

			case R.id.txt_comment_nameA:
			case R.id.txt_comment_nameC:
			case R.id.txt_comment_nameB:
			case R.id.txt_comment_contentA:
			case R.id.txt_comment_contentB:
			case R.id.txt_comment_contentC:
			case R.id.btn_more_comment:
				CommentListItem commentData = (CommentListItem) newsAdapter
						.getItem(postion);
				if (R.id.btn_more_comment == viewID) {
					// 查看全部评论
					/************ 测试：跳转至所有点赞的列表 ********/
					Intent intentToNewsDetail = new Intent(mContext,
							AllLikePersonActivity.class);
					intentToNewsDetail.putExtra(
							AllLikePersonActivity.INTENT_KEY_NEWS_ID,
							commentData.getNewsID());
					startActivityWithRight(intentToNewsDetail);
					/****************************************/
				} else {
					for (int iCount = 0; iCount < NEWS_COMMENT_NUM; ++iCount) {
						if (viewID == commentViewList.get(iCount).get("NAME")) {
							jumpToHomepage(JLXCUtils.stringToInt(commentData
									.getCommentList().get(iCount).getUserId()));
						} else if (viewID == commentViewList.get(iCount).get(
								"CONTENT")) {
							if (!commentData.getCommentList().get(iCount)
									.getUserId()
									.equals(String.valueOf(userModel.getUid()))) {
								// 跳转至评论页面并打开评论框,并变为回复某某的状态
								jumpToNewsDetail(commentData,
										NewsOperateModel.KEY_BOARD_REPLY,
										commentData.getCommentList()
												.get(iCount).getCommentID());
							} else {
								// 自己发布的评论跳转到动态详情
								jumpToNewsDetail(commentData,
										NewsOperateModel.KEY_BOARD_CLOSE, null);
							}
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
	 * 点赞操作
	 * */
	@SuppressWarnings("unchecked")
	private void likeOperate(int postion, View view,
			final OperateItem operateData) {

		final View oprtView = view;
		final int likeListPostion = postion + 1;
		try {
			ListView nListView = newsListView.getRefreshableView();
			View itemRootView = nListView.getChildAt(likeListPostion + 1
					- nListView.getFirstVisiblePosition());
			curntAdapter = null;
			if (null != itemRootView) {
				// 点赞头像列表可见
				LogUtils.i("itemRootView=" + itemRootView);
				NoScrollGridView likeGV = (NoScrollGridView) itemRootView
						.findViewById(R.id.gv_mian_Like_list);
				curntAdapter = (HelloHaAdapter<LikeModel>) likeGV.getAdapter();
			}
		} catch (Exception e) {
			LogUtils.e("动态点赞部分发生异常.");
		}

		newsOPerate.setLikeListener(new LikeCallBack() {

			@Override
			public void onOperateStart(boolean isLike) {
				if (isLike) {
					if (null != curntAdapter) {
						newsOPerate.addHeadToLikeList(curntAdapter);
					} else {
						newsOPerate.addDataToLikeList(newsAdapter,
								likeListPostion);
					}

					((Button) oprtView).setText("已赞");
					operateData.setIsLike("1");
					operateData.setLikeCount(String.valueOf((operateData
							.getLikeCount() + 1)));
				} else {
					if (null != curntAdapter) {
						newsOPerate.removeHeadFromLikeList(curntAdapter);
					} else {
						newsOPerate.removeDataFromLikeList(newsAdapter,
								likeListPostion);
					}

					((Button) oprtView).setText("点赞");
					operateData.setIsLike("0");
					operateData.setLikeCount(String.valueOf((operateData
							.getLikeCount() - 1)));
				}
			}

			@Override
			public void onOperateFail(boolean isLike) {
				if (isLike) {
					((Button) oprtView).setText("点赞");
					operateData.setIsLike("0");
				} else {
					((Button) oprtView).setText("已赞");
					operateData.setIsLike("1");
				}
				// 撤销上次
				newsOPerate.operateRevoked();
			}
		});
		if (operateData.getIsLike()) {
			newsOPerate.uploadLikeOperate(userModel, operateData.getNewsID(),
					false);
		} else {
			newsOPerate.uploadLikeOperate(userModel, operateData.getNewsID(),
					true);
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
			jumpToHomepage(JLXCUtils.stringToInt(likeUser.getUserID()));
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
	private void jumpToHomepage(int userID) {
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
		case NewsOperateModel.KEY_BOARD_CLOSE:
			intentToNewsDetail.putExtra(
					NewsOperateModel.INTENT_KEY_COMMENT_STATE,
					NewsOperateModel.KEY_BOARD_CLOSE);
			break;
		// 键盘打开等待评论
		case NewsOperateModel.KEY_BOARD_COMMENT:
			intentToNewsDetail.putExtra(
					NewsOperateModel.INTENT_KEY_COMMENT_STATE,
					NewsOperateModel.KEY_BOARD_COMMENT);
			break;
		// 键盘打开等待回复
		case NewsOperateModel.KEY_BOARD_REPLY:
			intentToNewsDetail.putExtra(
					NewsOperateModel.INTENT_KEY_COMMENT_STATE,
					NewsOperateModel.KEY_BOARD_REPLY);
			if (null != commentId) {
				intentToNewsDetail.putExtra(
						NewsOperateModel.INTENT_KEY_COMMENT_ID, commentId);
			} else {
				LogUtils.e("回复别人时必须要传递被评论的id.");
			}
			break;

		default:
			break;
		}
		// 当前操作的动态id
		intentToNewsDetail.putExtra(NewsOperateModel.INTENT_KEY_NEWS_ID,
				itemModel.getNewsID());

		// 找到当前的动态对象
		for (int index = 0; index < newsList.size(); ++index) {
			if (newsList.get(index).getNewsID().equals(itemModel.getNewsID())) {
				intentToNewsDetail.putExtra(
						NewsOperateModel.INTENT_KEY_NEWS_OBJ,
						newsList.get(index));
				indexAtNewsList = index;
				break;
			}
		}

		// 带有返回参数的跳转至动态详情
		startActivityForResult(intentToNewsDetail, 0);
		getActivity().overridePendingTransition(R.anim.push_right_in,
				R.anim.push_right_out);
	}

	/**
	 * 上一个Activity返回结束时调用
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent resultIntent) {
		if (null != resultIntent) {
			switch (resultCode) {
			case NewsOperateModel.OPERATE_UPDATE:
				if (resultIntent
						.hasExtra(NewsOperateModel.INTENT_KEY_BACK_NEWS_OBJ)) {
					NewsModel resultNews = (NewsModel) resultIntent
							.getSerializableExtra(NewsOperateModel.INTENT_KEY_BACK_NEWS_OBJ);
					newsList.set(indexAtNewsList, resultNews);
					newsAdapter
							.replaceAll(DataToItem.newsDataToItems(newsList));
				}
				break;
			case NewsOperateModel.OPERATE_DELETET:
				newsList.remove(indexAtNewsList);
				newsAdapter.replaceAll(DataToItem.newsDataToItems(newsList));
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, resultIntent);
	}
}
