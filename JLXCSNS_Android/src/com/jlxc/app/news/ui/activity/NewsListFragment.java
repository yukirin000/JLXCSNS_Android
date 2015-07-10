package com.jlxc.app.news.ui.activity;

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
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.CommentModel;
import com.jlxc.app.base.model.ImageModel;
import com.jlxc.app.base.model.LikeModel;
import com.jlxc.app.base.model.NewsItemModel;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.model.NewsItemModel.BodyItem;
import com.jlxc.app.base.model.NewsItemModel.LikeListItem;
import com.jlxc.app.base.model.NewsItemModel.OperateItem;
import com.jlxc.app.base.model.NewsItemModel.CommentItem;
import com.jlxc.app.base.model.NewsItemModel.TitleItem;
import com.jlxc.app.base.model.NewsModel;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.NewsToItem;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.login.ui.activity.RegisterInformationActivity;
import com.jlxc.app.login.ui.activity.SelectSchoolActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.content.Context;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class NewsListFragment extends BaseFragment {

	// 加载更多时每次加载的动态数量
	private int NEWS_DATA_NUM = 10;
	// 动态显示的评论数量
	private int NEWS_COMMENT_NUM = 3;
	// 用户实例
	private UserModel userModel;
	// 动态listview
	@ViewInject(R.id.news_listview)
	private PullToRefreshListView newsListView;
	// 原始数据源
	private List<NewsModel> listItem = new ArrayList<NewsModel>();
	// item数据源
	private List<NewsItemModel> itemDataList = null;
	// 动态列表适配器
	private HelloHaAdapter<NewsItemModel> newsAdapter = null;
	// 动态的图片适配器
	private HelloHaAdapter<ImageModel> newsGVAdapter;
	// 使支持多种item
	private MultiItemTypeSupport<NewsItemModel> multiItemTypeSupport = null;
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
	// 时间戳
	private String latestTimesTamp = "";
	// 是否下拉
	private boolean isPullDowm = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private gridViewItemClick imageItemClickListener;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_news_list;
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
		multiItemTypeSupport = new MultiItemTypeSupport<NewsItemModel>() {

			@Override
			public int getLayoutId(int position, NewsItemModel itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case NewsItemModel.TITLE:
					layoutId = R.layout.news_item_title_layout;
					break;
				case NewsItemModel.BODY:
					layoutId = R.layout.news_item_body_layout;
					break;
				case NewsItemModel.OPERATE:
					layoutId = R.layout.news_item_operate_layout;
					break;
				case NewsItemModel.LIKELIST:
					layoutId = R.layout.news_item_likelist_layout;
					break;
				case NewsItemModel.COMMENT:
					layoutId = R.layout.news_item_comment_layout;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return NewsItemModel.TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, NewsItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case NewsItemModel.TITLE:
					itemtype = NewsItemModel.TITLE;
					break;
				case NewsItemModel.BODY:
					itemtype = NewsItemModel.BODY;
					break;
				case NewsItemModel.OPERATE:
					itemtype = NewsItemModel.OPERATE;
					break;
				case NewsItemModel.LIKELIST:
					itemtype = NewsItemModel.LIKELIST;
					break;
				case NewsItemModel.COMMENT:
					itemtype = NewsItemModel.COMMENT;
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
				// 上拉刷新
				isPullDowm = false;
				getNewsData(String.valueOf(userModel.getUid()),
						String.valueOf(currentPage), latestTimesTamp);
			}
		});

		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<NewsItemModel>(mContext, itemDataList,
				multiItemTypeSupport) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					NewsItemModel item) {

				switch (helper.layoutId) {
				case R.layout.news_item_title_layout:
					setTitleItemView(helper, item);
					break;
				case R.layout.news_item_body_layout:
					setBodyItemView(helper, item);
					break;
				case R.layout.news_item_operate_layout:
					setOperateItemView(helper, item);
					break;
				case R.layout.news_item_likelist_layout:
					setLikeListItemView(helper, item);
					break;
				case R.layout.news_item_comment_layout:
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
						newsListView.setMode(Mode.PULL_FROM_END);
						newsListView.setRefreshing(true);
						isPullDowm = false;
						getNewsData(String.valueOf(userModel.getUid()),
								String.valueOf(currentPage), latestTimesTamp);
					}
				});
		// 快宿滑动时不加载图片
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
		userModel = new UserModel();
		userModel.setUid(19);

		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new gridViewItemClick();
		initBitmapUtils();
		listItem = getLastData();
		itemDataList = NewsToItem.newsToItems(listItem);

		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
		LogUtils.i("screenWidth=" + screenWidth + " screenHeight="
				+ screenHeight);
	}

	// 测试数据，上次缓存的数据
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
		tempCmt.setSubmitterName("朱旺:");
		cmtList.add(tempCmt);
		//
		tempCmt = new CommentModel();
		tempCmt.setCommentContent("哈哈哈哈哈1");
		tempCmt.setSubmitterName("朱旺:");
		cmtList.add(tempCmt);
		//
		tempCmt = new CommentModel();
		tempCmt.setCommentContent("哈哈哈哈哈2");
		tempCmt.setSubmitterName("朱旺:");
		cmtList.add(tempCmt);
		newsData.setCommentList(cmtList);

		lastDataList.add(newsData);
		return lastDataList;
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = BitmapManager.getInstance().getBitmapUtils(mContext,
				true, true);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.img_user_head);
		// 设置图片
		LayoutParams laParams = (LayoutParams) imgView.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.img_user_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间，标签
		helper.setText(R.id.txt_user_name, titleData.getUserName());
		helper.setText(R.id.txt_publish_time, titleData.getSendTime());
		helper.setText(R.id.txt_user_tag, titleData.getUserTag());

		// 设置事件监听
		helper.setOnClickListener(R.id.img_user_head, itemViewClickListener);
		helper.setOnClickListener(R.id.txt_user_name, itemViewClickListener);
	}

	/**
	 * 设置新闻主题item
	 * */
	private void setBodyItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		BodyItem bodyData = (BodyItem) item;
		List<ImageModel> pictureList = bodyData.getNewsImageListList();

		final NoScrollGridView bodyGridView = (NoScrollGridView) helper
				.getView().findViewById(R.id.gv_news_body_image);

		// 绑定图片显示
		if (pictureList.size() == 0) {
			// 没有图片的情况
			helper.setVisible(R.id.gv_news_body_image, false);
			helper.setVisible(R.id.iv_news_body_picture, false);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			helper.setVisible(R.id.gv_news_body_image, false);
			helper.setVisible(R.id.iv_news_body_picture, true);
			ImageView imgView = helper.getView(R.id.iv_news_body_picture);
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
			helper.setImageUrl(R.id.iv_news_body_picture, bitmapUtils,
					imageModel.getURL(), new NewsBitmapLoadCallBack());

			// 设置点击事件
			itemViewClickListener.setImagePath(imageModel.getURL());
			helper.setOnClickListener(R.id.iv_news_body_picture,
					itemViewClickListener);
		} else {
			// 多张图片以九宫格显示
			helper.setVisible(R.id.gv_news_body_image, true);
			helper.setVisible(R.id.iv_news_body_picture, false);

			newsGVAdapter = new HelloHaAdapter<ImageModel>(mContext,
					R.layout.news_body_gridview_item_layout, pictureList) {
				@Override
				protected void convert(HelloHaBaseAdapterHelper helper,
						ImageModel item) {
					// 设置显示图片的imageView大小
					int desSize = (screenWidth - 20) / 3;
					ImageView imgView = helper
							.getView(R.id.iv_body_gridview_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = desSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_body_gridview_item, bitmapUtils,
							item.getSubURL(), new NewsBitmapLoadCallBack());
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
			helper.setVisible(R.id.txt_news_content, false);
		} else {
			helper.setVisible(R.id.txt_news_content, true);
			helper.setText(R.id.txt_news_content, bodyData.getNewsContent());
		}
		// 设置地理位置
		if (bodyData.getLocation().equals("")) {
			helper.setVisible(R.id.txt_news_location, false);
		} else {
			helper.setVisible(R.id.txt_news_location, true);
			helper.setText(R.id.txt_news_location, bodyData.getLocation());
		}
	}

	/**
	 * 设置操作部分item
	 * */
	private void setOperateItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		OperateItem opData = (OperateItem) item;
		helper.setText(R.id.btn_reply, "评论 " + opData.getReplyCount());
		helper.setText(R.id.btn_like, "点赞 " + opData.getLikeCount());
	}

	/**
	 * 设置点赞部分item
	 * */
	private void setLikeListItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		LikeListItem lkData = (LikeListItem) item;
		// 所有点赞的头像列表
		List<LikeModel> lkImageList = lkData.getLikeHeadListimage();

		// 点赞头像的显示
		HelloHaAdapter<LikeModel> likeGVAdapter = new HelloHaAdapter<LikeModel>(
				mContext, R.layout.news_like_gridview_item_layout, lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				// 设置头像imageview的尺寸
				ImageView imgView = helper.getView(R.id.iv_like_gridview_item);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 12;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定图片
				if (10 == helper.getPosition()) {
					helper.setImageResource(R.id.iv_like_gridview_item,
							R.drawable.ic_launcher);
				} else if (helper.getPosition() < 10) {
					bitmapUtils.configDefaultBitmapMaxSize(30, 30);
					helper.setImageUrl(R.id.iv_like_gridview_item, bitmapUtils,
							item.getHeadSubImage(),
							new NewsBitmapLoadCallBack());
				}
			}
		};
		NoScrollGridView likeGridView = (NoScrollGridView) helper.getView()
				.findViewById(R.id.like_list_gridview);
		likeGridView.setAdapter(likeGVAdapter);
	}

	/**
	 * 设置回复评论item
	 * */
	private void setComentItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		CommentItem itemData = (CommentItem) item;
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
						.intValue(), commentList.get(iCount).getSubmitterName()
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
		if (commentList.size() < 3) {
			helper.setVisible(R.id.btn_more_comment, false);
		} else {
			helper.setVisible(R.id.btn_more_comment, true);
		}
	}

	/**
	 * 获取动态数据
	 * */
	private void getNewsData(String userID, String desPage, String lastTime) {

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
							// 获取动态列表
							List<JSONObject> JSONList = (List<JSONObject>) jResult
									.get("list");
							if (JSONList.size() >= NEWS_DATA_NUM) {
								currentPage++;
							}
							JsonToNewsModel(JSONList);
							newsListView.onRefreshComplete();
							newsListView.setMode(Mode.BOTH);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							newsListView.onRefreshComplete();
							newsListView.setMode(Mode.BOTH);
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络有毒=_=");
						newsListView.onRefreshComplete();
						newsListView.setMode(Mode.BOTH);
					}

				}, null));
	}

	/**
	 * 数据处理
	 * */
	private void JsonToNewsModel(List<JSONObject> dataList) {
		List<NewsModel> newDatas = new ArrayList<NewsModel>();
		for (JSONObject newsObj : dataList) {
			NewsModel tempNews = new NewsModel();
			tempNews.setContentWithJson(newsObj);
			newDatas.add(tempNews);
		}
		latestTimesTamp = newDatas.get(0).getTimesTamp();
		if (isPullDowm) {
			newsAdapter.replaceAll(NewsToItem.newsToItems(newDatas));
		} else {
			newsAdapter.addAll(NewsToItem.newsToItems(newDatas));
		}
		dataList.clear();
	}

	/**
	 * item上的view点击事件
	 * */
	public class ItemViewClick implements OnClickListener {
		String path = "";

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.img_user_head:
				ToastUtil.show(mContext, "点击了头像");
				break;
			case R.id.txt_user_name:
				ToastUtil.show(mContext, "点击昵称");
				break;
			case R.id.iv_news_body_picture:
				// 跳转到图片详情页面
				Intent intent = new Intent(mContext, BigImgLookActivity.class);
				intent.putExtra("filePath", path);
				startActivity(intent);
			default:
				break;
			}
		}

		/**
		 * 传递图片的url
		 * */
		public void setImagePath(String imgPath) {
			this.path = imgPath;
		}

	}

	/**
	 * 
	 * 点击图片事件
	 */
	public class gridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String currentImgPath = newsGVAdapter.getItem(position).getURL();
			// 跳转到图片详情页面
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra("filePath", currentImgPath);
			startActivity(intent);
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
			//
		}

		// 加载完成时
		@Override
		public void onLoadCompleted(ImageView container, String uri,
				Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
			fadeInDisplay(container, bitmap);
		}
	}

	/**
	 * 实现两个drawable资源之间淡入淡出的效果
	 * */
	private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
		final TransitionDrawable transitionDrawable = new TransitionDrawable(
				new Drawable[] {
						new ColorDrawable(android.R.color.transparent),
						new BitmapDrawable(imageView.getResources(), bitmap) });
		imageView.setImageDrawable(transitionDrawable);
		transitionDrawable.startTransition(100);
	}
}
