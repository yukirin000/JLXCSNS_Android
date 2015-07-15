package com.jlxc.app.news.ui.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.OperatedClientConnection;

import android.R.integer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.activity.MainTabActivity;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.DataToItem;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.login.ui.activity.RegisterActivity;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.model.ItemModel;
import com.jlxc.app.news.model.ItemModel.BodyItem;
import com.jlxc.app.news.model.ItemModel.CommentItem;
import com.jlxc.app.news.model.ItemModel.LikeListItem;
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
	// 点击二级评论监听
	private SubCmtListViewItemClick subCmtItemClickListener;
	// 评论的内容
	private String commentContent;
	// 评论操作类
	private CommentOperate commentOperate;

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
					commentOperate.addCommentRefresh("",
							TimeHandle.getCurrentDataStr(),
							String.valueOf(userModel.getUid()), tempContent,
							"0", false);
					uploadCommentData(String.valueOf(userModel.getUid()),
							newsID, tempContent);
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
		newsID = "63";

		dataList = new ArrayList<ItemModel>();
		itemViewClickListener = new ItemViewClick();
		imageItemClickListener = new ImageGridViewItemClick();
		likeItemClickListener = new LikeGridViewItemClick();
		subCmtItemClickListener = new SubCmtListViewItemClick();
		commentOperate = new CommentOperate();
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

				default:
					LogUtils.i("helper.layoutId=" + helper.layoutId);
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
		List<SubCommentModel> subcmtList = comment.getSubCommentList();
		// 回复评论layout
		LinearLayout replyCmt = helper.getView(R.id.reply_head_layout);
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
		// 设置子评论列表
		HelloHaAdapter<SubCommentModel> subCommentListAdapter = new HelloHaAdapter<SubCommentModel>(
				NewsDetailActivity.this,
				R.layout.news_detail_subcomment_listview_item, subcmtList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					SubCommentModel item) {
				helper.setText(R.id.txt_sub_comment_by_name,
						item.getCommentName());
				helper.setText(R.id.txt_by_sub_comment_name,
						item.getReplyName());
				helper.setText(R.id.txt_sub_comment_content,
						item.getCommentContent());
			}
		};
		ListView subCommitListView = (ListView) helper
				.getView(R.id.listview_news_detail_sub_comment);
		subCommitListView.setAdapter(subCommentListAdapter);
		subCommitListView.setOnItemClickListener(subCmtItemClickListener);

		// 设置点击事件
		final int postion = helper.getPosition();
		replyCmt.setOnClickListener(new OnClickListener() {

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
		LogUtils.i("数据接口是：" + path);
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
	private void uploadCommentData(String userId, String NewsID,
			String commentContent) {
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", userId);
		params.addBodyParameter("news_id", NewsID);
		params.addBodyParameter("comment_content", commentContent);

		LogUtils.i("input commentContent=" + commentContent);
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
							String commentID = JResult.getString("id");
							String addData = JResult.getString("add_date");
							String Uid = JResult.getString("user_id");
							String contentStr = JResult
									.getString("comment_content");
							String lkQuantity = JResult
									.getString("like_quantity");

							// 评论成功后再次刷新
							commentOperate.addCommentRefresh(commentID,
									addData, Uid, contentStr, lkQuantity, true);
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
	private void sendSubCommentData(String userId, String newsId,
			String commentContent, String replyUID, String replyCommentId,
			String topCommentID) {

		showLoading("评论中^_^", false);
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", userId);
		params.addBodyParameter("news_id", "");
		params.addBodyParameter("comment_content", commentContent);

		LogUtils.i("----uid=" + userId + " news_id=" + "" + " comment_content="
				+ commentContent);

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
						}

						if (status == JLXCConst.STATUS_FAIL) {
							hideLoading();
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
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

		LogUtils.i("CID=" + CID);
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
				likeOperate = new LikeCancelOperate(view, postion);
				if (operateData.getIsLike()) {
					likeOperate.Cancel();
					likeNetOperate(operateData.getNewsID(), "0");
				} else {
					likeOperate.Like();
					likeNetOperate(operateData.getNewsID(), "1");
				}
				break;

			case R.id.reply_head_layout:
				final int location = postion;
				// 当前一条的item数据
				final CommentModel cmtModel = ((CommentItem) detailAdapter
						.getItem(postion)).getCommentModel();
				if (cmtModel.getUserId().equals(
						String.valueOf(userModel.getUid()))) {
					// 删除评论
					CharSequence[] items = { "删除评论" };
					new AlertDialog.Builder(NewsDetailActivity.this).setItems(
							items, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									commentOperate.setPostion(location);
									commentOperate.deleteCommentRefresh();
									deleteCommentData(cmtModel.getCommentID(),
											newsID);
								}

							}).show();
				} else {
				}

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 二级评论点击监听
	 */
	public class SubCmtListViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			SubCommentModel subCmtUser = (SubCommentModel) parent.getAdapter()
					.getItem(position);
			ToastUtil.show(NewsDetailActivity.this,
					"UserID:" + subCmtUser.getSubID());
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
							ToastUtil.show(NewsDetailActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						// 失败则取消操作
						likeOperate.Revoked();
						ToastUtil.show(NewsDetailActivity.this,
								"卧槽，竟然操作失败，检查下网络");
					}

				}, null));

	}

	/**
	 * listview滑动时获取listview的游标
	 * */
	private int getPositionInNewCursor(int newCursorCount, int firstVisiblePos,
			ListView listView) {
		if (firstVisiblePos == 0) {
			firstVisiblePos += 1;
		}

		int headerCount = listView.getHeaderViewsCount();
		int newCursorPos = newCursorCount - listView.getAdapter().getCount()
				+ firstVisiblePos - headerCount;

		return newCursorPos;
	}

	/***
	 * 获取偏移量
	 */
	private int getOffsetY(Cursor cursor, int firstVisiblePos,
			int newCursorPosition,ListView listView) {
		int y;

		View firstVisibleItem = null;
		if (firstVisiblePos == 0) {
			firstVisibleItem = listView.getChildAt(1);
		} else {
			firstVisibleItem = listView.getChildAt(0);
		}
		y = firstVisibleItem.getTop();

		View timeView = firstVisibleItem.findViewById(R.id.time_text_view);
		if (timeView != null && timeView.getVisibility() == View.VISIBLE) {

			Cursor curItem = (Cursor) listView.getAdapter().getItem(newCursorPosition);
			Cursor preItem = (Cursor) listView.getAdapter().getItem(newCursorPosition - 1);
			// if (curItem != null || preItem != null) {
			// long curTimeStamp = curItem
			// .getLong(MessagesProjection.JEDI_CREATE_DATE_INDX);
			// long preTimeStamp = preItem
			// .getLong(MessagesProjection.JEDI_CREATE_DATE_INDX);
			//
			// if (Math.abs(curTimeStamp - preTimeStamp) <=
			// SHOW_TIME_STAMP_TEN_MINS) {
			// LayoutParams param = (LinearLayout.LayoutParams) mTimeView
			// .getLayoutParams();
			// y += mTimeView.getHeight() + param.topMargin
			// + param.bottomMargin;
			// }
			// }
		}

		return y;
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
		// 操作的item的值
		private ItemModel oprtItem;
		// 操作的类型
		private int oprtType = 0;
		// 当前操作的item离顶部的距离
		private int opertItemToTop = 0;

		public void setPostion(int postion) {
			oprtPostion = postion;
		}

		/**
		 * 更新一级评论列表
		 * */
		public void addCommentRefresh(String commentID, String addData,
				String Uid, String content, String lkQuantity,
				boolean isNetFeedback) {
			// 自动显示最后一条
			newsDetailListView.getRefreshableView().setTranscriptMode(
					ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			if (isNetFeedback) {
				detailAdapter.remove(oprtItem);
				newsDetailListView.getRefreshableView().setTranscriptMode(
						ListView.TRANSCRIPT_MODE_DISABLED);
			}
			oprtType = Add_Comment;

			// 新建一条评论对象
			CommentModel newCmtModel = new CommentModel();
			newCmtModel.setCommentID(commentID);
			newCmtModel.setSubmitterName(userModel.getUsername());
			newCmtModel.setHeadImage(userModel.getHead_image());
			newCmtModel.setHeadSubImage(userModel.getHead_sub_image());
			newCmtModel.setAddDate(addData);
			newCmtModel.setUserId(Uid);
			newCmtModel.setCommentContent(content);
			newCmtModel.setLikeQuantity(lkQuantity);
			List<SubCommentModel> subCmtList = new ArrayList<SubCommentModel>();
			newCmtModel.setSubCommentList(subCmtList);
			oprtItem = DataToItem.createComment(newCmtModel);
			detailAdapter.add(oprtItem);
			// 清空输入内容
			commentEditText.setText("");
			commentEditText.setHint("来条神评论...");
			// 隐藏输入键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(NewsDetailActivity.this
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}

		/**
		 * 删除一级评论刷新
		 * */
		public void deleteCommentRefresh() {
			oprtType = Delete_Comment;
			oprtItem = (CommentItem) detailAdapter.getItem(oprtPostion);
			int firstVisible = newsDetailListView.getRefreshableView()
					.getFirstVisiblePosition();
			opertItemToTop = newsDetailListView.getChildAt(
					oprtPostion - firstVisible).getTop();
			detailAdapter.remove(oprtPostion);
		}

		/**
		 * 添加二级评论刷新
		 * */
		private void addSubCommentRefresh(String uid, String newsId) {

		}

		/**
		 * 删除二级评论刷新
		 * */
		private void deleteSubCommentRefresh(String uid, String newsId) {

		}

		/**
		 * 撤销上次操作
		 * */
		public void Revoked() {
			switch (oprtType) {
			case Add_Comment:
				detailAdapter.remove(oprtItem);
				break;
			case Delete_Comment:
				detailAdapter.insert(oprtPostion, oprtItem);
				newsDetailListView.getRefreshableView().setSelectionFromTop(
						oprtPostion, opertItemToTop);
				break;
			case Add_Sub_Comment:
				break;
			case Delete_Sub_Comment:
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
		private View view;
		private int postion;
		private boolean isLikeOperate = false;

		public LikeCancelOperate(View view, int postion) {
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
