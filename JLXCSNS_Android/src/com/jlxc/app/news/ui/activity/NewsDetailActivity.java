package com.jlxc.app.news.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.view.KeyboardLayout;
import com.jlxc.app.base.ui.view.KeyboardLayout.onKeyboardsChangeListener;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.DataToItem;
import com.jlxc.app.base.utils.JLXCConst;
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
import com.jlxc.app.news.model.SubCommentModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

public class NewsDetailActivity extends BaseActivityWithTopBar {

	// 评论的类型
	private final static int Input_Type_Comment = 0;
	private final static int Input_Type_SubComment = 1;
	private final static int Input_Type_SubReply = 2;
	// 最多点赞数
	private int MAX_LIKE_COUNT = 10;
	// 主listview
	@ViewInject(R.id.news_detail_listView)
	private PullToRefreshListView newsDetailListView;
	// 评论输入框
	@ViewInject(R.id.edt_comment_input)
	private EditText commentEditText;
	// 评论发送按钮
	@ViewInject(R.id.btn_comment_send)
	private Button btnSendComment;
	// 数据源
	private List<ItemModel> dataList;
	// 主适配器
	private HelloHaAdapter<ItemModel> detailAdapter;
	// 动态的ID
	private String newsID;
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
	// 点赞操作类
	private LikeCancelOperate likeOperate;
	// 点赞头像gridview
	private NoScrollGridView likeGridView;
	// 点击点赞头像监听
	private LikeGridViewItemClick likeItemClickListener;
	// 评论的内容
	private String commentContent = "";
	// 评论操作类
	private CommentOperate commentOperate;
	// 当前的操作的评论对象
	private CommentModel currentCommentModel;
	// 当前的操作的子评论对象
	private SubCommentModel currentSubCmtModel;
	// 评论的类型
	private int commentType = 0;

	@Override
	public int setLayoutId() {
		return R.layout.activity_news_detail;
	}

	@Override
	protected void setUpView() {
		init();
		multiItemTypeSet();
		listViewSet();

		// 点击回复发送按钮
		btnSendComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (commentContent.length() > 0) {
					String tempContent = commentContent;
					// 清空输入内容
					commentEditText.setText("");
					commentEditText.setHint("来条神评论...");
					// 隐藏输入键盘
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(NewsDetailActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

					// 根据评论类型进行评论
					switch (commentType) {
					case Input_Type_Comment:
						CommentModel temMode = new CommentModel();
						temMode.setCommentContent(tempContent);
						temMode.setAddDate(TimeHandle.getCurrentDataStr());
						// 将数据更新至UI
						commentOperate.addCommentRefresh(temMode, false);
						// 将数据更新至服务器
						uploadCommentData(temMode);
						break;

					case Input_Type_SubComment:
						SubCommentModel tempMd = new SubCommentModel();
						tempMd.setCommentContent(tempContent);
						tempMd.setReplyUid(currentCommentModel.getUserId());
						tempMd.setReplyName(currentCommentModel
								.getSubmitterName());
						tempMd.setReplyCommentId(currentCommentModel
								.getCommentID());
						tempMd.setTopCommentId(currentCommentModel
								.getCommentID());
						// 更新UI
						commentOperate.addSubCommentRefresh(tempMd, false);
						// 提交至服务器
						uploadSubCommentData(tempMd);
						break;
					case Input_Type_SubReply:
						// 发布子回复
						SubCommentModel tpMold = new SubCommentModel();
						tpMold.setCommentContent(tempContent);
						tpMold.setReplyUid(currentSubCmtModel.getPublishId());
						tpMold.setReplyName(currentSubCmtModel.getPublishName());
						tpMold.setReplyCommentId(currentSubCmtModel.getSubID());
						tpMold.setTopCommentId(currentSubCmtModel
								.getTopCommentId());
						// 更新UI
						commentOperate.addSubCommentRefresh(tpMold, false);
						// 提交至服务器
						uploadSubCommentData(tpMold);
						break;
					default:
						break;
					}
				}
			}
		});

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
							// 软键盘隐藏时
							if (commentContent.length() <= 0) {
								commentType = Input_Type_Comment;
								commentEditText.setHint("是时候来条神评论了...");
							}
						}
					}
				});
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {
		// userModel = UserManager.getInstance().getUser();
		userModel = new UserModel();
		userModel.setUid(21);
		userModel
				.setHead_sub_image("http://192.168.1.100/jlxc_php/Uploads/2015-07-01/191435720077_sub.png");
		userModel.setUsername("啦啦啦");
		// 获取动态id
		// Intent intent = this.getIntent();
		// Bundle bundle = intent.getExtras();
		// newsID = bundle.getString("News_ID");
		newsID = "72";

		dataList = new ArrayList<ItemModel>();
		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		commentOperate = new CommentOperate();
		likeOperate = new LikeCancelOperate();
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
		bitmapUtils = new BitmapUtils(NewsDetailActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
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
						getNewsDetailData(String.valueOf(userModel.getUid()),
								newsID);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {

						// 上拉
						/*
						 * getNewsDetailData(String.valueOf(userModel.getUid()),
						 * newsID);
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
			likeOperate.lastSeverLikeState = true;
		} else {
			helper.setText(R.id.btn_news_detail_like, "点赞 ");
			likeOperate.lastSeverLikeState = false;
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
		HelloHaAdapter<LikeModel> likeGVAdapter = new HelloHaAdapter<LikeModel>(
				NewsDetailActivity.this,
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
				if (helper.getPosition() < MAX_LIKE_COUNT) {
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
				comment.getSubmitterName());
		helper.setText(R.id.txt_news_detail_comment_content,
				comment.getCommentContent());

		// 设置评论item的点击事件
		LinearLayout replyCmt = helper.getView(R.id.reply_head_layout);
		final int postion = helper.getPosition();
		replyCmt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		});
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
		LinearLayout subCmtLayout = helper.getView(R.id.subcomment_root_view);
		final int postion = helper.getPosition();
		subCmtLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				itemViewClickListener.onClick(view, postion, view.getId());
			}
		});
	}

	/**
	 * 数据处理
	 */
	private void JsonToNewsModel(JSONObject data) {
		NewsModel tempNews = new NewsModel();
		tempNews.setContentWithJson(data);
		// 更新时间戳
		detailAdapter.replaceAll(DataToItem.newsDetailToItems(tempNews));
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
							newsDetailListView.onRefreshComplete();
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
							newsDetailListView.onRefreshComplete();
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						newsDetailListView.onRefreshComplete();
						ToastUtil.show(NewsDetailActivity.this, "网络有毒=_=");
					}

				}, null));
	}

	/**
	 * 发送一级评论
	 * */
	private void uploadCommentData(CommentModel cmtMode) {

		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", String.valueOf(userModel.getUid()));
		params.addBodyParameter("news_id", newsID);
		params.addBodyParameter("comment_content", cmtMode.getCommentContent());

		HttpManager.post(JLXCConst.SEND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							hideLoading();
							// 刷新列表
							JSONObject JResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							CommentModel temMode = new CommentModel();
							temMode.setContentWithJson(JResult);
							// 评论成功后再次刷新
							commentOperate.addCommentRefresh(temMode, true);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							commentOperate.Revoked();
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						commentOperate.Revoked();
						ToastUtil
								.show(NewsDetailActivity.this, "竟然评论失败，请检查网络!");
					}
				}, null));
	}

	/**
	 * 发送二级评论
	 * */
	private void uploadSubCommentData(SubCommentModel subModle) {
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", String.valueOf(userModel.getUid()));
		params.addBodyParameter("news_id", newsID);
		params.addBodyParameter("comment_content", subModle.getCommentContent());
		params.addBodyParameter("reply_uid", subModle.getReplyUid());
		params.addBodyParameter("reply_comment_id",
				subModle.getReplyCommentId());
		params.addBodyParameter("top_comment_id", subModle.getTopCommentId());

		LogUtils.i("**** reply_uid=" + subModle.getReplyUid()
				+ " top_comment_id=" + subModle.getTopCommentId()
				+ " reply_comment_id=" + subModle.getReplyCommentId()
				+ "user_id=" + userModel.getUid() + "news_id=" + newsID
				+ "comment_content=" + subModle.getCommentContent());

		final String replyToName = subModle.getReplyName();
		HttpManager.post(JLXCConst.SEND_SECOND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							// 刷新列表
							JSONObject JResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);

							SubCommentModel tempMd = new SubCommentModel();
							tempMd.setContentWithJson(JResult);
							tempMd.setReplyName(replyToName);
							commentOperate.addSubCommentRefresh(tempMd, true);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							commentOperate.Revoked();
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						commentOperate.Revoked();
						ToastUtil
								.show(NewsDetailActivity.this, "竟然评论失败，请检查网络!");
					}
				}, null));
	}

	/**
	 * 删除一级评论
	 * */
	private void deleteCommentData(String CID, String newsID) {
		RequestParams params = new RequestParams();
		params.addBodyParameter("cid", CID);
		params.addBodyParameter("news_id", newsID);

		HttpManager.post(JLXCConst.DELETE_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							ToastUtil.show(NewsDetailActivity.this, "删除成功");
						}

						if (status == JLXCConst.STATUS_FAIL) {
							commentOperate.Revoked();
							ToastUtil.show(NewsDetailActivity.this, "删除失败");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						commentOperate.Revoked();
						ToastUtil.show(NewsDetailActivity.this, "竟然删除失败，请检查网络");
					}
				}, null));
	}

	/**
	 * 删除二级评论
	 * */
	private void deleteSubCommentData(String CID, String newsID) {
		RequestParams params = new RequestParams();
		params.addBodyParameter("cid", CID);
		params.addBodyParameter("news_id", newsID);

		HttpManager.post(JLXCConst.DELETE_SECOND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							ToastUtil.show(NewsDetailActivity.this, "删除成功");
						}

						if (status == JLXCConst.STATUS_FAIL) {
							commentOperate.Revoked();
							ToastUtil.show(NewsDetailActivity.this, "删除失败");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						commentOperate.Revoked();
						ToastUtil.show(NewsDetailActivity.this, "竟然删除失败，请检查网络");
					}
				}, null));
	}

	/**
	 * view点击事件
	 * */
	public class ItemViewClick implements ListItemClickHelp {

		@Override
		public void onClick(View view, int postion, int viewID) {
			switch (viewID) {
			case R.id.img_news_detail_user_head:
			case R.id.txt_news_detail_user_name:
				TitleItem titleData = (TitleItem) detailAdapter
						.getItem(postion);
				if (R.id.img_news_detail_user_head == viewID) {
					ToastUtil.show(NewsDetailActivity.this, "点击了头像:"
							+ titleData.getUserName());
				} else {
					ToastUtil.show(NewsDetailActivity.this,
							"" + titleData.getUserName());
				}
				break;

			case R.id.iv_news_detail_body_picture:
				BodyItem bodyData = (BodyItem) detailAdapter.getItem(postion);
				String path = bodyData.getNewsImageListList().get(0).getURL();
				// 跳转到图片详情页面
				Intent intent = new Intent(NewsDetailActivity.this,
						BigImgLookActivity.class);
				intent.putExtra("filePath", path);
				startActivity(intent);
				break;

			case R.id.btn_news_detail_like:
				TitleItem operateData = (TitleItem) detailAdapter
						.getItem(postion);
				likeOperate.setOperateData(view, postion);
				if (operateData.getIsLike()) {
					likeOperate.Cancel();
					if (likeOperate.lastSeverLikeState) {
						likeNetOperate(operateData.getNewsID(), "0");
					}
				} else {
					likeOperate.Like();
					if (!likeOperate.lastSeverLikeState) {
						likeNetOperate(operateData.getNewsID(), "1");
					}
				}
				break;

			case R.id.reply_head_layout:
				commentOperate.setPostion(postion);
				// 当前的item数据
				currentCommentModel = ((CommentItem) detailAdapter
						.getItem(postion)).getCommentModel();
				if (currentCommentModel.getUserId().equals(
						String.valueOf(userModel.getUid()))) {
					// 如果是自己发布的评论，则删除评论
					CharSequence[] items = { "删除评论" };
					new AlertDialog.Builder(NewsDetailActivity.this).setItems(
							items, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									commentOperate.deleteCommentRefresh();
									deleteCommentData(
											currentCommentModel.getCommentID(),
											newsID);
								}

							}).show();
				} else {
					// 发布回复别人的评论
					commentEditText.requestFocus();
					commentEditText.setHint("回复："
							+ currentCommentModel.getSubmitterName());
					commentType = Input_Type_SubComment;
					InputMethodManager imm = (InputMethodManager) commentEditText
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
				}

				break;

			// 点击了子评论
			case R.id.subcomment_root_view:
				commentOperate.setPostion(postion);
				// 当前的item数据
				currentSubCmtModel = ((SubCommentItem) detailAdapter
						.getItem(postion)).getSubCommentModel();

				if (currentSubCmtModel.getPublishId().equals(
						String.valueOf(userModel.getUid()))) {
					// 如果是自己发布的评论，则删除评论
					CharSequence[] items = { "删除评论" };
					new AlertDialog.Builder(NewsDetailActivity.this).setItems(
							items, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									commentOperate.deleteSubCommentRefresh();
									deleteSubCommentData(
											currentSubCmtModel.getSubID(),
											newsID);
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
					commentType = Input_Type_SubReply;
					InputMethodManager imm = (InputMethodManager) commentEditText
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
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
			ToastUtil.show(NewsDetailActivity.this,
					"UserID:" + likeUser.getUserID());
		}
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
			Intent intent = new Intent(NewsDetailActivity.this,
					BigImgLookActivity.class);
			intent.putExtra("filePath", currentImgPath);
			startActivity(intent);
		}
	}

	/***
	 * 点赞操作网络请求
	 */
	private void likeNetOperate(String newsId, String likeOrCancel) {
		if (!likeOperate.isLikeUpload) {
			likeOperate.isLikeUpload = true;
			if (likeOrCancel.equals("1")) {
				likeOperate.lastSeverLikeState = true;
			} else {
				likeOperate.lastSeverLikeState = false;
			}
			// 参数设置
			RequestParams params = new RequestParams();
			params.addBodyParameter("news_id", newsId);
			params.addBodyParameter("isLike", likeOrCancel);
			params.addBodyParameter("user_id",
					String.valueOf(userModel.getUid()));
			params.addBodyParameter("is_second", "0");

			HttpManager.post(JLXCConst.LIKE_OR_CANCEL, params,
					new JsonRequestCallBack<String>(
							new LoadDataHandler<String>() {

								@Override
								public void onSuccess(JSONObject jsonResponse,
										String flag) {
									super.onSuccess(jsonResponse, flag);
									int status = jsonResponse
											.getInteger(JLXCConst.HTTP_STATUS);
									if (status == JLXCConst.STATUS_SUCCESS) {
										likeOperate.isLikeUpload = false;
									}

									if (status == JLXCConst.STATUS_FAIL) {
										// 失败则取消操作
										likeOperate.Revoked();
										ToastUtil
												.show(NewsDetailActivity.this,
														jsonResponse
																.getString(JLXCConst.HTTP_MESSAGE));
										likeOperate.lastSeverLikeState = !likeOperate.lastSeverLikeState;
										likeOperate.isLikeUpload = false;
									}
								}

								@Override
								public void onFailure(HttpException arg0,
										String arg1, String flag) {
									super.onFailure(arg0, arg1, flag);
									// 失败则取消操作
									likeOperate.Revoked();
									ToastUtil.show(NewsDetailActivity.this,
											"卧槽，竟然操作失败，检查下网络");
									likeOperate.lastSeverLikeState = !likeOperate.lastSeverLikeState;
									likeOperate.isLikeUpload = false;
								}
							}, null));
		}
	}

	/**
	 * 评论操作
	 * 
	 * @author luis
	 */
	private class CommentOperate {

		// 评论的操作类型
		private final static int Add_Comment = 0;
		private final static int Delete_Comment = 1;
		private final static int Add_Sub_Comment = 2;
		private final static int Delete_Sub_Comment = 3;
		// 操作的位置
		private int oprtPostion;
		// 第一条可见的位置
		private int firstVisiblePostion;
		// 操作的item的值
		private ItemModel oprtItem;
		// 操作的类型
		private int LastOperateType = -1;
		// 当前操作的item离顶部的距离
		private int opertItemToTop = 0;

		public void setPostion(int postion) {
			oprtPostion = postion;
		}

		/**
		 * 更新一级评论列表
		 * */
		public void addCommentRefresh(CommentModel cmtModel,
				boolean isNetFeedback) {
			// 自动显示最后一条
			newsDetailListView.getRefreshableView().setTranscriptMode(
					ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			if (isNetFeedback) {
				// 是不是网络回调
				detailAdapter.remove(oprtItem);
				newsDetailListView.getRefreshableView().setTranscriptMode(
						ListView.TRANSCRIPT_MODE_DISABLED);
			}
			LastOperateType = Add_Comment;

			// 新建一条评论对象
			cmtModel.setSubmitterName(userModel.getUsername());
			cmtModel.setHeadImage(userModel.getHead_image());
			cmtModel.setHeadSubImage(userModel.getHead_sub_image());
			List<SubCommentModel> subCmtList = new ArrayList<SubCommentModel>();
			cmtModel.setSubCommentList(subCmtList);
			oprtItem = DataToItem.createComment(cmtModel);
			detailAdapter.add(oprtItem);
		}

		/**
		 * 删除一级评论刷新
		 * */
		public void deleteCommentRefresh() {
			LastOperateType = Delete_Comment;
			oprtItem = (CommentItem) detailAdapter.getItem(oprtPostion);
			// 保存当前第一个可见的item的索引和偏移量
			firstVisiblePostion = newsDetailListView.getRefreshableView()
					.getFirstVisiblePosition();
			View view = newsDetailListView.getChildAt(0);
			opertItemToTop = (view == null) ? 0 : view.getTop();
			detailAdapter.remove(oprtPostion);
		}

		/**
		 * 添加二级评论刷新
		 * 
		 * @param replyCommentId
		 *            :被回复的评论ID
		 * @param topCommentId
		 *            ：顶部的评论ID
		 * @param replyUid
		 *            ：被回复者的ID
		 * @param userId
		 *            :回复者的ID
		 * */
		private void addSubCommentRefresh(SubCommentModel subModel,
				boolean isNetFeedback) {
			LastOperateType = Add_Sub_Comment;

			// 找到需要插入的位置
			int index = oprtPostion + 1;
			while (index < detailAdapter.getCount()) {
				if (ItemModel.NEWS_DETAIL_COMMENT == detailAdapter.getItem(
						index).getItemType()) {
					break;
				}
				index++;
			}
			// 设置发布的人的信息
			subModel.setPublishName(userModel.getUsername());
			subModel.setPublishId(String.valueOf(userModel.getUid()));
			if (isNetFeedback) {
				detailAdapter.remove(oprtItem);
				// 中间多隔了一条数据
				index--;
			}
			// 滚动到添加的评论处
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				newsDetailListView.getRefreshableView().smoothScrollToPosition(
						index);
			} else {
				newsDetailListView.getRefreshableView().setSelection(index);
			}

			oprtItem = DataToItem.createSubComment(subModel, newsID);
			detailAdapter.insert(index, oprtItem);
		}

		/**
		 * 删除二级评论刷新
		 * */
		private void deleteSubCommentRefresh() {
			LastOperateType = Delete_Sub_Comment;
			oprtItem = (SubCommentItem) detailAdapter.getItem(oprtPostion);
			// 保存当前第一个可见的item的索引和偏移量
			firstVisiblePostion = newsDetailListView.getRefreshableView()
					.getFirstVisiblePosition();
			View view = newsDetailListView.getChildAt(0);
			opertItemToTop = (view == null) ? 0 : view.getTop();
			detailAdapter.remove(oprtPostion);
		}

		/**
		 * 撤销上次操作
		 * */
		public void Revoked() {
			switch (LastOperateType) {
			case Add_Comment:
			case Add_Sub_Comment:
				detailAdapter.remove(oprtItem);
				break;

			case Delete_Comment:
			case Delete_Sub_Comment:
				detailAdapter.insert(oprtPostion, oprtItem);
				// 恢复上次的位置
				newsDetailListView.getRefreshableView().setSelectionFromTop(
						firstVisiblePostion, opertItemToTop);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 点赞或取消
	 * 
	 * @author luis
	 */
	private class LikeCancelOperate {
		// 是否正在上传点赞数据
		public boolean isLikeUpload = false;
		// 上次点赞的状态
		public boolean lastSeverLikeState = false;
		// 点赞的控件
		private View view;
		// 点赞的位置
		private int postion;
		// 记录上传的操作类型
		private boolean isLikeOperate = false;

		// 设置操作的数据
		public void setOperateData(View view, int postion) {
			this.view = view;
			this.postion = postion;
		}

		/**
		 * 点赞操作函数
		 * */
		@SuppressWarnings("unchecked")
		public void Like() {
			isLikeOperate = true;
			TitleItem likeData = (TitleItem) detailAdapter.getItem(postion);
			((Button) view).setText("已赞");
			likeData.setIsLike("1");

			LikeModel myModel = new LikeModel();
			myModel.setUserID(String.valueOf(userModel.getUid()));
			myModel.setHeadImage(userModel.getHead_image());
			myModel.setHeadSubImage(userModel.getHead_sub_image());
			((HelloHaAdapter<LikeModel>) likeGridView.getAdapter())
					.addToFirst(myModel);
		}

		/**
		 * 取消点赞
		 * */
		@SuppressWarnings("unchecked")
		public void Cancel() {
			isLikeOperate = false;
			TitleItem likeData = (TitleItem) detailAdapter.getItem(postion);
			likeData.setIsLike("0");
			((Button) view).setText("点赞");

			HelloHaAdapter<LikeModel> lkAdapter = ((HelloHaAdapter<LikeModel>) likeGridView
					.getAdapter());
			// 移除头像
			for (int index = 0; index < lkAdapter.getCount(); ++index) {
				if (lkAdapter.getItem(index).getUserID()
						.equals(String.valueOf(userModel.getUid()))) {
					lkAdapter.remove(index);
					break;
				} else {
					LogUtils.e("点赞数据发生了错误.");
				}
			}
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
