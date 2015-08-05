package com.jlxc.app.news.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.view.RoundImageView;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.news.model.LikeModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;

public class LikeImageListView extends LinearLayout {

	// 小分辨率手机
	private final static int SMALL_PIX = 540;
	// 中等分辨率
	private final static int MIDDLE_PIX = 720;
	// 大分辨率
	private final static int LAGER_PIX = 900;
	//
	private Context mContext;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// 根布局
	private RelativeLayout rootView;
	// 赞的图片
	private List<RoundImageView> likeImageViews = new ArrayList<RoundImageView>();
	// 其余点赞 的人
	private TextView allLikeView;
	// 最多点赞数默认为8
	private int maxLikeCount = 8;
	// 所有点赞数
	private int allLikeCount = 0;
	// 点赞数据集
	private List<LikeModel> likeList;
	// bitmap
	private BitmapUtils bitmapUtils;
	// 动态的id
	private String newsId;
	// 点击事件回调接口
	private EventCallBack callInterface;

	public LikeImageListView(Context context) {
		super(context);
	}

	public LikeImageListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		getWidget();
		controlSizeInit();
	}

	/**
	 * 获取控件
	 * */
	private void getWidget() {
		View view = View.inflate(mContext, R.layout.custom_like_list_layout,
				this);
		rootView = (RelativeLayout) view
				.findViewById(R.id.layout_like_list_root_view);
		allLikeView = (TextView) view
				.findViewById(R.id.tv_custom_like_all_person);
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_A));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_B));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_C));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_D));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_E));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_F));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_G));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_H));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_I));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_J));
		likeImageViews.add((RoundImageView) view
				.findViewById(R.id.iv_like_head_img_K));
		setWidgetListener();
	}

	/**
	 * 事件监听
	 * */
	private void setWidgetListener() {
		for (int index = 0; index < likeImageViews.size(); index++) {
			likeImageViews.get(index).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					for (int index = 0; index < likeImageViews.size(); index++) {
						if (view.getId() == likeImageViews.get(index).getId()) {
							callInterface.onItemClick(JLXCUtils
									.stringToInt(likeList.get(index)
											.getUserID()));
							break;
						}
					}
				}
			});
		}
		allLikeView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callInterface.onAllPersonBtnClick(newsId);
			}
		});

	}

	/**
	 * 设置操作回调
	 * 
	 * @param callInterface
	 */
	public void setEventListener(EventCallBack callInterface) {
		this.callInterface = callInterface;
	}

	/**
	 * 计算尺寸
	 * */
	private void controlSizeInit() {
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;

		// 计算显示的头像个数
		if (screenWidth <= SMALL_PIX) {
			maxLikeCount = 8;
		} else if (screenWidth > LAGER_PIX) {
			maxLikeCount = 9;
		}
	}

	/**
	 * 初始化
	 * */
	public void dataInit(int allCount, BitmapUtils bmapUtils, String nId) {
		allLikeCount = allCount;
		bitmapUtils = bmapUtils;
		newsId = nId;
	}

	/**
	 * 数据绑定
	 * */
	public void listDataBindSet(List<LikeModel> imageList) {
		likeList = imageList;
		listRefresh();
	}

	/**
	 * 数据绑定
	 * */
	private void listRefresh() {
		if (likeList.size() == 0) {
			rootView.setVisibility(View.GONE);
		} else {
			rootView.setVisibility(View.VISIBLE);
		}
		if (likeList.size() < maxLikeCount) {
			allLikeView.setVisibility(View.INVISIBLE);
		} else {
			allLikeView.setVisibility(View.VISIBLE);
			allLikeView.setText(String.valueOf(allLikeCount));
		}

		for (int index = 0; index < likeImageViews.size(); index++) {
			RoundImageView tpImageView = likeImageViews.get(index);
			if (index < likeList.size() && index < maxLikeCount) {
				if (tpImageView.getVisibility() == View.GONE) {
					tpImageView.setVisibility(View.VISIBLE);
				}
				bitmapUtils.display(likeImageViews.get(index),
						likeList.get(index).getHeadSubImage(),
						new NewsBitmapLoadCallBack());
			} else {
				tpImageView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 插入到首部
	 * */
	public void insertToFirst(LikeModel newData) {
		if (!likeList.contains(newData)) {
			likeList.add(0, newData);
			allLikeCount++;
			listRefresh();
		} else {
			LogUtils.e("我的点赞数据已经存在");
		}
	}

	//
	public void removeHeadImg() {
		for (int index = 0; index < likeList.size(); index++) {
			if (JLXCUtils.stringToInt(likeList.get(index).getUserID()) == UserManager
					.getInstance().getUser().getUid()) {
				likeList.remove(index);
				allLikeCount--;
				break;
			} else if (index >= likeList.size()) {
				LogUtils.e("点赞发生了错误，未找到数据.");
			}
		}
		listRefresh();
	}

	/**
	 * 加载图片时的回调函数
	 * */
	private class NewsBitmapLoadCallBack extends
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
	 * 点击事件回调接口
	 * */
	public interface EventCallBack {
		public void onItemClick(int userId);

		public void onAllPersonBtnClick(String newsId);
	}
}
