package com.jlxc.app.login.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.R.string;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.locate.JLXCLocate;
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
	// 学校<JSON对象列表
	List<JSONObject> schoolList = new ArrayList<JSONObject>();
	// 用户当前所在的区域编码
	private String districtCode = "110101";
	// 查询学校时的page值
	private int pageIndex = 1;

	@OnClick(R.id.base_tv_back)
	public void viewCickListener(View view) {

	}

	@Override
	public int setLayoutId() {
		return R.layout.select_school_layout;
	}

	@Override
	protected void setUpView() {
		initData();
		initListViewSet();
		// 设置为底部刷新模式
		schoolListView.setMode(Mode.PULL_FROM_END);

		// 开始定位，每0.2s通知一次，距离变化10通知一次，超时时间为5秒
		Location districtLocation = new Location(SelectSchoolActivity.this);
		districtLocation.locateInit(200, 10, 5000);

		// 设置搜索框内容改变的监听事件
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				//
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
				// 数据绑定
				helper.setText(R.id.school_name_textView, item.getSchoolName());
				if (SchoolModel.JUNIOR_MIDDLE_SCHOOL == item.getSchoolType()) {
					helper.setText(R.id.school_location_textView,
							item.getCityName() + item.getDistrictName()
									+ "◆ 初中");
				} else if (SchoolModel.SENIOR_MIDDLE_SCHOOL == item
						.getSchoolType()) {
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
				new GetDataTask().execute(Integer.valueOf(PULL_DOWM_MODE));
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 上拉刷新
				new GetDataTask().execute(Integer.valueOf(PULL_UP_MODE));
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
	private void initData() {
		getSchoolList("1", districtCode, "");
		if (null != schoolList) {
			for (JSONObject schoolobj : schoolList) {
				SchoolModel tempModel = new SchoolModel();
				tempModel.setContentWithJson(schoolobj);
				mDatas.add(tempModel);
			}
			schoolList.clear();
		}
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

		LogUtils.i("查询成功,page=" + page);
		LogUtils.i("districtCode=" + districtCode);

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
							schoolList = (List<JSONObject>) jResult.get("list");
							LogUtils.i("查询成功");
						}

						if (status == JLXCConst.STATUS_FAIL) {
							Toast.makeText(
									SelectSchoolActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE),
									Toast.LENGTH_SHORT).show();
							LogUtils.e("查询失败！");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						Toast.makeText(SelectSchoolActivity.this,
								"卧槽，竟然查询失败，检查下网络", Toast.LENGTH_SHORT).show();
					}

				}, null));
	}

	/**
	 * 异步刷新事件
	 * */
	private class GetDataTask extends
			AsyncTask<Integer, Void, List<SchoolModel>> {

		// 后台处理部分
		@Override
		protected List<SchoolModel> doInBackground(Integer... params) {
			List<SchoolModel> newDatas = new ArrayList<SchoolModel>();
			if (PULL_DOWM_MODE == params[0].intValue()) {
				// 下拉时获取数据
				/*
				 * getSchoolList("1", districtCode, ""); for (JSONObject
				 * schoolobj : schoolList) { SchoolModel tempModel = new
				 * SchoolModel(); tempModel.setContentWithJson(schoolobj);
				 * newDatas.add(tempModel); } schoolList.clear();
				 */
			} else {
				// 上拉时获取数据
				getSchoolList(String.valueOf(pageIndex), districtCode, "");
				for (JSONObject schoolobj : schoolList) {
					SchoolModel tempModel = new SchoolModel();
					tempModel.setContentWithJson(schoolobj);
					newDatas.add(tempModel);
				}
				// 如果上次
				if (schoolList.size() > 0) {
					pageIndex++;
				}
				schoolList.clear();
			}
			return newDatas;
		}

		// 获取数据后的处理
		@Override
		protected void onPostExecute(List<SchoolModel> newDatas) {
			schoolAdapter.addAll(newDatas);
			schoolListView.onRefreshComplete();
			super.onPostExecute(newDatas);
		}
	}

	// 定位部分
	private class Location extends JLXCLocate {
		public Location(Context context) {
			super(context);
		}

		@Override
		public void onLocateFinish(boolean state) {
			if (state) {
				// 定位成功
				listTitleTextView.setText(this.getCityStr() + " "
						+ this.getDistrictStr());
				this.stopLocation();
			} else {
				// 定位失败
			}
		}
	}
}
