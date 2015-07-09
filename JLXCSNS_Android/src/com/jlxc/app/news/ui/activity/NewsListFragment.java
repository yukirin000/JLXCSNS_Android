package com.jlxc.app.news.ui.activity;

import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.adapter.MultiItemTypeSupport;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.model.ImageModel;
import com.jlxc.app.base.model.LikeModel;
import com.jlxc.app.base.model.NewsItemModel;
import com.jlxc.app.base.model.NewsItemModel.BodyItem;
import com.jlxc.app.base.model.NewsItemModel.LikeListItem;
import com.jlxc.app.base.model.NewsItemModel.OperateItem;
import com.jlxc.app.base.model.NewsItemModel.ReplyItem;
import com.jlxc.app.base.model.NewsItemModel.TitleItem;
import com.jlxc.app.base.model.NewsModel;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.utils.NewsToItem;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;

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

public class NewsListFragment extends Fragment {

	// 动态listview
	@ViewInject(R.id.news_listview)
	private PullToRefreshListView newsListView;
	// 原始数据源
	private List<NewsModel> listItem = null;
	// item数据源
	private List<NewsItemModel> listItemData = null;
	// 适配器
	private HelloHaAdapter<NewsItemModel> mAdapter = null;
	// 使支持多种item
	private MultiItemTypeSupport<NewsItemModel> multiItemTypeSupport = null;
	// 上下文信息
	private Context mContext;
	//
	public static BitmapUtils bitmapUtils;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		mContext = getActivity().getApplicationContext();
		View rootView = inflater.inflate(R.layout.fragment_news_list,
				container, false);
		initBitmapUtils();
		setView();

		return rootView;
	}

	/**
	 * 数据的初始化
	 * */
	private void init() {

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
	 * listview的设置
	 * */
	private void setView() {
		// 设置刷新模式
		newsListView.setMode(Mode.BOTH);
		// 数据转换
		listItemData = NewsToItem.newsToItems(listItem);
		newsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
			}
		});

		//
		mAdapter = new HelloHaAdapter<NewsItemModel>(mContext, listItemData,
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
		mAdapter.setItemsClickEnable(false);
		actualListView.setAdapter(mAdapter);
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
		// =========================动态正文中的九宫格图片======================
		HelloHaAdapter<ImageModel> newsGVAdapter = new HelloHaAdapter<ImageModel>(
				mContext, R.layout.news_body_gridview_item_layout, pictureList) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					ImageModel item) {
				// 设置图片
				helper.setImageUrl(R.id.iv_body_gridview_item, bitmapUtils,
						item.getSubURL(), new NewsBitmapLoadCallBack());
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
		// 所有点赞的头像
		List<LikeModel> lkImageList = (List<LikeModel>) lkData
				.getLikeHeadListimage();

		// =========================点赞头像=============================//
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

		/*
		 * helper.setText(R.id.txt_comment_nameA, replyData.getReplyList()
		 * .get(0).getSubmitterName());
		 * helper.setText(R.id.txt_comment_contentA, replyData
		 * .getReplyList().get(0).getCommentContent());
		 * 
		 * helper.setText(R.id.txt_comment_nameB, replyData.getReplyList()
		 * .get(1).getSubmitterName());
		 * helper.setText(R.id.txt_comment_contentB, replyData
		 * .getReplyList().get(1).getCommentContent());
		 * 
		 * helper.setText(R.id.txt_comment_nameC, replyData.getReplyList()
		 * .get(2).getSubmitterName());
		 * helper.setText(R.id.txt_comment_contentC, replyData
		 * .getReplyList().get(2).getCommentContent());
		 */
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
		transitionDrawable.startTransition(500);
	}
}
