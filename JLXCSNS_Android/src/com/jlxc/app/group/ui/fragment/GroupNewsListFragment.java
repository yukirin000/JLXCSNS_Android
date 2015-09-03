package com.jlxc.app.group.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.HttpCacheUtils;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.group.model.GroupItemModel;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsBodyItem;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsOperateItem;
import com.jlxc.app.group.model.GroupItemModel.GroupNewsTitleItem;
import com.jlxc.app.group.utils.NewsToItemData;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.NewsConstants;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
import com.jlxc.app.news.ui.view.LikeButton;
import com.jlxc.app.news.ui.view.LikeImageListView;
import com.jlxc.app.news.ui.view.MultiImageMetroView;
import com.jlxc.app.news.ui.view.MultiImageMetroView.JumpCallBack;
import com.jlxc.app.news.ui.view.TextViewHandel;
import com.jlxc.app.news.utils.NewsOperate;
import com.jlxc.app.news.utils.NewsOperate.LikeCallBack;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GroupNewsListFragment extends BaseFragment {

	// 动态listview
	@ViewInject(R.id.listview_group_news)
	private PullToRefreshListView newsListView;
	// 发布按钮
	// @ViewInject(R.id.img_main_publish_btn)
	// private ImageView publishBtn;
	// 原始数据源
	private List<NewsModel> newsList = new ArrayList<NewsModel>();
	// item数据源
	private List<GroupItemModel> itemDataList = null;
	// 动态列表适配器
	private HelloHaAdapter<GroupItemModel> newsAdapter = null;
	// 使支持多种item
	private MultiItemTypeSupport<GroupItemModel> multiItemTypeSupport = null;
	// 上下文信息
	private Context mContext;
	// 当前数据的页
	private int pageIndex = 1;
	// 是否是最后一页数据
	private boolean lastPage = false;
	// 时间戳
	private String latestTimesTamp = "";
	// 是否下拉
	private boolean isPullDowm = true;
	// 是否正在请求数据
	private boolean isRequestingData = false;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 对动态的操作
	private NewsOperate newsOPerate;
	// 当前点赞对应的gridview的adpter
	private LikeImageListView currentLikeListControl;
	// 加载图片
	private ImageLoader imgLoader;
	// 图片配置
	private DisplayImageOptions options;
	// 是否为文字长按事件
	private boolean isLongClick = false;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_group_news_layout;
	}

	@Override
	public void loadLayout(View rootView) {
	}

	@Override
	public void setUpViews(View rootView) {
		init();
		multiItemTypeSet();
		newsListViewSet();
		// 从服务器加载数据
		getNewsData(UserManager.getInstance().getUser().getUid(), pageIndex, "");

		// //点击发布按钮
		// publishBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// Intent intentUsrMain = new Intent(mContext,
		// PublishNewsActivity.class);
		// startActivityWithRight(intentUsrMain);
		// }
		// });
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		mContext = this.getActivity();

		itemViewClickListener = new ItemViewClick();
		newsOPerate = new NewsOperate(mContext);
		// 获取显示图片的实例
		imgLoader = ImageLoader.getInstance();
		// 显示图片的配置
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.default_avatar)
				.showImageOnFail(R.drawable.default_avatar).cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	/**
	 * listView 支持多种item的设置
	 * */
	private void multiItemTypeSet() {
		multiItemTypeSupport = new MultiItemTypeSupport<GroupItemModel>() {

			@Override
			public int getLayoutId(int position, GroupItemModel itemData) {
				int layoutId = 0;
				switch (itemData.getItemType()) {
				case GroupItemModel.GROUP_TITLE:
					layoutId = R.layout.group_news_item_title_layout;
					break;
				case GroupItemModel.GROUP_BODY:
					layoutId = R.layout.group_news_item_body_layout;
					break;
				case GroupItemModel.GROUP_OPERATE:
					layoutId = R.layout.group_news_item_operate_layout;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return GroupItemModel.NEWS_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, GroupItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case GroupItemModel.GROUP_TITLE:
					itemtype = GroupItemModel.GROUP_TITLE;
					break;
				case GroupItemModel.GROUP_BODY:
					itemtype = GroupItemModel.GROUP_BODY;
					break;
				case GroupItemModel.GROUP_OPERATE:
					itemtype = GroupItemModel.GROUP_OPERATE;
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
				if (!isRequestingData) {
					isRequestingData = true;
					pageIndex = 1;
					isPullDowm = true;
					getNewsData(UserManager.getInstance().getUser().getUid(),
							pageIndex, "");
				}
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (!lastPage && !isRequestingData) {
					isRequestingData = true;
					isPullDowm = false;
					getNewsData(UserManager.getInstance().getUser().getUid(),
							pageIndex, latestTimesTamp);
				}
			}
		});

		/**
		 * 设置底部自动刷新
		 * */
		newsListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (!lastPage) {
							newsListView.setMode(Mode.PULL_FROM_END);
							newsListView.setRefreshing(true);
						}
					}
				});

		// 添加顶部布局
		View header = View.inflate(mContext,
				R.layout.main_news_item_head_layout, null);// 头部内容
		newsListView.getRefreshableView().addHeaderView(header);

		/**
		 * adapter的设置
		 * */
		newsAdapter = new HelloHaAdapter<GroupItemModel>(mContext, itemDataList,
				multiItemTypeSupport) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					GroupItemModel item) {

				switch (helper.layoutId) {
				case R.layout.group_news_item_title_layout:
					setTitleItemView(helper, item);
					break;
				case R.layout.group_news_item_body_layout:
					setBodyItemView(helper, item);
					break;
				case R.layout.group_news_item_operate_layout:
					setOperateItemView(helper, item);
					break;

				default:
					break;
				}
			}
		};

		// 设置不可点击
		newsAdapter.setItemsClickEnable(false);
		newsListView.setAdapter(newsAdapter);
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			GroupItemModel item) {
		GroupNewsTitleItem titleData = (GroupNewsTitleItem) item;
		// 显示头像
		if (null != titleData.getUserSubHeadImage()
				&& titleData.getUserSubHeadImage().length() > 0) {
			imgLoader.displayImage(titleData.getUserSubHeadImage(),
					(ImageView) helper.getView(R.id.img_mian_news_user_head),
					options);
		} else {
			((ImageView) helper.getView(R.id.img_mian_news_user_head))
					.setImageResource(R.drawable.default_avatar);
		}

		// 设置用户名，学校，标签
		helper.setText(R.id.txt_main_news_user_name, titleData.getUserName());
		helper.setText(R.id.txt_main_news_user_school,
				titleData.getSchool());

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
	private void setBodyItemView(HelloHaBaseAdapterHelper helper, GroupItemModel item) {
		final GroupNewsBodyItem bodyData = (GroupNewsBodyItem) item;
		List<ImageModel> pictureList = bodyData.getNewsImageListList();
		// MultiImageView bodyImages =
		// helper.getView(R.id.miv_main_news_images);
		MultiImageMetroView bodyImages = helper
				.getView(R.id.miv_main_news_images);
		bodyImages.imageDataSet(pictureList);
		// 快速滑动时不加载图片
		bodyImages.loadImageOnFastSlide(newsListView, true);

		bodyImages.setJumpListener(new JumpCallBack() {

			@Override
			public void onImageClick(Intent intentToimageoBig) {
				startActivity(intentToimageoBig);
			}
		});
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (!isLongClick) {
					itemViewClickListener.onClick(view, postion, view.getId());
				}
			}
		};

		// 设置 文字内容
		if (bodyData.getNewsContent().equals("")) {
			helper.setVisible(R.id.txt_main_news_content, false);
		} else {
			//
			TextViewHandel customTvHandel = new TextViewHandel(getActivity(),
					bodyData.getNewsContent());
			helper.setVisible(R.id.txt_main_news_content, true);
			TextView contentView = helper.getView(R.id.txt_main_news_content);
			contentView.setText(bodyData.getNewsContent());
			// customTvHandel.setTextContent(contentView);
			// 长按复制
			contentView.setOnLongClickListener(TextViewHandel
					.getLongClickListener(getActivity(),
							bodyData.getNewsContent()));
			// 点击
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
		helper.setOnClickListener(R.id.miv_main_news_images, listener);
		helper.setOnClickListener(R.id.layout_news_body_rootview, listener);
	}

	/**
	 * 设置操作部分item
	 * */
	private void setOperateItemView(HelloHaBaseAdapterHelper helper,
			GroupItemModel item) {
		GroupNewsOperateItem opData = (GroupNewsOperateItem) item;
		// 点赞按钮
		LikeButton likeBtn = helper.getView(R.id.btn_news_like);
		if (opData.getIsLike()) {
			likeBtn.setStatue(true);
		} else {
			likeBtn.setStatue(false);
		}

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		// 绑定时间
		helper.setText(R.id.txt_main_news_publish_time,
				TimeHandle.getShowTimeFormat(opData.getTime()));
		// 事件监听绑定
		helper.setOnClickListener(R.id.btn_mian_reply, listener);
		helper.setOnClickListener(R.id.btn_news_like, listener);
		helper.setOnClickListener(R.id.layout_news_operate_rootview, listener);
	}

	/**
	 * 获取动态数据
	 * */
	private void getNewsData(int userID, int desPage, String lastTime) {
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
							JsonToNewsModel(JSONList);
							newsListView.onRefreshComplete();
							if (jResult.getString("is_last").equals("0")) {
								lastPage = false;
								pageIndex++;
								newsListView.setMode(Mode.BOTH);
							} else {
								lastPage = true;
								newsListView.setMode(Mode.PULL_FROM_START);
							}
							isRequestingData = false;
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							newsListView.onRefreshComplete();
							newsListView.setMode(Mode.BOTH);
							isRequestingData = false;
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(mContext, "网络抽筋了，请检查(→_→)");
						newsListView.onRefreshComplete();
						newsListView.setMode(Mode.BOTH);
						isRequestingData = false;
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
			newsAdapter.replaceAll(NewsToItemData.newsToItem(newDatas));
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
			case R.id.layout_news_title_rootview:
			case R.id.img_mian_news_user_head:
			case R.id.txt_main_news_user_name:
				GroupNewsTitleItem titleData = (GroupNewsTitleItem) newsAdapter.getItem(postion);
				if (R.id.layout_news_title_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(titleData, NewsConstants.KEY_BOARD_CLOSE,
							null);
				} else {
					jumpToHomepage(JLXCUtils.stringToInt(titleData.getUserID()));
				}
				break;

			case R.id.layout_news_body_rootview:
			case R.id.txt_main_news_content:
			case R.id.miv_main_news_images:
				GroupNewsBodyItem bodyData = (GroupNewsBodyItem) newsAdapter.getItem(postion);
				// 跳转到动态详情
				jumpToNewsDetail(bodyData, NewsConstants.KEY_BOARD_CLOSE, null);
				break;

			case R.id.btn_mian_reply:
			case R.id.btn_news_like:
			case R.id.layout_news_operate_rootview:

				final GroupNewsOperateItem operateData = (GroupNewsOperateItem) newsAdapter
						.getItem(postion);
				if (R.id.layout_news_operate_rootview == viewID) {
					// 跳转到动态详情
					jumpToNewsDetail(operateData,
							NewsConstants.KEY_BOARD_CLOSE, null);
				} else if (R.id.btn_mian_reply == viewID) {
					// 跳转至评论页面并打开评论框
					jumpToNewsDetail(operateData,
							NewsConstants.KEY_BOARD_COMMENT, null);
				} else {
					// 点赞操作
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
	 */
	private interface ListItemClickHelp {
		void onClick(View view, int postion, int viewID);
	}

	/**
	 * 点赞操作
	 * */
	private void likeOperate(int postion, View view,
			final GroupNewsOperateItem operateData) {

		final LikeButton oprtView = (LikeButton) view;
		final int likeListPostion = postion + 1;
		try {
			ListView nListView = newsListView.getRefreshableView();
			View itemRootView = nListView.getChildAt(likeListPostion + 1
					- nListView.getFirstVisiblePosition());
			currentLikeListControl = null;
			if (null != itemRootView) {
				// 点赞头像列表可见的情况下
				currentLikeListControl = (LikeImageListView) itemRootView
						.findViewById(R.id.control_news_like_listview);
			}
		} catch (Exception e) {
			LogUtils.e("动态点赞部分发生异常.");
		}

		newsOPerate.setLikeListener(new LikeCallBack() {

			@Override
			public void onOperateStart(boolean isLike) {
				if (isLike) {
					// 点赞操作
					if (null != currentLikeListControl) {
						newsOPerate.addHeadToLikeList(currentLikeListControl);
					} else {
						//newsOPerate.addDataToLikeList(newsAdapter,
						//		likeListPostion);
					}
					oprtView.setStatue(true);
					operateData.setIsLike("1");
				} else {
					// 取消点赞
					if (null != currentLikeListControl) {
						newsOPerate
								.removeHeadFromLikeList(currentLikeListControl);
					} else {
						//newsOPerate.removeDataFromLikeList(newsAdapter,
						//		likeListPostion);
					}

					oprtView.setStatue(false);
					operateData.setIsLike("0");
				}
			}

			@Override
			public void onOperateFail(boolean isLike) {
				// 撤销上次
				newsOPerate.operateRevoked();
				if (isLike) {
					oprtView.setStatue(false);
					operateData.setIsLike("0");
				} else {
					oprtView.setStatue(true);
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
	private void jumpToNewsDetail(GroupItemModel itemModel, int keyBoardMode,
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

		// 找到当前的动态对象
		for (int index = 0; index < newsList.size(); ++index) {
			if (newsList.get(index).getNewsID().equals(itemModel.getNewsID())) {
				intentToNewsDetail.putExtra(NewsConstants.INTENT_KEY_NEWS_OBJ,
						newsList.get(index));
				break;
			}
		}

		// 带有返回参数的跳转至动态详情
		startActivityForResult(intentToNewsDetail, 1);
		getActivity().overridePendingTransition(R.anim.push_right_in,
				R.anim.push_right_out);
	}

	// 平滑滚动到顶
	private void smoothToTop() {
		int firstVisiblePosition = newsListView.getRefreshableView()
				.getFirstVisiblePosition();
		if (0 == firstVisiblePosition) {
			// 已经在顶部
			newsListView.setMode(Mode.PULL_FROM_START);
			newsListView.setRefreshing();
		} else {
			if (firstVisiblePosition < 20) {
				newsListView.getRefreshableView().smoothScrollToPosition(0);
			} else {
				newsListView.getRefreshableView().setSelection(20);
				newsListView.getRefreshableView().smoothScrollToPosition(0);
			}
			newsListView.getRefreshableView().clearFocus();
		}
	}
}
