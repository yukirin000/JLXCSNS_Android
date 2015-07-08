package com.jlxc.app.login.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
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
import com.jlxc.app.base.model.SchoolModel;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SelectSchoolActivity extends BaseActivityWithTopBar {

	// 下拉模式
	public static final int PULL_DOWM_MODE = 0;
	// 上拉模式
	public static final int PULL_UP_MODE = 1;
	// 学校数据列表
	private List<SchoolModel> mDatas = new ArrayList<SchoolModel>();
	// 学校listview的适配器
	private HelloHaAdapter<SchoolModel> schoolAdapter;
	// 搜索框
	@ViewInject(R.id.search_edittext)
	private EditText searchEditText;
	// 学校列表标头
	@ViewInject(R.id.school_list_title_textview)
	private TextView listTitleTextView;
	// 学校列表listview
	@ViewInject(R.id.school_listview)
	private PullToRefreshListView schoolListView;
	// 用户当前所在的区域编码
	private String districtCode = "110101";
	// 需要搜索的学校名字
	private String schoolName = "";
	// 查询学校时的page值
	private int pageIndex = 1;
	// 定位对象
	Location districtLocation;

	@OnClick(R.id.base_tv_back)
	public void viewCickListener(View view) {
		back();
	}

	@Override
	public int setLayoutId() {
		return R.layout.activity_select_school_layout;
	}

	@Override
	protected void onStart() {
		// 开始定位，每0.2s通知一次，距离变化10通知一次，超时时间为5秒
		districtLocation = new Location(SelectSchoolActivity.this);
		districtLocation.locateInit(200, 10, 5000);
		super.onStart();
	}

	@Override
	protected void setUpView() {
		initListViewSet();
		// 设置为底部刷新模式
		schoolListView.setMode(Mode.PULL_FROM_END);

		// 设置搜索框内容改变的监听事件
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				getSchoolList();
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

	/***
	 * 
	 * listview的设置
	 */
	private void initListViewSet() {
		schoolAdapter = new HelloHaAdapter<SchoolModel>(
				SelectSchoolActivity.this, R.layout.school_listitem_layout,
				mDatas) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					SchoolModel item) {
				// 学校绑定
				helper.setText(R.id.school_name_textView, item.getSchoolName());
				// 位置绑定
				if (item.getSchoolType().equals(
						SchoolModel.JUNIOR_MIDDLE_SCHOOL)) {
					helper.setText(R.id.school_location_textView,
							item.getCityName() + item.getDistrictName()
									+ "◆ 初中");
				} else if (item.getSchoolType().equals(
						SchoolModel.SENIOR_MIDDLE_SCHOOL)) {
					helper.setText(R.id.school_location_textView,
							item.getCityName() + item.getDistrictName()
									+ "◆ 高中");
				} else {
					// 其他
				}
			}
		};

		// 适配器绑定
		schoolListView.setAdapter(schoolAdapter);
		schoolListView.setPullToRefreshOverScrollEnabled(false);
		// 设置刷新事件监听
		schoolListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 下拉刷新
				// getSchoolList(String.valueOf(pageIndex), districtCode, "");
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 上拉刷新
				getSchoolList(String.valueOf(pageIndex), districtCode, schoolName);
				LogUtils.i("-------" + pageIndex);
			}

		});

		// 设置点击事件
		schoolListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 事件处理
			}
		});
	}

	/**
	 * 初始化数据
	 * */
	private void schoolDataHandle(List<JSONObject> dataList) {
		List<SchoolModel> newDatas = new ArrayList<SchoolModel>();
		for (JSONObject schoolobj : dataList) {
			SchoolModel tempModel = new SchoolModel();
			tempModel.setContentWithJson(schoolobj);
			newDatas.add(tempModel);
		}
		schoolAdapter.addAll(newDatas);
		dataList.clear();
	}

	/**
	 * 获取学校列表
	 * */
	private void getSchoolList(String page, String districtCode,
			String schoolName) {
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("page", page);
		params.addBodyParameter("district_code", districtCode);
		params.addBodyParameter("school_name", schoolName);

		HttpManager.post(JLXCConst.GET_SCHOOL_LIST, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							// 获取学校列表
							LogUtils.i("查询学校数据成功");
							List<JSONObject> objList = (List<JSONObject>) jResult
									.get("list");
							if (objList.size() > 0) {
								pageIndex++;
							}
							schoolDataHandle(objList);
							schoolListView.onRefreshComplete();
						}

						if (status == JLXCConst.STATUS_FAIL) {
							Toast.makeText(
									SelectSchoolActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE),
									Toast.LENGTH_SHORT).show();
							LogUtils.e("查询学校列表失败");
							schoolListView.onRefreshComplete();
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						Toast.makeText(SelectSchoolActivity.this,
								"卧槽，竟然查询失败，检查下网络", Toast.LENGTH_SHORT).show();
						schoolListView.onRefreshComplete();
					}

				}, null));
	}

	/**
	 * 定位获取区域代码
	 * */
	private class Location implements AMapLocationListener {

		private Context mContext;
		private LocationManagerProxy aMapLocManager = null;

		public Location(Context context) {
			mContext = context;
		}

		/**
		 * 初始化定位,通知时间（毫秒），通知距离（米），超时值（毫秒）
		 */
		public void locateInit(long minTime, float minDistance, int timeOutValue) {
			aMapLocManager = LocationManagerProxy.getInstance(mContext);
			aMapLocManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, minTime, minDistance,
					this);
			LogUtils.i("Locate init successed.");
		}

		/**
		 * 停止定位并销毁对象
		 */
		private void stopLocation() {
			if (aMapLocManager != null) {
				aMapLocManager.removeUpdates(this);
				aMapLocManager.destroy();
			}
			aMapLocManager = null;
			LogUtils.i("Locate stop successed.");
		}

		@Override
		public void onLocationChanged(android.location.Location location) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(AMapLocation location) {
			if (location != null) {
				districtCode = location.getAdCode();
				LogUtils.i("get district Code successed.");
				// 查询区域代码成功后开始查找学校数据
				getSchoolList("1", districtCode, schoolName);
				stopLocation();
			}
		}
	}

	@Override
	protected void onStop() {
		// 停止定位
		districtLocation.stopLocation();
		super.onStop();
	}

	/**
	 * 返回操作
	 * */
	private void back() {
		// 停止定位
		districtLocation.stopLocation();
		finishWithRight();
	}

	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
}