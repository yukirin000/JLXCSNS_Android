package com.jlxc.app.news.ui.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.ui.view.NoScrollGridView;
import com.jlxc.app.base.ui.view.NoScrollGridView.OnTouchInvalidPositionListener;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.news.model.ImageModel;
import com.jlxc.app.news.ui.fragment.NewsListFragment.NewsBitmapLoadCallBack;
import com.jlxc.app.news.ui.view.LikeImageListView.EventCallBack;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MultiImageView extends RelativeLayout {

	// 根布局
	private RelativeLayout rootView;
	// 多图
	private List<ImageView> imageViewsList = new ArrayList<ImageView>();
	// 单图
	private ImageView singleImageView;
	//
	private Context mContext;
	// 屏幕宽度
	private static int screenWidth = 0;
	// 图片源
	private List<ImageModel> dataList = new ArrayList<ImageModel>();
	// 点击事件回调接口
	private JumpCallBack jumpInterface;
	// bitmap
	private BitmapUtils bitmapUtils;

	public MultiImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MultiImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
		getWidget();
	}

	/**
	 * 初始化
	 * */
	private void init() {
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;

		bitmapUtils = BitmapManager.getInstance().getBitmapUtils(mContext,
				true, true);

		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
	}

	/**
	 * 获取控件
	 * */
	private void getWidget() {
		View view = View.inflate(mContext, R.layout.custom_multi_image_view,
				this);
		rootView = (RelativeLayout) view
				.findViewById(R.id.layout_multi_pic_root_view);
		singleImageView = (ImageView) view.findViewById(R.id.iv_custom_big_pic);
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_A));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_B));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_C));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_D));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_E));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_F));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_G));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_H));
		imageViewsList.add((ImageView) view
				.findViewById(R.id.iv_custom_picture_I));
		setClickListener();
	}

	/**
	 * 事件监听
	 * */
	private void setClickListener() {
		singleImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				jumpToBigImage(BigImgLookActivity.INTENT_KEY, dataList.get(0)
						.getURL(), 0);
			}
		});
		// 图片点击事件
		for (int index = 0; index < imageViewsList.size(); index++) {
			imageViewsList.get(index).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					for (int postion = 0; postion < imageViewsList.size(); postion++) {
						if (view.getId() == imageViewsList.get(postion).getId()) {
							jumpToBigImage(
									BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
									dataList, postion);
							break;
						}
					}
				}
			});
		}
	}

	/**
	 * 绑定数据
	 */
	public void imageDataSet(final List<ImageModel> pictureList) {
		dataList = pictureList;
		if (pictureList.size() == 0) {
			// 没有图片的情况
			rootView.setVisibility(View.GONE);
		} else if (pictureList.size() == 1) {
			// 只有一张图片的情况
			rootView.setVisibility(View.VISIBLE);
			singleImageView.setVisibility(View.VISIBLE);
			ImageModel imageModel = pictureList.get(0);

			// 图片尺寸
			int imagHeight = 100;
			int imageWidth = 100;
			if (imageModel.getImageHheight() >= imageModel.getImageWidth()) {
				imagHeight = screenWidth * 3 / 5;
				imageWidth = (int) ((imageModel.getImageWidth() * screenWidth * 3) / (5.0 * imageModel
						.getImageHheight()));
			} else {
				imagHeight = (int) ((imageModel.getImageHheight() * screenWidth * 3) / (5.0 * imageModel
						.getImageWidth()));
				imageWidth = screenWidth * 3 / 5;
			}
			singleImageView.setLayoutParams(new LayoutParams(imageWidth,
					imagHeight));
			bitmapUtils.display(singleImageView, pictureList.get(0).getURL(),
					new NewsBitmapLoadCallBack());
			// 隐藏其余的imageview
			for (int index = 1; index < imageViewsList.size(); index++) {
				imageViewsList.get(index).setVisibility(View.GONE);
			}
		} else {
			rootView.setVisibility(View.VISIBLE);
			singleImageView.setVisibility(View.GONE);
			for (int index = 0; index < imageViewsList.size(); index++) {
				if (index < pictureList.size()) {
					imageViewsList.get(index).setVisibility(View.VISIBLE);
					bitmapUtils.display(imageViewsList.get(index), pictureList
							.get(index).getURL(), new NewsBitmapLoadCallBack());
				} else {
					imageViewsList.get(index).setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * 设置跳转回调
	 * 
	 * @param callInterface
	 */
	public void setJumpListener(JumpCallBack callInterface) {
		this.jumpInterface = callInterface;
	}

	/**
	 * 跳转查看大图
	 */
	private void jumpToBigImage(String intentKey, Object path, int index) {
		if (intentKey.equals(BigImgLookActivity.INTENT_KEY)) {
			// 单张图片跳转
			String pathUrl = (String) path;
			Intent intentPicDetail = new Intent(mContext,
					BigImgLookActivity.class);
			intentPicDetail.putExtra(BigImgLookActivity.INTENT_KEY, pathUrl);
			jumpInterface.onImageClick(intentPicDetail);
		} else if (intentKey
				.equals(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST)) {
			// 传递model列表
			@SuppressWarnings("unchecked")
			List<ImageModel> mdPath = (List<ImageModel>) path;
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_MODEl_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			jumpInterface.onImageClick(intent);
		} else if (intentKey.equals(BigImgLookActivity.INTENT_KEY_IMG_LIST)) {
			// 传递String列表
			@SuppressWarnings("unchecked")
			List<String> mdPath = (List<String>) path;
			Intent intent = new Intent(mContext, BigImgLookActivity.class);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_IMG_LIST,
					(Serializable) mdPath);
			intent.putExtra(BigImgLookActivity.INTENT_KEY_INDEX, index);
			jumpInterface.onImageClick(intent);
		} else {
			LogUtils.e("未传递图片地址");
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
	 * 点击事件回调接口
	 * */
	public interface JumpCallBack {
		public void onImageClick(Intent intentToimageoBig);
	}
}
