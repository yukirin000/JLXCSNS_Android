package com.jlxc.app.discovery.ui.avtivity;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.MainTabActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.discovery.model.PersonModel;
import com.jlxc.app.login.ui.activity.SecondLoginActivity;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

public class ContactsUserActivity extends BaseActivityWithTopBar {

	// 获取库Phon表字段
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Phone.CONTACT_ID };
	// 联系人显示名称
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;
	// 电话号码
	private static final int PHONES_NUMBER_INDEX = 1;
	// 联系人名称
	private ArrayList<String> mContactsName = new ArrayList<String>();
	// 联系人号码
	private ArrayList<String> mContactsNumber = new ArrayList<String>();
	// 联系人listview
	@ViewInject(R.id.listview_contacts_user)
	private PullToRefreshListView contactsListView;
	// 数据源
	private List<PersonModel> dataList = new ArrayList<PersonModel>();
	// 适配器
	private HelloHaAdapter<PersonModel> contactsAdapter;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	// bitmap的处理
	private static BitmapUtils bitmapUtils;
	// 用户实例
	private UserModel userModel;

	@Override
	public int setLayoutId() {
		return R.layout.activity_add_contacts_user;
	}

	@Override
	protected void setUpView() {
		init();
		getPhoneContacts();
		getSIMContacts();
		listviewSet();
		getContactsPerson(String.valueOf(userModel.getUid()),
				getContactsJSON(mContactsNumber));
	}

	/**
	 * 初始化
	 * */
	private void init() {
		initBitmapUtils();
		userModel = UserManager.getInstance().getUser();
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
	}

	/**
	 * 初始化BitmapUtils
	 * */
	private void initBitmapUtils() {
		bitmapUtils = new BitmapUtils(ContactsUserActivity.this);
		bitmapUtils.configDefaultBitmapMaxSize(screenWidth, screenHeight);
		bitmapUtils.configDefaultLoadingImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultLoadFailedImage(android.R.color.darker_gray);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	/**
	 * 数据绑定初始化
	 * */
	private void listviewSet() {
		// 设置刷新模式
		contactsListView.setMode(Mode.BOTH);

		contactsAdapter = new HelloHaAdapter<PersonModel>(
				ContactsUserActivity.this, R.layout.add_contacts_item_layout,
				dataList) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					PersonModel item) {
				// 联系人头像
				ImageView imgView = helper.getView(R.id.iv_contacts_head);
				LayoutParams laParams = (LayoutParams) imgView
						.getLayoutParams();
				laParams.width = laParams.height = (screenWidth) / 7;
				imgView.setLayoutParams(laParams);
				imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				bitmapUtils
						.configDefaultBitmapMaxSize(screenWidth, screenWidth);

				// 绑定头像图片
				bitmapUtils.configDefaultBitmapMaxSize(laParams.width,
						laParams.width);
				helper.setImageUrl(R.id.iv_contacts_head, bitmapUtils,
						item.getHeadSubImage(), new NewsBitmapLoadCallBack());

				// 绑定昵称
				helper.setText(R.id.tv_contacts_name, item.getUserName());
				helper.setText(R.id.tv_contact_user_name, item.getUserName());
			}
		};

		/**
		 * 刷新监听
		 * */
		contactsListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						contactsListView.postDelayed(new Runnable() {
							@Override
							public void run() {
								contactsListView.onRefreshComplete();
							}
						}, 0);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						contactsListView.postDelayed(new Runnable() {
							@Override
							public void run() {
								contactsListView.onRefreshComplete();
							}
						}, 0);
					}
				});

		// 快速滑动时不加载图片
		contactsListView.setOnScrollListener(new PauseOnScrollListener(
				bitmapUtils, false, true));
		contactsListView.setAdapter(contactsAdapter);

		// 单击
		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 跳转至用户的主页
				Intent intentUsrMain = new Intent(ContactsUserActivity.this,
						OtherPersonalActivity.class);
				intentUsrMain.putExtra(
						OtherPersonalActivity.INTENT_KEY,
						JLXCUtils.stringToInt(contactsAdapter.getItem(
								position - 1).getUerId()));
				startActivityWithRight(intentUsrMain);
			}
		});
	}

	/**
	 * 数据转换为jsonObject
	 * */
	private String getContactsJSON(ArrayList<String> numberList) {
		JSONArray array = new JSONArray();
		for (String phoneNum : numberList) {
			array.add(phoneNum);
		}
		return array.toJSONString();
	}

	/**
	 * 得到手机通讯录联系人信息
	 **/
	private void getPhoneContacts() {
		ContentResolver resolver = ContactsUserActivity.this
				.getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				if (TextUtils.isEmpty(phoneNumber)) {
					continue;
				}
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);
				mContactsName.add(contactName);
				mContactsNumber.add(phoneNumber);
			}
			phoneCursor.close();
		}
	}

	/**
	 * 得到手机SIM卡联系人人信息
	 **/
	private void getSIMContacts() {
		ContentResolver resolver = ContactsUserActivity.this
				.getContentResolver();
		Uri uri = Uri.parse("content://icc/adn");
		Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
				null);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				if (TextUtils.isEmpty(phoneNumber)) {
					continue;
				}
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);
				mContactsName.add(contactName);
				mContactsNumber.add(phoneNumber);
			}
			phoneCursor.close();
		}
	}

	/**
	 * 获取通讯录好友信息
	 * */
	private void getContactsPerson(String userId, String contact) {
		// 网络请求
		showLoading("加载中...", true);
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", userId);
		params.addBodyParameter("contact", contact);
		LogUtils.i("path=" + JLXCConst.GET_CONTACT_USER + "?user_id=" + userId
				+ "&contact=" + contact);
		HttpManager.post(JLXCConst.GET_CONTACT_USER, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						switch (status) {
						case JLXCConst.STATUS_SUCCESS:
							hideLoading();
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							// 获取数据列表
							@SuppressWarnings("unchecked")
							List<JSONObject> JPersonList = (List<JSONObject>) jResult
									.get("list");
							JsonToPersonData(JPersonList);
							break;

						case JLXCConst.STATUS_FAIL:
							hideLoading();
							ToastUtil.show(ContactsUserActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						ToastUtil.show(ContactsUserActivity.this, "网络有毒=_=");
					}

				}, null));
	}

	/**
	 * 数据解析
	 * */
	private void JsonToPersonData(List<JSONObject> jPersonList) {
		dataList.clear();
		for (JSONObject likeObj : jPersonList) {
			PersonModel tempPerson = new PersonModel();
			tempPerson.setContentWithJson(likeObj);
			dataList.add(tempPerson);
		}
		contactsAdapter.addAll(dataList);
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
