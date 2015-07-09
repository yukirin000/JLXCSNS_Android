package com.jlxc.app.news.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
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
import com.jlxc.app.login.ui.activity.SelectSchoolActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;

public class NewsDetailActivity extends BaseActivityWithTopBar {

	@ViewInject(R.id.news_detail_listView)
	private ListView newsDetailListView;
	private HelloHaAdapter<String> detailAdapter;
	
	/////////////////////////////override//////////////////////////////////
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_news_detail;
	}

	@Override
	protected void setUpView() {
		//获取数据
		getData();
	}
	
	///////////////////////////private method//////////////////////////////
	private void getData() {
		
//		http://192.168.1.100/jlxc_php/index.php/Home/MobileApi/newsDetail?news_id=63&user_id=19
//		String path = JLXCConst.NEWS_DETAIL+"?"+"news_id="+63+"&user_id="+19;
//		HttpManager.get(path,
//				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
//
//					@Override
//					public void onSuccess(JSONObject jsonResponse, String flag) {
//						super.onSuccess(jsonResponse, flag);
//						int status = jsonResponse
//								.getInteger(JLXCConst.HTTP_STATUS);
//						if (status == JLXCConst.STATUS_SUCCESS) {
//							JSONObject jResult = jsonResponse
//									.getJSONObject(JLXCConst.HTTP_RESULT);
//							LogUtils.i(jResult.toJSONString(), 1);
//						}
//
//						if (status == JLXCConst.STATUS_FAIL) {
//							ToastUtil.show(NewsDetailActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
//						}
//					}
//
//					@Override
//					public void onFailure(HttpException arg0, String arg1,
//							String flag) {
//						super.onFailure(arg0, arg1, flag);
//						ToastUtil.show(NewsDetailActivity.this, "网络有毒=_=");
//					}
//
//				}, null));
		
		
		
		View view = View.inflate(this, R.layout.news_detail_head_view, null);
		newsDetailListView.addHeaderView(view, null, false);
//		View gridView = view.findViewById(R.id.images_grid_view);
//		gridView.setVisibility(View.GONE);
		
		List<String> list = new ArrayList<String>();
		detailAdapter = new HelloHaAdapter<String>(this, R.layout.location_listitem_adapter, list) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, String item) {
				// TODO Auto-generated method stub
//				helper.setText(R.id.location_name_textView, item);
			}
		};
		newsDetailListView.setAdapter(detailAdapter);		
	}
}
