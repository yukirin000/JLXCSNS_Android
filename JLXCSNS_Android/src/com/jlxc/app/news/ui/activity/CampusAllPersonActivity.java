package com.jlxc.app.news.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CampusPersonModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

public class CampusAllPersonActivity extends BaseActivityWithTopBar {

	// 学校的人gridview
	@ViewInject(R.id.gv_school_all_person)
	private GridView personListGridView;
	// 筛选按钮
	private TextView filterBtn;
	// 学生数据
	private List<CampusPersonModel> personList;
	// 适配器
	private HelloHaAdapter<CampusPersonModel> personAdapter;
	// 学校代码
	private String schoolCode;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;

	@Override
	public int setLayoutId() {
		return R.layout.activity_school_all_person_layout;
	}

	@Override
	protected void setUpView() {
		init();
		GridViewSetup();
		setRightBtn();
		// 查询数据
		getCampusAllPerson(schoolCode);
	}

	/**
	 * 筛选按钮
	 * */
	private void setRightBtn() {
		CharSequence[] items = { "只看帅哥", "只看美女", "全部都看" };
		final Builder filterDialog = new AlertDialog.Builder(
				CampusAllPersonActivity.this).setItems(items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						// 只看男生
						case 0:
							List<CampusPersonModel> boyList = new ArrayList<CampusPersonModel>();
							for (CampusPersonModel personModel : personList) {
								if (personModel.getSex().equals("0")) {
									boyList.add(personModel);
								}
							}
							personAdapter.replaceAll(boyList);
							break;
						// 只看女生
						case 1:
							List<CampusPersonModel> girlList = new ArrayList<CampusPersonModel>();
							for (CampusPersonModel personModel : personList) {
								if (personModel.getSex().equals("1")) {
									girlList.add(personModel);
								}
							}
							personAdapter.replaceAll(girlList);
							break;
						// 全部都看
						case 2:
							personAdapter.replaceAll(personList);
							break;

						default:
							break;
						}
					}
				});

		filterBtn = this.addRightBtn("筛选");
		filterBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				filterDialog.show();
			}
		});
	}

	/**
	 * 初始化
	 * */
	private void init() {
		initBitmapUtils();
		// 获取学校代码
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		schoolCode = bundle.getString("School_Code");
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
		bitmapUtils = new BitmapUtils(CampusAllPersonActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/***
	 * gridview的初始化
	 */
	private void GridViewSetup() {
		personAdapter = new HelloHaAdapter<CampusPersonModel>(
				CampusAllPersonActivity.this,
				R.layout.campus_person_gridview_item_layout, personList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					CampusPersonModel item) {
				ImageView imgView = helper.getView(R.id.iv_campus_person_head);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 5;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定头像图片
				bitmapUtils.configDefaultBitmapMaxSize(laParams.width,
						laParams.width);
				helper.setImageUrl(R.id.iv_campus_person_head, bitmapUtils,
						item.getHeadSubImage(), new NewsBitmapLoadCallBack());

				// 绑定昵称
				helper.setText(R.id.txt_campus_person_name, item.getUserName());
			}
		};

		personListGridView.setAdapter(personAdapter);
		PersonGridViewItemClick personItemClickListener = new PersonGridViewItemClick();
		personListGridView.setOnItemClickListener(personItemClickListener);
	}

	/**
	 * 数据解析
	 * */
	private void JsonToPersonData(List<JSONObject> JPersonList) {
		List<CampusPersonModel> dataList = new ArrayList<CampusPersonModel>();
		// 解析校园的人
		for (JSONObject personObj : JPersonList) {
			CampusPersonModel tempPerson = new CampusPersonModel();
			tempPerson.setContentWithJson(personObj);
			dataList.add(tempPerson);
		}
		personList = dataList;
		personAdapter.replaceAll(personList);
		if (null != JPersonList) {
			JPersonList.clear();
		}
	}

	/**
	 * 获取学校所有学生信息
	 * */
	private void getCampusAllPerson(String schoolCode) {
		String path = JLXCConst.GET_SCHOOL_STUDENT_LIST + "?" + "&school_code="
				+ schoolCode;

		LogUtils.i("path=" + path);
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
							// 获取数据列表
							List<JSONObject> JPersonList = (List<JSONObject>) jResult
									.get("list");
							JsonToPersonData(JPersonList);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(CampusAllPersonActivity.this, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(CampusAllPersonActivity.this, "网络有毒=_=");
					}

				}, null));
	}

	/**
	 * 学校的人gridview点击监听
	 */
	public class PersonGridViewItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CampusPersonModel personModel = (CampusPersonModel) parent
					.getAdapter().getItem(position);

			ToastUtil.show(CampusAllPersonActivity.this,
					"UserID:" + personModel.getUserId());
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
