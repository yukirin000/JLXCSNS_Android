package com.jlxc.app.personal.ui.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.ui.view.NoScrollGridView.OnTouchInvalidPositionListener;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.NewsOperateModel;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
import com.jlxc.app.news.utils.NewsOperate;
import com.jlxc.app.news.utils.NewsOperate.LikeCallBack;
import com.jlxc.app.news.utils.NewsOperate.OperateCallBack;
import com.jlxc.app.personal.model.MyNewsListItemModel;
import com.jlxc.app.personal.model.MyNewsListItemModel.MyNewsBodyItem;
import com.jlxc.app.personal.model.MyNewsListItemModel.MyNewsOperateItem;
import com.jlxc.app.personal.model.MyNewsListItemModel.MyNewsTitleItem;
import com.jlxc.app.personal.utils.NewsToItemData;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

//我的动态列表
public class MyNewsListActivity extends BaseActivityWithTopBar {

	// 其他Activity传递进入的被查看的用户id
	public final static String INTNET_KEY_UID = "user_id";
	// 用户实例
	private UserModel userModel;
	// 动态listview
	@ViewInject(R.id.listview_my_news_list)
	private PullToRefreshListView newsListView;
	// 原始数据源
	private List<NewsModel> newsList = new ArrayList<NewsModel>();
	// item类型数据
	private List<MyNewsListItemModel> itemDataList = null;
	// 动态列表适配器
	private HelloHaAdapter<MyNewsListItemModel> newsAdapter = null;
	// 使支持多种item
	private MultiItemTypeSupport<MyNewsListItemModel> multiItemTypeSupport = null;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 当前的数据页
	private int currentPage = 1;
	// 是否是最后一页数据
	private String lastPage = "0";
	// 是否下拉
	private boolean isPullDowm = false;
	// 是否正在请求数据
	private boolean isRequestData = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private ImageGridViewItemClick imageItemClickListener;
	// 当前操作的动态id
	private String currentNewsId = "";
	// 对动态的操作
	private NewsOperate newsOPerate;
	// 当前操作的位置
	private int indexAtNewsList = 0;
	// 被查看者的用户ID
	private String currentUid = "";

	@Override
	public int setLayoutId() {
		return R.layout.activity_my_news_list;
	}

	@Override
	protected void setUpView() {
		init();
		multiItemTypeSet();
		newsListViewSet();

		/******** 首次获取数据 *********/
		isPullDowm = true;
		getMyNewsData(currentUid, String.valueOf(currentPage));
		/*************************/
	}

	/**
	 * listView 支持多种item的设置
	 * */
	private void multiItemTypeSet() {
		multiItemTypeSupport = new MultiItemTypeSupport<MyNewsListItemModel>() {

			@Override
			public int getLayoutId(int position, MyNewsListItemModel itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case MyNewsListItemModel.NEWS_TITLE:
					layoutId = R.layout.my_newslist_item_title;
					break;
				case MyNewsListItemModel.NEWS_BODY:
					layoutId = R.layout.my_newslist_item_body;
					break;
				case MyNewsListItemModel.NEWS_OPERATE:
					layoutId = R.layout.my_newslist_item_operate;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return MyNewsListItemModel.NEWS_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, MyNewsListItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case MyNewsListItemModel.NEWS_TITLE:
					itemtype = MyNewsListItemModel.NEWS_TITLE;
					break;
				case MyNewsListItemModel.NEWS_BODY:
					itemtype = MyNewsListItemModel.NEWS_BODY;
					break;
				case MyNewsListItemModel.NEWS_OPERATE:
					itemtype = MyNewsListItemModel.NEWS_OPERATE;
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
				getMyNewsData(currentUid, String.valueOf(currentPage));
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
					ToastUtil.show(MyNewsListActivity.this, "没有数据了,哦哦");
				} else {
					isPullDowm = false;
					getMyNewsData(currentUid, String.valueOf(currentPage));
				}
			}
		});

		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<MyNewsListItemModel>(
				MyNewsListActivity.this, itemDataList, multiItemTypeSupport) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					MyNewsListItemModel item) {

				switch (helper.layoutId) {
				case R.layout.my_newslist_item_title:
					setTitleItemView(helper, item);
					break;
				case R.layout.my_newslist_item_body:
					setBodyItemView(helper, item);
					break;
				case R.layout.my_newslist_item_operate:
					setOperateItemView(helper, item);
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
							ToastUtil.show(MyNewsListActivity.this, "没有数据了,哦哦");
						} else {
							newsListView.setMode(Mode.PULL_FROM_END);
							newsListView.setRefreshing(true);
							isPullDowm = false;
							getMyNewsData(currentUid,
									String.valueOf(currentPage));
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

	/**
	 * 数据的初始化
	 * */
	private void init() {
		userModel = UserManager.getInstance().getUser();

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		newsOperateSet();
		initBitmapUtils();

		Intent intent = this.getIntent();
		if (null != intent && intent.hasExtra(INTNET_KEY_UID)) {
			currentUid = intent.getStringExtra(INTNET_KEY_UID);
		} else {
			LogUtils.e("用户id传输错误，用户id为：" + currentUid);
		}

		/*** 测试 *****/
		// currentUid = "21";
		// userModel.setUid(21);

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
		bitmapUtils = new BitmapUtils(MyNewsListActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * 动态操作类的初始化
	 * */
	private void newsOperateSet() {
		newsOPerate = new NewsOperate(MyNewsListActivity.this);
		newsOPerate.setOperateListener(new OperateCallBack() {

			@Override
			public void onStart(int operateType) {
				// 操作开始
			}

			@Override
			public void onFinish(int operateType, boolean isSucceed,
					Object resultValue) {
				switch (operateType) {
				case NewsOperate.OP_Type_Delete_News:
					if (isSucceed) {
						for (int index = 0; index < newsAdapter.getCount(); index++) {
							if (newsAdapter.getItem(index).getNewsID()
									.equals(currentNewsId)) {
								newsAdapter.remove(index);
								index--;
							}
						}
						ToastUtil.show(MyNewsListActivity.this, "删除成功");
					}
					break;

				default:
					break;
				}
			}
		});
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			MyNewsListItemModel item) {
		MyNewsTitleItem titleData = (MyNewsTitleItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.img_my_news_list_head);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.img_my_news_list_head, bitmapUtils,
				titleData.getUserSubHeadImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间
		helper.setText(R.id.txt_my_news_list_name, titleData.getUserName());
		helper.setText(R.id.txt_my_news_list_tiem,
				TimeHandle.getShowTimeFormat(titleData.getSendTime()));

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		if (currentUid.equals(String.valueOf(userModel.getUid()))) {
			helper.setVisible(R.id.btn_my_news_delete, true);
			helper.setOnClickListener(R.id.btn_my_news_delete, listener);
		} else {
			helper.setVisible(R.id.btn_my_news_delete, false);
		}
		helper.setOnClickListener(R.id.img_my_news_list_head, listener);
		helper.setOnClickListener(R.id.layout_my_news_list_title_rootview,
				listener);
	}

	/**
	 * 设置新闻主体item
	 * */
	private void setBodyItemView(HelloHaBaseAdapterHelper helper,
			MyNewsListItemModel item) {
		MyNewsBodyItem bodyData = (MyNewsBodyItem) item;
		List<ImageModel> pictureList = bodyData.getNewsImageListList();

		// 绑定图片显示
		if (pictureList.size() == 0) {
			// 没有图片的情况
			helper.setVisible(R.id.gv_my_news_list_body_image, false);
			helper.setVisible(R.id.iv_my_news_list_body_picture, false);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			helper.setVisible(R.id.gv_my_news_list_body_image, false);
			helper.setVisible(R.id.iv_my_news_list_body_picture, true);
			ImageView imgView = helper
					.getView(R.id.iv_my_news_list_body_picture);
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
			helper.setImageUrl(R.id.iv_my_news_list_body_picture, bitmapUtils,
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
			helper.setVisible(R.id.gv_my_news_list_body_image, true);
			helper.setVisible(R.id.iv_my_news_list_body_picture, false);
			NoScrollGridView bodyGridView = (NoScrollGridView) helper
					.getView(R.id.gv_my_news_list_body_image);

			HelloHaAdapter<ImageModel> newsGVAdapter = new HelloHaAdapter<ImageModel>(
					MyNewsListActivity.this,
					R.layout.my_news_list_gridview_item_layout, pictureList) {
				@Override
				protected void convert(HelloHaBaseAdapterHelper helper,
						ImageModel item) {
					// 设置显示图片的imageView大小
					int desSize = (screenWidth - 20) / 3;
					ImageView imgView = helper
							.getView(R.id.iv_my_news_body_gridview_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = desSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_my_news_body_gridview_item,
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
			helper.setVisible(R.id.txt_my_news_list_content, false);
		} else {
			helper.setVisible(R.id.txt_my_news_list_content, true);
			helper.setText(R.id.txt_my_news_list_content,
					bodyData.getNewsContent());
			helper.setOnClickListener(R.id.txt_my_news_list_content, listener);
		}
		// 设置地理位置
		if (bodyData.getLocation().equals("")) {
			helper.setVisible(R.id.txt_my_news_list_location, false);
		} else {
			helper.setVisible(R.id.txt_my_news_list_location, true);
			helper.setText(R.id.txt_my_news_list_location,
					bodyData.getLocation());
		}
		// 父布局监听
		helper.setOnClickListener(R.id.layout_my_news_list_body_rootview,
				listener);
	}

	/**
	 * 设置操作部分item
	 * */
	private void setOperateItemView(HelloHaBaseAdapterHelper helper,
			MyNewsListItemModel item) {

		MyNewsOperateItem opData = (MyNewsOperateItem) item;
		helper.setText(R.id.btn_my_news_list_reply,
				"评论 " + opData.getReplyCount());
		if (opData.getIsLike()) {
			helper.setText(R.id.btn_my_news_list_like,
					"已赞 " + opData.getLikeCount());
		} else {
			helper.setText(R.id.btn_my_news_list_like,
					"点赞 " + opData.getLikeCount());
		}
		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.btn_my_news_list_reply, listener);
		helper.setOnClickListener(R.id.btn_my_news_list_like, listener);
		helper.setOnClickListener(R.id.layout_my_news_list_operate_rootview,
				listener);
	}

	/**
	 * 获取动态数据
	 * */
	private void getMyNewsData(String userID, String page) {
		if (!isRequestData) {
			isRequestData = true;
			String path = JLXCConst.USER_NEWS_LIST + "?" + "user_id=" + userID
					+ "&page=" + page + "&size=" + "";

			LogUtils.i("path=" + path);
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
								ToastUtil
										.show(MyNewsListActivity.this,
												jsonResponse
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
							ToastUtil.show(MyNewsListActivity.this, "网络有毒=_=");
							newsListView.onRefreshComplete();
							newsListView.setMode(Mode.BOTH);
							isRequestData = false;
						}

					}, null));
		}
	}

	/**
	 * 删除当前评论
	 * */
	private void deleteCurrentNews(final String newsID) {
		final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
		alterDialog.setMessage("真的狠心删除吗？");
		alterDialog.setCancelable(true);

		alterDialog.setPositiveButton("是的",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						currentNewsId = newsID;
						newsOPerate.deleteNews(newsID);
					}
				});
		alterDialog.setNegativeButton("舍不得",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		alterDialog.show();
	}

	/**
	 * 点赞操作
	 * */
	private void likeOperate(int postion, View view,
			final MyNewsOperateItem operateData) {

		final View oprtView = view;

		newsOPerate.setLikeListener(new LikeCallBack() {

			@Override
			public void onOperateStart(boolean isLike) {
				if (isLike) {
					operateData.setLikeCount(String.valueOf(operateData
							.getLikeCount() + 1));
					((Button) oprtView).setText("已赞 "
							+ operateData.getLikeCount());
					operateData.setIsLike("1");
				} else {
					operateData.setLikeCount(String.valueOf(operateData
							.getLikeCount() - 1));
					((Button) oprtView).setText("点赞 "
							+ operateData.getLikeCount());
					operateData.setIsLike("0");
				}
			}

			@Override
			public void onOperateFail(boolean isLike) {
				if (isLike) {
					operateData.setLikeCount(String.valueOf(operateData
							.getLikeCount() - 1));
					((Button) oprtView).setText("点赞 "
							+ operateData.getLikeCount());
					operateData.setIsLike("0");
				} else {
					operateData.setLikeCount(String.valueOf(operateData
							.getLikeCount() + 1));
					((Button) oprtView).setText("已赞 "
							+ operateData.getLikeCount());
					operateData.setIsLike("1");
				}
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
			newsList.clear();
			newsList.addAll(newDatas);
			itemDataList = NewsToItemData.newsToItem(newDatas);
			newsAdapter.replaceAll(itemDataList);
		} else {
			newsList.addAll(newDatas);
			newsAdapter.addAll(NewsToItemData.newsToItem(newDatas));
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
			case R.id.layout_my_news_list_title_rootview:
			case R.id.img_my_news_list_head:
			case R.id.btn_my_news_delete:
				MyNewsTitleItem titleData = (MyNewsTitleItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_my_news_list_title_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(titleData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				} else if (R.id.btn_my_news_delete == viewID) {
					deleteCurrentNews(titleData.getNewsID());
				} else {
					jumpToHomepage(JLXCUtils.stringToInt(titleData.getUserID()));
				}
				break;

			case R.id.layout_my_news_list_body_rootview:
			case R.id.txt_my_news_list_content:
			case R.id.iv_my_news_list_body_picture:
				MyNewsBodyItem bodyData = (MyNewsBodyItem) newsAdapter
						.getItem(postion);
				if (R.id.iv_my_news_list_body_picture == viewID) {
					// 跳转到图片详情页面
					String path = bodyData.getNewsImageListList().get(0)
							.getURL();
					jumpToBigImage(BigImgLookActivity.INTENT_KEY, path, 0);
				} else {
					// 跳转至动态详情
					jumpToNewsDetail(bodyData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				}
				break;

			case R.id.btn_my_news_list_reply:
			case R.id.btn_my_news_list_like:
			case R.id.layout_my_news_list_operate_rootview:

				final MyNewsOperateItem operateData = (MyNewsOperateItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_my_news_list_operate_rootview == viewID) {
					// 跳转至动态详情
					jumpToNewsDetail(operateData,
							NewsOperateModel.KEY_BOARD_CLOSE, null);
				} else if (R.id.btn_my_news_list_reply == viewID) {
					// 跳转至评论页面并打开评论框
					jumpToNewsDetail(operateData,
							NewsOperateModel.KEY_BOARD_COMMENT, null);
				} else {
					// 进行点赞操作
					likeOperate(postion, view, operateData);
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
			Intent intentPicDetail = new Intent(MyNewsListActivity.this,
					BigImgLookActivity.class);
			intentPicDetail.putExtra(BigImgLookActivity.INTENT_KEY, pathUrl);
			startActivity(intentPicDetail);
		} else if (intentKey
				.equals(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST)) {
			// 传递model列表
			@SuppressWarnings("unchecked")
			List<ImageModel> mdPath = (List<ImageModel>) path;
			Intent intent = new Intent(MyNewsListActivity.this,
					BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			startActivity(intent);
		} else if (intentKey.equals(BigImgLookActivity.INTENT_KEY_IMG_LIST)) {
			// 传递String列表
			@SuppressWarnings("unchecked")
			List<String> mdPath = (List<String>) path;
			Intent intent = new Intent(MyNewsListActivity.this,
					BigImgLookActivity.class);
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
		Intent intentUsrMain = new Intent(MyNewsListActivity.this,
				OtherPersonalActivity.class);
		intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY, userID);
		startActivityWithRight(intentUsrMain);
	}

	/***
	 * 跳转至动态相详情
	 */
	private void jumpToNewsDetail(MyNewsListItemModel itemModel,
			int keyBoardMode, String commentId) {
		// 跳转到动态详情
		Intent intentToNewsDetail = new Intent(MyNewsListActivity.this,
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

		// 找到当前的操作对象
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
		this.overridePendingTransition(R.anim.push_right_in,
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
					newsAdapter.replaceAll(NewsToItemData.newsToItem(newsList));
				}
				break;
			case NewsOperateModel.OPERATE_DELETET:
				newsList.remove(indexAtNewsList);
				newsAdapter.replaceAll(NewsToItemData.newsToItem(newsList));
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, resultIntent);
	}

}
