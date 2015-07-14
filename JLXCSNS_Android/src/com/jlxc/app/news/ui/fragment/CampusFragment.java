package com.jlxc.app.news.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
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
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.DataToItem;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CampusPersonModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CampusHeadItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.OperateItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.ui.activity.CampusAllPerson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

public class CampusFragment extends BaseFragment {

	// 最多点赞数
	private int MAX_LIKE_COUNT = 10;
	// 用户实例
	private UserModel userModel;
	// 动态listview
	@ViewInject(R.id.campus_listview)
	private PullToRefreshListView campusListView;
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
	// 点击头像监听
	private LikeGridViewItemClick likeItemClickListener;
	// 点赞操作类
	private LikeCancel likeOperate;
	// 点赞头像gridview
	private NoScrollGridView likeGridView;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_campus_layout;
	}

	@Override
	public void loadLayout(View rootView) {
		init();
		multiItemTypeSet();
		newsListViewSet();

		// 进入本页面时请求数据
		currentPage = 1;
		isPullDowm = true;
		getCampusData(String.valueOf(userModel.getUid()),
				String.valueOf(currentPage), userModel.getSchool_code(), "");
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
				return ItemModel.NEWS_ITEM_TYPE_COUNT;
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
				currentPage = 1;
				isPullDowm = true;
				getCampusData(String.valueOf(userModel.getUid()),
						String.valueOf(currentPage),
						userModel.getSchool_code(), "");
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (lastPage.equals("1")) {
					campusListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							campusListView.onRefreshComplete();
						}
					}, 1000);
					ToastUtil.show(mContext, "没有数据了,哦哦");
				} else {
					isPullDowm = false;
					getCampusData(String.valueOf(userModel.getUid()),
							String.valueOf(currentPage),
							userModel.getSchool_code(), latestTimesTamp);
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
						if (lastPage.equals("1")) {
							campusListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									campusListView.onRefreshComplete();
								}
							}, 1000);
							ToastUtil.show(mContext, "没有数据了,哦哦");
						} else {
							campusListView.setMode(Mode.PULL_FROM_END);
							campusListView.setRefreshing(true);
							isPullDowm = false;
							getCampusData(String.valueOf(userModel.getUid()),
									String.valueOf(currentPage),
									userModel.getSchool_code(), latestTimesTamp);
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

	@Override
	public void setUpViews(View rootView) {

	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();
		// userModel = UserManager.getInstance().getUser();
		userModel = new UserModel();
		userModel.setUid(21);
		userModel
				.setHead_sub_image("http://192.168.1.100/jlxc_php/Uploads/2015-07-01/191435720077_sub.png");
		userModel.setSchool_code("10000001");
		userModel.setSchool("深圳市高级中学");

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
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
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.img_campus_user_head);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
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
		}

		// 设置 文字内容
		if (bodyData.getNewsContent().equals("")) {
			helper.setVisible(R.id.txt_campus_news_content, false);
		} else {
			helper.setVisible(R.id.txt_campus_news_content, true);
			helper.setText(R.id.txt_campus_news_content,
					bodyData.getNewsContent());
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
		helper.setText(R.id.btn_campus_reply, "评论 " + opData.getReplyCount());
		if (opData.getIsLike()) {
			helper.setText(R.id.btn_campus_like, "已赞 " + opData.getLikeCount());
		} else {
			helper.setText(R.id.btn_campus_like, "点赞 " + opData.getLikeCount());
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
				mContext, R.layout.campus_news_likelist_gridview_item_layout,
				lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				// 设置头像imageview的尺寸
				ImageView imgView = helper
						.getView(R.id.iv_campus_like_gridview_item);
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
					helper.setImageUrl(R.id.iv_campus_like_gridview_item,
							bitmapUtils, item.getHeadSubImage(),
							new NewsBitmapLoadCallBack());
				} else if (10 == helper.getPosition()) {
					helper.setImageResource(R.id.iv_campus_like_gridview_item,
							R.drawable.ic_launcher);
				}
			}
		};
		likeGridView = (NoScrollGridView) helper
				.getView(R.id.gv_campus_like_list);
		likeGridView.setAdapter(likeGVAdapter);
		likeGridView.setOnItemClickListener(likeItemClickListener);
	}

	/**
	 * 设置头部item
	 * */
	private void setCampusHeadView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {

		CampusHeadItem headData = (CampusHeadItem) item;
		helper.setText(R.id.tv_campus_head_name, userModel.getSchool());

		List<CampusPersonModel> personList = headData.getPersonList();

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
				laParams.width = laParams.height = (screenWidth) / 7;
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
		GridView headPersonGridView = (GridView) helper
				.getView(R.id.gv_school_person);
		headPersonGridView.setAdapter(personGVAdapter);
		PersonGridViewItemClick personItemClickListener = new PersonGridViewItemClick();
		headPersonGridView.setOnItemClickListener(personItemClickListener);

		// 查看所有的校友
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.txt_campus_all_alumnus, listener);
	}

	/**
	 * 获取动态数据
	 * */
	private void getCampusData(String userID, String desPage,
			String schoolCode, String lastTime) {
		if (!isRequestData) {
			isRequestData = true;
			String path = JLXCConst.SCHOOL_NEWS_LIST + "?" + "user_id="
					+ userID + "&page=" + desPage + "&school_code="
					+ schoolCode + "&frist_time=" + lastTime;

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
								// 获取数据列表
								List<JSONObject> JNewsList = (List<JSONObject>) jResult
										.get("list");
								List<JSONObject> JPersonList = null;
								if (jResult.containsKey("info")) {
									JPersonList = (List<JSONObject>) jResult
											.get("info");
								}
								JsonToItemData(JNewsList, JPersonList);
								lastPage = jResult.getString("is_last");
								if (lastPage.equals("0")) {
									currentPage++;
								}
								campusListView.onRefreshComplete();
								campusListView.setMode(Mode.BOTH);
								isRequestData = false;
							}

							if (status == JLXCConst.STATUS_FAIL) {
								ToastUtil.show(mContext, jsonResponse
										.getString(JLXCConst.HTTP_MESSAGE));
								campusListView.onRefreshComplete();
								campusListView.setMode(Mode.BOTH);
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
	private void JsonToItemData(List<JSONObject> JNewsList,
			List<JSONObject> JPersonList) {
		// 校园的动态
		List<NewsModel> newsList = new ArrayList<NewsModel>();
		for (JSONObject newsObj : JNewsList) {
			NewsModel tempNews = new NewsModel();
			tempNews.setContentWithJson(newsObj);
			newsList.add(tempNews);
		}
		List<CampusPersonModel> personList = new ArrayList<CampusPersonModel>();
		if (isPullDowm) {
			// 解析校园的人
			for (JSONObject personObj : JPersonList) {
				CampusPersonModel tempPerson = new CampusPersonModel();
				tempPerson.setContentWithJson(personObj);
				personList.add(tempPerson);
			}
			latestTimesTamp = newsList.get(0).getTimesTamp();
			newsAdapter.replaceAll(DataToItem.campusDataToItems(newsList,
					personList));
		} else {
			newsAdapter.addAll(DataToItem.campusDataToItems(newsList,
					personList));
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
				TitleItem titleData = (TitleItem) newsAdapter.getItem(postion);
				if (R.id.img_campus_user_head == viewID) {
					ToastUtil
							.show(mContext, "点击了头像:" + titleData.getUserName());
				} else {
					ToastUtil.show(mContext, "" + titleData.getUserName());
				}
				break;

			case R.id.iv_campus_news_body_picture:
				BodyItem bodyData = (BodyItem) newsAdapter.getItem(postion);
				String path = bodyData.getNewsImageListList().get(0).getURL();
				// 跳转到图片详情页面
				Intent intent = new Intent(mContext, BigImgLookActivity.class);
				intent.putExtra("filePath", path);
				startActivity(intent);
				break;

			case R.id.btn_campus_reply:
			case R.id.btn_campus_like:
				OperateItem operateData = (OperateItem) newsAdapter
						.getItem(postion);
				if (R.id.btn_campus_reply == viewID) {
					ToastUtil.show(mContext,
							"评论次数:" + operateData.getReplyCount());
				} else {
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

			case R.id.txt_campus_all_alumnus:
				// 跳转到所有好友列表页面
				Intent personIntent = new Intent(mContext,
						CampusAllPerson.class);
				personIntent
						.putExtra("School_Code", userModel.getSchool_code());
				startActivityWithRight(personIntent);
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
	 * 学校的人gridview监听
	 */
	public class PersonGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CampusPersonModel personModel = (CampusPersonModel) parent
					.getAdapter().getItem(position);

			// 跳转至个人主页
			ToastUtil.show(mContext, "UserID:" + personModel.getUserId());
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
			ToastUtil.show(mContext, "UserID:" + likeUser.getUserID());
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
}
