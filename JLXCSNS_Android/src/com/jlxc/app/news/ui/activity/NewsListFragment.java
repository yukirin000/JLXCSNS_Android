package com.jlxc.app.news.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.model.CommentModel;
import com.jlxc.app.base.model.ImageModel;
import com.jlxc.app.base.model.LikeModel;
import com.jlxc.app.base.model.NewsItemModel;
import com.jlxc.app.base.model.SchoolModel;
import com.jlxc.app.base.model.NewsItemModel.BodyItem;
import com.jlxc.app.base.model.NewsItemModel.LikeListItem;
import com.jlxc.app.base.model.NewsItemModel.OperateItem;
import com.jlxc.app.base.model.NewsItemModel.ReplyItem;
import com.jlxc.app.base.model.NewsItemModel.TitleItem;
import com.jlxc.app.base.model.NewsModel;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.ui.fragment.BaseFragmentWithTopBar;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.NewsToItem;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class NewsListFragment extends BaseFragment {

	// 动态listview
	@ViewInject(R.id.news_listview)
	private PullToRefreshListView newsListView;
	// 原始数据源
	private List<NewsModel> listItem = new ArrayList<NewsModel>();
	// item数据源
	private List<NewsItemModel> itemDataList = null;
	// 适配器
	private HelloHaAdapter<NewsItemModel> newsAdapter = null;
	// 使支持多种item
	private MultiItemTypeSupport<NewsItemModel> multiItemTypeSupport = null;
	// 上下文信息
	private Context mContext;
	//
	private static BitmapUtils bitmapUtils;
	// 评论部分的控件
	private List<Map<String, Integer>> commentViewList;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_news_list;
	}

	@Override
	public void loadLayout(View rootView) {
		init();
		multiItemTypeSet();
		newsListviewSet();
		// 将评论控件存储在map
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
				case NewsItemModel.REPLY:
					layoutId = R.layout.news_item_replylist_layout;
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
				case NewsItemModel.REPLY:
					itemtype = NewsItemModel.REPLY;
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
	private void newsListviewSet() {
		// 设置刷新模式
		newsListView.setMode(Mode.BOTH);
		/**
		 * 刷新监听
		 * */
		newsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				getNewsData("19", "2", "1436366058");
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
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
				case R.layout.news_item_replylist_layout:
					setReplyItemView(helper, item);
					break;

				default:
					break;
				}
			}
		};

		// 设置适配器
		ListView actualListView = newsListView.getRefreshableView();
		// 设置不可点击
		newsAdapter.setItemsClickEnable(false);
		actualListView.setAdapter(newsAdapter);
	}

	@Override
	public void setUpViews(View rootView) {

	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		mContext = this.getActivity().getApplicationContext();
		initBitmapUtils();
		// listItem = getLastData();
		itemDataList = NewsToItem.newsToItems(listItem);
	}

	private List<NewsModel> getLastData() {
		String pathStr = "http://192.168.1.100/jlxc_php/Uploads/2015-06-30/401435667218_sub.png";
		NewsModel newsData = new NewsModel();
		newsData.setUserName("朱旺");
		newsData.setUserHeadSubImage(pathStr);
		newsData.setSendTime("12:11");
		newsData.setUserSchool("罗湖中学");
		
		newsData.setNewsContent("哈哈哈哈哈哈哈哈哈");
		return null;
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = BitmapManager.getInstance().getBitmapUtils(mContext,
				false, false);
		bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
		// 设置最大宽高, 不设置时更具控件属性自适应.
		bitmapUtils.configDefaultBitmapMaxSize(BitmapCommonUtils.getScreenSize(
				getActivity()).scaleDown(3));
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		helper.setImageUrl(R.id.img_user_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间，标签
		helper.setText(R.id.txt_user_name, titleData.getUserName());
		helper.setText(R.id.txt_publish_time, titleData.getSendTime());
		helper.setText(R.id.txt_user_tag, titleData.getUserTag());
	}

	/**
	 * 设置新闻主题item
	 * */
	private void setBodyItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		BodyItem bodyData = (BodyItem) item;
		// 所有图片列表
		List<ImageModel> pictureList = (List<ImageModel>) bodyData
				.getNewsImageListList();

		// 设置文本内日
		helper.setText(R.id.txt_news_content, bodyData.getNewsContent());
		// 动态中的九宫格图片显示
		HelloHaAdapter<ImageModel> newsGVAdapter = new HelloHaAdapter<ImageModel>(
				mContext, R.layout.news_body_gridview_item_layout, pictureList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					ImageModel item) {
				// 设置图片
				helper.setImageUrl(R.id.iv_body_gridview_item, bitmapUtils,
						item.getSubURL(), new NewsBitmapLoadCallBack());
				LogUtils.i("***:" + item.getSubURL());
			}
		};
		NoScrollGridView bodyGridView = (NoScrollGridView) helper.getView()
				.findViewById(R.id.gv_news_body_image);
		// 设置九宫格格式
		if (pictureList.size() == 0) {
			helper.setVisible(R.id.gv_news_body_image, false);
		} else if (pictureList.size() == 1) {
			helper.setVisible(R.id.gv_news_body_image, true);
			bodyGridView.setNumColumns(1);
		} else {
			helper.setVisible(R.id.gv_news_body_image, true);
			bodyGridView.setNumColumns(3);
		}
		bodyGridView.setAdapter(newsGVAdapter);

		// 设置地理位置
		helper.setText(R.id.txt_news_location, bodyData.getLocation());
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
		List<LikeModel> lkImageList = (List<LikeModel>) lkData
				.getLikeHeadListimage();

		// 点赞头像的显示
		HelloHaAdapter<LikeModel> likeGVAdapter = new HelloHaAdapter<LikeModel>(
				mContext, R.layout.news_like_gridview_item_layout, lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				if (10 == helper.getPosition()) {
					// 最后一张显示为加载按钮
					helper.setImageResource(R.id.iv_like_gridview_item,
							R.drawable.ic_launcher);
				} else if (helper.getPosition() < 10) {
					// 设置头像
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
	private void setReplyItemView(HelloHaBaseAdapterHelper helper,
			NewsItemModel item) {
		ReplyItem replyData = (ReplyItem) item;
		// 所有的评论数据
		List<CommentModel> commentList = (List<CommentModel>) replyData
				.getReplyList();

		LogUtils.i(" commentList.size()=" + commentList.size());
		// 显示三条评论
		for (int iCount = 0; iCount < 3; ++iCount) {
			if (iCount < commentList.size()) {
				helper.setText(commentViewList.get(iCount).get("NAME")
						.intValue(), commentList.get(iCount).getSubmitterName());
				helper.setText(commentViewList.get(iCount).get("CONTENT")
						.intValue(), commentList.get(iCount)
						.getCommentContent());
			} else {
				helper.setVisible(commentViewList.get(iCount).get("NAME")
						.intValue(), false);
				helper.setVisible(commentViewList.get(iCount).get("CONTENT")
						.intValue(), false);
			}

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
							List<JSONObject> objList = (List<JSONObject>) jResult
									.get("list");
							newsDataHandle(objList);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络有毒=_=");
					}

				}, null));
	}

	/**
	 * 数据处理
	 * */
	private void newsDataHandle(List<JSONObject> dataList) {
		List<NewsModel> newDatas = new ArrayList<NewsModel>();
		for (JSONObject newsObj : dataList) {
			NewsModel tempModel = new NewsModel();
			tempModel.setContentWithJson(newsObj);
			newDatas.add(tempModel);
		}
		newsAdapter.replaceAll(NewsToItem.newsToItems(newDatas));
		dataList.clear();
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
