package com.jlxc.app.news.ui.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

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
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.view.KeyboardLayout;
import com.jlxc.app.base.ui.view.KeyboardLayout.onKeyboardsChangeListener;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CommentItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
import com.jlxc.app.news.model.ItemModel.SubCommentItem;
import com.jlxc.app.news.model.ItemModel.TitleItem;
import com.jlxc.app.news.model.LikeModel;
import com.jlxc.app.news.model.NewsModel;
import com.jlxc.app.news.model.NewsOperateModel;
import com.jlxc.app.news.model.SubCommentModel;
import com.jlxc.app.news.utils.DataToItem;
import com.jlxc.app.news.utils.NewsOperate;
import com.jlxc.app.news.utils.NewsOperate.LikeCallBack;
import com.jlxc.app.news.utils.NewsOperate.OperateCallBack;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rockerhieu.emojicon.EmojiconEditText;

public class NewsDetailActivity extends BaseActivityWithTopBar {

	// 当前的操作类型
	private int operateType = NewsOperateModel.Input_Type_Comment;
	// 评论的类型
	private int commentType = NewsOperateModel.Input_Type_Comment;
	// 主listview
	@ViewInject(R.id.news_detail_listView)
	private PullToRefreshListView newsDetailListView;
	// 评论输入框
	@ViewInject(R.id.edt_comment_input)
	private EmojiconEditText commentEditText;
	// 评论发送按钮
	@ViewInject(R.id.btn_comment_send)
	private Button btnSendComment;
	// 数据源
	private List<ItemModel> dataList;
	// 主适配器
	private HelloHaAdapter<ItemModel> detailAdapter;
	// 当前的动态对象
	private NewsModel currentNews;
	// 用户实例
	private UserModel userModel;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 使支持多种item
	private MultiItemTypeSupport<ItemModel> multiItemTypeSupport = null;
	// 点击view监听对象
	private ItemViewClick itemViewClickListener;
	// 点击图片监听
	private ImageGridViewItemClick imageItemClickListener;
	// 对动态的操作
	private NewsOperate newsOPerate;
	// 点赞头像gridview
	private NoScrollGridView likeGridView;
	// 点赞适配器
	private HelloHaAdapter<LikeModel> likeGVAdapter;
	// 点击点赞头像监听
	private LikeGridViewItemClick likeItemClickListener;
	// 评论的内容
	private String commentContent = "";
	// 当前的操作的item
	private CommentModel currentCommentModel;
	// 当前的操作的子评论对象
	private SubCommentModel currentSubCmtModel;
	// 当前操作的位置
	private int currentOperateIndex = 0;
	// 是否是第一次请求数据
	private boolean firstRequstData = true;

	/**
	 * 事件监听函数
	 * */
	@OnClick(value = { R.id.base_ll_right_btns, R.id.btn_comment_send })
	private void clickEvent(View view) {
		switch (view.getId()) {
		// 删除动态
		case R.id.base_ll_right_btns:
			operateType = NewsOperateModel.OPERATE_DELETET;
			deleteCurrentNews();
			break;

		// 发布评论
		case R.id.btn_comment_send:
			operateType = NewsOperateModel.OPERATE_UPDATE;
			publishComment();
			break;
		default:
			break;
		}
	}

	@Override
	public int setLayoutId() {
		return R.layout.activity_news_detail;
	}

	@Override
	protected void setUpView() {
		init();
		multiItemTypeSet();
		listViewSet();
		newsOperateSet();

		// 监听输入框文本的变化
		commentEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence str, int start, int before,
					int count) {
				commentContent = str.toString().trim();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// 监听软键盘是否打开
		((KeyboardLayout) findViewById(R.id.news_detail_root_view))
				.setOnkeyboarddStateListener(new onKeyboardsChangeListener() {

					@Override
					public void onKeyBoardStateChange(int state) {
						if (KeyboardLayout.KEYBOARD_STATE_HIDE == state) {
							// 内容为空并且，软键盘隐藏时
							if (commentContent.length() <= 0) {
								commentType = NewsOperateModel.Input_Type_Comment;
								commentEditText.setHint("是时候来条神评论了...");
							}
						}
					}
				});

		Intent intent = this.getIntent();
		if (null != intent) {
			if (intent.hasExtra(NewsOperateModel.INTENT_KEY_NEWS_OBJ)) {
				currentNews = (NewsModel) intent
						.getSerializableExtra(NewsOperateModel.INTENT_KEY_NEWS_OBJ);
				// 获取传递过来的的数据
				detailAdapter.replaceAll(DataToItem
						.newsDetailToItems(currentNews));
			} else if (intent.hasExtra(NewsOperateModel.INTENT_KEY_NEWS_ID)) {
				currentNews = new NewsModel();
				currentNews.setNewsID(intent
						.getStringExtra(NewsOperateModel.INTENT_KEY_NEWS_ID));
			} else {
				LogUtils.e("未传递任何动态信息到详情页面.");
			}
		} else {
			LogUtils.e("跳转到详情页面时，意图null.");
		}

		// 更新数据
		getNewsDetailData(String.valueOf(userModel.getUid()),
				currentNews.getNewsID());
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		userModel = UserManager.getInstance().getUser();

		dataList = new ArrayList<ItemModel>();
		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		initBitmapUtils();

		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
	}

	/**
	 * 初始状态处理
	 * */
	private void stateHandel() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		switch (bundle.getInt(NewsOperateModel.INTENT_KEY_COMMENT_STATE)) {
		case NewsOperateModel.KEY_BOARD_CLOSE:
			btnSendComment.setFocusable(true);
			break;
		case NewsOperateModel.KEY_BOARD_COMMENT:
			commentEditText.setFocusable(true);
			commentEditText.setFocusableInTouchMode(true);
			commentEditText.requestFocus();
			commentType = NewsOperateModel.Input_Type_Comment;
			setKeyboardStatu(true);
			break;
		case NewsOperateModel.KEY_BOARD_REPLY:
			// 直接回复评论
			String cmtId = bundle
					.getString(NewsOperateModel.INTENT_KEY_COMMENT_ID);

			for (int index = 0; index < dataList.size(); ++index) {
				ItemModel tempItemModel = dataList.get(index);
				int itemType = tempItemModel.getItemType();
				if (ItemModel.NEWS_DETAIL_COMMENT == itemType) {
					// 回复评论
					currentCommentModel = ((CommentItem) tempItemModel)
							.getCommentModel();
					if (currentCommentModel.getCommentID().equals(cmtId)) {
						commentEditText.setHint("回复："
								+ currentCommentModel.getPublishName());
						commentType = NewsOperateModel.Input_Type_SubComment;
						break;
					}
				} else if (ItemModel.NEWS_DETAIL_SUB_COMMENT == itemType) {
					// 回复子评论
					currentSubCmtModel = ((SubCommentItem) tempItemModel)
							.getSubCommentModel();
					if (currentSubCmtModel.getSubID().equals(cmtId)) {
						commentEditText.setHint("回复："
								+ currentSubCmtModel.getPublishName());
						commentType = NewsOperateModel.Input_Type_SubReply;
						break;
					}
				}
				currentOperateIndex = index + 1;
			}
			commentEditText.setFocusable(true);
			commentEditText.setFocusableInTouchMode(true);
			commentEditText.requestFocus();
			setKeyboardStatu(true);

			break;

		default:
			break;
		}
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = new BitmapUtils(NewsDetailActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * 设置键盘状态
	 * */
	private void setKeyboardStatu(boolean state) {
		if (state) {
			InputMethodManager imm = (InputMethodManager) commentEditText
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
		} else {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(NewsDetailActivity.this
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 动态操作设置
	 * */
	private void newsOperateSet() {
		newsOPerate = new NewsOperate(NewsDetailActivity.this);
		newsOPerate.setOperateListener(new OperateCallBack() {
			@Override
			public void onStart(int operateType) {
				userModel = UserManager.getInstance().getUser();
				LogUtils.i("头像：" + userModel.getHead_sub_image());
				switch (operateType) {
				case NewsOperate.OP_Type_Delete_News:
					break;

				case NewsOperate.OP_Type_Add_Comment:
					showLoading("努力发布中...", true);

				case NewsOperate.OP_Type_Delete_Comment:
					break;

				case NewsOperate.OP_Type_Add_Sub_Comment: {
					showLoading("努力发布中...", true);
				}
					break;

				case NewsOperate.OP_Type_Delete_Sub_Comment:

					break;
				default:
					break;
				}

			}

			@Override
			public void onFinish(int operateType, boolean isSucceed,
					Object resultValue) {
				switch (operateType) {
				case NewsOperate.OP_Type_Delete_News:
					if (isSucceed) {
						ToastUtil.show(NewsDetailActivity.this, "删除成功");
						// 返回上一页
						Intent intentBack = new Intent();
						setResult(NewsOperateModel.OPERATE_DELETET, intentBack);
						finishWithRight();
					}
					break;

				case NewsOperate.OP_Type_Add_Comment:
					if (isSucceed) {
						// 发布成功则更新评论
						CommentModel resultmModel = (CommentModel) resultValue;
						detailAdapter.add(DataToItem.createComment(
								resultmModel, ItemModel.NEWS_DETAIL_COMMENT));
						// 滚动到底部
						newsDetailListView.getRefreshableView().setSelection(
								detailAdapter.getCount() - 1);
						hideLoading();
					} else {
						hideLoading();
					}
					break;

				case NewsOperate.OP_Type_Delete_Comment:
					if (isSucceed) {
						ToastUtil.show(NewsDetailActivity.this, "删除成功");
						detailAdapter.remove(currentOperateIndex);
					} else {
						ToastUtil.show(NewsDetailActivity.this, "竟然删除失败");
					}
					break;

				case NewsOperate.OP_Type_Add_Sub_Comment:
					if (isSucceed) {
						// 发布成功则更新评论
						SubCommentModel resultmModel = (SubCommentModel) resultValue;
						// 找到需要插入的位置
						int index = currentOperateIndex + 1;
						while (index < detailAdapter.getCount()) {
							int itemType = detailAdapter.getItem(index)
									.getItemType();
							if (ItemModel.NEWS_DETAIL_COMMENT == itemType) {
								break;
							}
							index++;
						}
						// 插入子评论
						detailAdapter.insert(index, DataToItem
								.createSubComment(resultmModel,
										currentNews.getNewsID(),
										ItemModel.NEWS_DETAIL_SUB_COMMENT));
						// 滚动到添加的评论处
						newsDetailListView.getRefreshableView().setSelection(
								index);
						hideLoading();
					} else {
						hideLoading();
					}
					break;

				case NewsOperate.OP_Type_Delete_Sub_Comment:
					if (isSucceed) {
						detailAdapter.remove(currentOperateIndex);
						ToastUtil.show(NewsDetailActivity.this, "删除成功");
					} else {
						ToastUtil.show(NewsDetailActivity.this, "竟然删除失败");
					}
					break;
				default:
					break;
				}
			}
		});
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
				case ItemModel.NEWS_DETAIL_TITLE:
					layoutId = R.layout.news_detail_title_layout;
					break;
				case ItemModel.NEWS_DETAIL_BODY:
					layoutId = R.layout.news_detail_body_layout;
					break;
				case ItemModel.NEWS_DETAIL_LIKELIST:
					layoutId = R.layout.news_detail_likelist_layout;
					break;
				case ItemModel.NEWS_DETAIL_COMMENT:
					layoutId = R.layout.news_detail_comment_layout;
					break;
				case ItemModel.NEWS_DETAIL_SUB_COMMENT:
					layoutId = R.layout.news_detail_subcomment_layout;
					break;
				default:
					break;
				}
				return layoutId;
			}

			@Override
			public int getViewTypeCount() {
				return ItemModel.NEWS_DETAIL_ITEM_TYPE_COUNT;
			}

			@Override
			public int getItemViewType(int postion, ItemModel itemData) {
				int itemtype = 0;
				switch (itemData.getItemType()) {
				case ItemModel.NEWS_DETAIL_TITLE:
					itemtype = ItemModel.NEWS_DETAIL_TITLE;
					break;
				case ItemModel.NEWS_DETAIL_BODY:
					itemtype = ItemModel.NEWS_DETAIL_BODY;
					break;
				case ItemModel.NEWS_DETAIL_LIKELIST:
					itemtype = ItemModel.NEWS_DETAIL_LIKELIST;
					break;
				case ItemModel.NEWS_DETAIL_COMMENT:
					itemtype = ItemModel.NEWS_DETAIL_COMMENT;
					break;
				case ItemModel.NEWS_DETAIL_SUB_COMMENT:
					itemtype = ItemModel.NEWS_DETAIL_SUB_COMMENT;
					break;
				default:
					break;
				}
				return itemtype;

			}
		};
	}

	/**
	 * listview设置
	 * */
	private void listViewSet() {

		// 设置刷新模式
		newsDetailListView.setMode(Mode.PULL_FROM_START);
		/**
		 * 刷新监听
		 * */
		newsDetailListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						firstRequstData = false;
						getNewsDetailData(String.valueOf(userModel.getUid()),
								currentNews.getNewsID());
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// 上拉
						/*
						 * getNewsDetailData(String.valueOf(userModel.getUid()),
						 * currentNews.getNewsID());
						 */
					}
				});

		/**
		 * adapter的设置
		 * */
		detailAdapter = new HelloHaAdapter<ItemModel>(NewsDetailActivity.this,
				dataList, multiItemTypeSupport) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					ItemModel item) {

				switch (helper.layoutId) {
				case R.layout.news_detail_title_layout:
					setTitleItemView(helper, item);
					break;
				case R.layout.news_detail_body_layout:
					setBodyItemView(helper, item);
					break;
				case R.layout.news_detail_likelist_layout:
					setLikeListItemView(helper, item);
					break;
				case R.layout.news_detail_comment_layout:
					setComentItemView(helper, item);
					break;
				case R.layout.news_detail_subcomment_layout:
					setSubComentItemView(helper, item);
					break;

				default:
					break;
				}
			}
		};

		// 快速滑动时不加载图片
		newsDetailListView.setOnScrollListener(new PauseOnScrollListener(
				bitmapUtils, false, true));
		// 设置不可点击
		detailAdapter.setItemsClickEnable(false);
		newsDetailListView.setAdapter(detailAdapter);
	}

	/**
	 * titleItem的数据绑定与设置
	 * */
	private void setTitleItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		TitleItem titleData = (TitleItem) item;

		// 设置头像
		ImageView imgView = helper.getView(R.id.img_news_detail_user_head);
		// 设置图片
		LinearLayout.LayoutParams laParams = (LinearLayout.LayoutParams) imgView
				.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 6;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.img_news_detail_user_head, bitmapUtils,
				titleData.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置用户名,发布的时间，标签
		helper.setText(R.id.txt_news_detail_user_name, titleData.getUserName());
		helper.setText(R.id.txt_news_detail_publish_time,
				TimeHandle.getShowTimeFormat(titleData.getSendTime()));
		helper.setText(R.id.txt_news_detail_user_tag, titleData.getUserTag());
		if (titleData.getIsLike()) {
			helper.setText(R.id.btn_news_detail_like, "已赞 ");
		} else {
			helper.setText(R.id.btn_news_detail_like, "点赞 ");
		}

		// 设置事件监听
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.img_news_detail_user_head, listener);
		helper.setOnClickListener(R.id.txt_news_detail_user_name, listener);
		helper.setOnClickListener(R.id.btn_news_detail_like, listener);

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
			helper.setVisible(R.id.gv_news_detail_body_image, false);
			helper.setVisible(R.id.iv_news_detail_body_picture, false);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			helper.setVisible(R.id.gv_news_detail_body_image, false);
			helper.setVisible(R.id.iv_news_detail_body_picture, true);
			ImageView imgView = helper
					.getView(R.id.iv_news_detail_body_picture);
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
			helper.setImageUrl(R.id.iv_news_detail_body_picture, bitmapUtils,
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
			helper.setVisible(R.id.gv_news_detail_body_image, true);
			helper.setVisible(R.id.iv_news_detail_body_picture, false);
			NoScrollGridView bodyGridView = (NoScrollGridView) helper
					.getView(R.id.gv_news_detail_body_image);
			HelloHaAdapter<ImageModel> newsGVAdapter = new HelloHaAdapter<ImageModel>(
					NewsDetailActivity.this,
					R.layout.news_detail_body_gridview_item_layout, pictureList) {
				@Override
				protected void convert(HelloHaBaseAdapterHelper helper,
						ImageModel item) {
					// 设置显示图片的imageView大小
					int desSize = (screenWidth - 20) / 3;
					ImageView imgView = helper
							.getView(R.id.iv_news_detail_body_gridview_item);
					LayoutParams laParams = (LayoutParams) imgView
							.getLayoutParams();
					laParams.width = laParams.height = desSize;
					imgView.setLayoutParams(laParams);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					bitmapUtils.configDefaultBitmapMaxSize(screenWidth,
							screenWidth);
					helper.setImageUrl(R.id.iv_news_detail_body_gridview_item,
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
			helper.setVisible(R.id.txt_news_detail_content, false);
		} else {
			helper.setVisible(R.id.txt_news_detail_content, true);
			helper.setText(R.id.txt_news_detail_content,
					bodyData.getNewsContent());
		}
		// 设置地理位置
		if (bodyData.getLocation().equals("")) {
			helper.setVisible(R.id.txt_news_detail_location, false);
		} else {
			helper.setVisible(R.id.txt_news_detail_location, true);
			helper.setText(R.id.txt_news_detail_location,
					bodyData.getLocation());
		}
	}

	/**
	 * 设置点赞部分item
	 * */
	private void setLikeListItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		LikeListItem lkData = (LikeListItem) item;
		List<LikeModel> lkImageList = lkData.getLikeHeadListimage();

		helper.setVisible(R.id.news_detail_likelist_rootview, true);
		// 点赞头像的显示
		likeGVAdapter = new HelloHaAdapter<LikeModel>(NewsDetailActivity.this,
				R.layout.news_detail_like_gridview_item_layout, lkImageList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					LikeModel item) {
				// 设置头像imageview的尺寸
				ImageView imgView = helper
						.getView(R.id.iv_news_detail_like_gridview_item);
				LinearLayout.LayoutParams laParams = (LinearLayout.LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 12;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定图片
				if (helper.getPosition() < NewsOperateModel.MAX_LIKE_COUNT) {
					bitmapUtils.configDefaultBitmapMaxSize(30, 30);
					helper.setImageUrl(R.id.iv_news_detail_like_gridview_item,
							bitmapUtils, item.getHeadSubImage(),
							new NewsBitmapLoadCallBack());
				} else if (10 == helper.getPosition()) {
					helper.setImageResource(
							R.id.iv_news_detail_like_gridview_item,
							R.drawable.ic_launcher);
				}
			}
		};
		likeGridView = (NoScrollGridView) helper
				.getView(R.id.gv_news_detail_Like_list);
		likeGridView.setAdapter(likeGVAdapter);
		likeGridView.setOnItemClickListener(likeItemClickListener);
	}

	/**
	 * 设置评论item
	 * */
	private void setComentItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		CommentModel comment = ((CommentItem) item).getCommentModel();
		// 设置评论者的头像
		ImageView imgView = helper.getView(R.id.iv_comment_head);
		LinearLayout.LayoutParams laParams = (LinearLayout.LayoutParams) imgView
				.getLayoutParams();
		laParams.width = laParams.height = (screenWidth) / 8;
		imgView.setLayoutParams(laParams);
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		bitmapUtils.configDefaultBitmapMaxSize((screenWidth) / 4,
				(screenWidth) / 4);
		helper.setImageUrl(R.id.iv_comment_head, bitmapUtils,
				comment.getHeadSubImage(), new NewsBitmapLoadCallBack());
		// 设置评论的时间、学校与内容
		helper.setText(R.id.txt_news_detail_comment_time,
				TimeHandle.getShowTimeFormat(comment.getAddDate()));
		helper.setText(R.id.txt_news_detail_comment_name,
				comment.getPublishName());
		helper.setText(R.id.txt_news_detail_comment_content,
				comment.getCommentContent());

		// 设置评论item的点击事件
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.iv_comment_head, listener);
		helper.setOnClickListener(R.id.reply_head_layout, listener);
		helper.setOnClickListener(R.id.txt_news_detail_comment_name, listener);
	}

	/**
	 * 设置子评论item
	 * */
	private void setSubComentItemView(HelloHaBaseAdapterHelper helper,
			ItemModel item) {
		SubCommentModel subCmtModel = ((SubCommentItem) item)
				.getSubCommentModel();

		helper.setText(R.id.txt_sub_comment_by_name,
				subCmtModel.getPublishName());
		helper.setText(R.id.txt_by_sub_comment_name, subCmtModel.getReplyName());
		helper.setText(R.id.txt_sub_comment_content,
				subCmtModel.getCommentContent());

		// 设置评论item的点击事件
		final int postion = helper.getPosition();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		};
		helper.setOnClickListener(R.id.subcomment_root_view, listener);
		helper.setOnClickListener(R.id.txt_sub_comment_by_name, listener);
		helper.setOnClickListener(R.id.txt_by_sub_comment_name, listener);
	}

	/**
	 * 数据处理
	 */
	private void JsonToNewsModel(JSONObject data) {
		currentNews.setContentWithJson(data);
		// 如果是自己发布的动态
		if (currentNews.getUid().equals(String.valueOf(userModel.getUid()))) {
			addRightBtn("删除");
		}
		dataList = DataToItem.newsDetailToItems(currentNews);
		detailAdapter.replaceAll(dataList);
	}

	/**
	 * 获取动态详情数据
	 * */
	private void getNewsDetailData(String uid, String newsId) {
		String path = JLXCConst.NEWS_DETAIL + "?" + "news_id=" + newsId
				+ "&user_id=" + uid;
		HttpManager.get(path, new JsonRequestCallBack<String>(
				new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							JsonToNewsModel(jResult);
							if (firstRequstData) {
								stateHandel();
							} else {
								newsDetailListView.onRefreshComplete();
							}
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));

							if (!firstRequstData) {
								newsDetailListView.onRefreshComplete();
							}
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						if (!firstRequstData) {
							newsDetailListView.onRefreshComplete();
						}
						ToastUtil.show(NewsDetailActivity.this, "网络有毒=_=");
					}

				}, null));
	}

	/**
	 * 删除动态
	 * */
	private void deleteCurrentNews() {
		final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
		alterDialog.setMessage("真的狠心删除吗？");
		alterDialog.setCancelable(true);

		alterDialog.setPositiveButton("是的",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						newsOPerate.deleteNews(currentNews.getNewsID());
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

	// 点击回复发送按钮
	private void publishComment() {
		if (commentContent.length() > 0) {
			String tempContent = commentContent;
			// 清空输入内容
			commentEditText.setText("");
			commentEditText.setHint("来条神评论...");
			// 隐藏输入键盘
			setKeyboardStatu(false);

			switch (commentType) {
			case NewsOperateModel.Input_Type_Comment:
				// 发布评论
				CommentModel temMode = new CommentModel();
				temMode.setCommentContent(tempContent);
				temMode.setAddDate(TimeHandle.getCurrentDataStr());
				newsOPerate.publishComment(userModel, currentNews.getNewsID(),
						tempContent);
				break;

			case NewsOperateModel.Input_Type_SubComment:
				// 发布二级评论
				SubCommentModel tempMd = new SubCommentModel();
				tempMd.setCommentContent(tempContent);
				tempMd.setReplyUid(currentCommentModel.getUserId());
				tempMd.setReplyName(currentCommentModel.getPublishName());
				tempMd.setReplyCommentId(currentCommentModel.getCommentID());
				tempMd.setTopCommentId(currentCommentModel.getCommentID());
				newsOPerate.publishSubComment(userModel,
						currentNews.getNewsID(), tempMd);
				break;

			case NewsOperateModel.Input_Type_SubReply:
				// 发布子回复
				SubCommentModel tpMold = new SubCommentModel();
				tpMold.setCommentContent(tempContent);
				tpMold.setReplyUid(currentSubCmtModel.getPublishId());
				tpMold.setReplyName(currentSubCmtModel.getPublishName());
				tpMold.setReplyCommentId(currentSubCmtModel.getSubID());
				tpMold.setTopCommentId(currentSubCmtModel.getTopCommentId());
				newsOPerate.publishSubComment(userModel,
						currentNews.getNewsID(), tpMold);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			operateType = NewsOperateModel.OPERATE_UPDATE;
			switch (viewID) {
			case R.id.img_news_detail_user_head:
			case R.id.txt_news_detail_user_name:
				TitleItem titleData = (TitleItem) detailAdapter
						.getItem(postion);
				JumpToHomepage(JLXCUtils.stringToInt(titleData.getUserID()));
				break;

			case R.id.iv_news_detail_body_picture:
				BodyItem bodyData = (BodyItem) detailAdapter.getItem(postion);
				String path = bodyData.getNewsImageListList().get(0).getURL();
				// 跳转到图片详情页面
				jumpToBigImage(BigImgLookActivity.INTENT_KEY, path, 0);
				break;

			case R.id.btn_news_detail_like:
				final TitleItem operateData = (TitleItem) detailAdapter
						.getItem(postion);
				final View oprtView = view;
				newsOPerate.setLikeListener(new LikeCallBack() {

					@Override
					public void onOperateStart(boolean isLike) {
						if (isLike) {
							newsOPerate.addHeadToLikeList(likeGVAdapter);
							((Button) oprtView).setText("已赞");
							operateData.setIsLike("1");
						} else {
							newsOPerate.removeHeadFromLikeList(likeGVAdapter);
							((Button) oprtView).setText("点赞");
							operateData.setIsLike("0");
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
						// 撤销上次操作
						newsOPerate.operateRevoked();
					}
				});

				if (operateData.getIsLike()) {
					newsOPerate.uploadLikeOperate(userModel,
							currentNews.getNewsID(), false);
				} else {
					newsOPerate.uploadLikeOperate(userModel,
							currentNews.getNewsID(), true);
				}
				break;

			case R.id.reply_head_layout:
			case R.id.iv_comment_head:
			case R.id.txt_news_detail_comment_name:
				currentCommentModel = ((CommentItem) detailAdapter
						.getItem(postion)).getCommentModel();
				if (viewID == R.id.reply_head_layout) {
					currentOperateIndex = postion;
					if (currentCommentModel.getUserId().equals(
							String.valueOf(userModel.getUid()))) {
						// 如果是自己发布的评论，则删除评论
						CharSequence[] items = { "删除评论" };
						new AlertDialog.Builder(NewsDetailActivity.this)
								.setItems(items,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												newsOPerate.deleteComment(
														currentCommentModel
																.getCommentID(),
														currentNews.getNewsID());
											}

										}).show();
					} else {
						// 发布回复别人的评论
						commentEditText.requestFocus();
						commentEditText.setHint("回复："
								+ currentCommentModel.getPublishName());
						commentType = NewsOperateModel.Input_Type_SubComment;
						// 显示键盘
						setKeyboardStatu(true);
					}
				} else {
					JumpToHomepage(JLXCUtils.stringToInt(currentCommentModel
							.getUserId()));
				}

				break;

			// 点击了子评论
			case R.id.subcomment_root_view:
			case R.id.txt_sub_comment_by_name:
			case R.id.txt_by_sub_comment_name:
				currentSubCmtModel = ((SubCommentItem) detailAdapter
						.getItem(postion)).getSubCommentModel();
				if (viewID == R.id.subcomment_root_view) {
					currentOperateIndex = postion;
					if (currentSubCmtModel.getPublishId().equals(
							String.valueOf(userModel.getUid()))) {
						// 如果是自己发布的评论，则删除评论
						CharSequence[] items = { "删除评论" };
						new AlertDialog.Builder(NewsDetailActivity.this)
								.setItems(items,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												newsOPerate.deleteSubComment(
														currentSubCmtModel
																.getSubID(),
														currentNews.getNewsID());
											}
										}).show();
					} else {
						// 找到topComment
						int index = postion;
						while (index >= 0) {
							if (ItemModel.NEWS_DETAIL_COMMENT == detailAdapter
									.getItem(index).getItemType()) {
								break;
							}
							--index;
						}
						currentCommentModel = ((CommentItem) detailAdapter
								.getItem(index)).getCommentModel();

						commentEditText.requestFocus();
						commentEditText.setHint("回复："
								+ currentSubCmtModel.getPublishName());
						commentType = NewsOperateModel.Input_Type_SubReply;
						setKeyboardStatu(true);
					}
				} else if (viewID == R.id.txt_sub_comment_by_name) {
					JumpToHomepage(JLXCUtils.stringToInt(currentSubCmtModel
							.getPublishId()));
				} else {
					JumpToHomepage(JLXCUtils.stringToInt(currentSubCmtModel
							.getReplyUid()));
				}

				break;

			default:
				break;
			}
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
			JumpToHomepage(JLXCUtils.stringToInt(likeUser.getUserID()));
		}
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
	 * listview点击事件接口,用于区分不同view的点击事件
	 * 
	 * @author Alan
	 * 
	 */
	private interface ListItemClickHelp {
		void onClick(View view, int postion, int viewID);
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
			Intent intentPicDetail = new Intent(NewsDetailActivity.this,
					BigImgLookActivity.class);
			intentPicDetail.putExtra(BigImgLookActivity.INTENT_KEY, pathUrl);
			startActivity(intentPicDetail);
		} else if (intentKey
				.equals(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST)) {
			// 传递model列表
			@SuppressWarnings("unchecked")
			List<ImageModel> mdPath = (List<ImageModel>) path;
			Intent intent = new Intent(NewsDetailActivity.this,
					BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			startActivity(intent);
		} else if (intentKey.equals(BigImgLookActivity.INTENT_KEY_IMG_LIST)) {
			// 传递String列表
			@SuppressWarnings("unchecked")
			List<String> mdPath = (List<String>) path;
			Intent intent = new Intent(NewsDetailActivity.this,
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
	private void JumpToHomepage(int userID) {
		Intent intentUsrMain = new Intent(NewsDetailActivity.this,
				OtherPersonalActivity.class);
		intentUsrMain.putExtra(OtherPersonalActivity.INTENT_KEY, userID);
		startActivityWithRight(intentUsrMain);
	}

	@Override
	public void finishWithRight() {
		updateResultData();
		super.finishWithRight();
	}

	// 监听返回事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			this.finishWithRight();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 保存修改的数据，并返回给上一个activity
	 * */
	private void updateResultData() {
		if (NewsOperateModel.OPERATE_UPDATE == operateType) {
			Intent backWithResultIntent = new Intent();
			String isLike = "0";
			int likeCount = 0;
			// 最新的点赞数据
			List<LikeModel> newlkList = new ArrayList<LikeModel>();
			for (int index = 0; index < likeGVAdapter.getCount(); index++) {
				newlkList.add(likeGVAdapter.getItem(index));
				if (likeGVAdapter.getItem(index).getUserID()
						.equals(String.valueOf(userModel.getUid()))) {
					isLike = "1";
				}
				likeCount++;
			}

			// 最新的评论数据
			List<CommentModel> newCmtList = new ArrayList<CommentModel>();
			for (int index = 0; index < detailAdapter.getCount(); index++) {
				if (ItemModel.NEWS_DETAIL_COMMENT == detailAdapter.getItem(
						index).getItemType()) {
					newCmtList.add(((CommentItem) detailAdapter.getItem(index))
							.getCommentModel());
				}
			}

			currentNews.setIsLike(isLike);
			currentNews.setLikeQuantity(String.valueOf(likeCount));
			currentNews.setCommentQuantity(String.valueOf(detailAdapter
					.getCount() - 3));
			currentNews.setLikeHeadListimage(newlkList);
			currentNews.setCommentList(newCmtList);
			backWithResultIntent.putExtra(
					NewsOperateModel.INTENT_KEY_BACK_NEWS_OBJ, currentNews);
			NewsDetailActivity.this.setResult(NewsOperateModel.OPERATE_UPDATE,
					backWithResultIntent);
		}
	}
}
