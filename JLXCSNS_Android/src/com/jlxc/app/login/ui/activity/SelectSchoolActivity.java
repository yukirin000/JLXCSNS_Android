package com.jlxc.app.login.ui.activity;

import java.util.ArrayList;
import java.util.List;

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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.locate.JLXCLocate;
import com.jlxc.app.base.model.SchoolModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SelectSchoolActivity extends BaseActivityWithTopBar {

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

	@OnClick({ R.id.base_tv_back, R.id.next_button, R.id.revalidated_textview,
			R.id.register_activity })
	public void viewCickListener(View view) {

	}

	@Override
	public int setLayoutId() {
		return R.layout.select_school_layout;
	}

	@Override
	protected void setUpView() {
		testDatas();
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

	// listview的设置
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
							item.getSchoolLocation() + "◆ 初中");
				} else if (SchoolModel.SENIOR_MIDDLE_SCHOOL == item
						.getSchoolType()) {
					helper.setText(R.id.school_location_textView,
							item.getSchoolLocation() + "◆ 高中");
				} else {
					// 其他
				}
			}
		};

		// 适配器绑定
		schoolListView.setAdapter(schoolAdapter);

		// 设置刷新事件监听
		schoolListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 下拉
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 上拉
				new GetDataTask().execute();
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

	// 测试数据
	private void testDatas() {
		SchoolModel schoolModel = new SchoolModel("育才中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("深圳中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("保安中学", "深圳市保安区",
				SchoolModel.SENIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("罗湖中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("深圳中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("保安中学", "深圳市保安区",
				SchoolModel.SENIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("罗湖中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("深圳中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("保安中学", "深圳市保安区",
				SchoolModel.SENIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

		schoolModel = new SchoolModel("罗湖中学", "深圳市罗湖区",
				SchoolModel.JUNIOR_MIDDLE_SCHOOL);
		mDatas.add(schoolModel);

	}

	// 异步刷新事件
	private class GetDataTask extends AsyncTask<Void, Void, List<SchoolModel>> {

		// 后台处理部分
		@Override
		protected List<SchoolModel> doInBackground(Void... params) {
			List<SchoolModel> newDatas = null;
			try {
				Thread.sleep(1000);
				// 获取后台数据
			} catch (InterruptedException e) {
			}
			return newDatas;
		}

		// 获取数据后的处理
		@Override
		protected void onPostExecute(List<SchoolModel> newDatas) {
			schoolAdapter.addAll(newDatas);
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
