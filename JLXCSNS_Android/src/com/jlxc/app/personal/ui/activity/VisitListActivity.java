package com.jlxc.app.personal.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
//最近来访Activity
import com.jlxc.app.personal.model.VisitModel;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
public class VisitListActivity extends BaseActivityWithTopBar {

	public final static String INTENT_KEY = "uid";
	//下拉列表
	@ViewInject(R.id.visit_refresh_list)
	private PullToRefreshListView visitListView;
	//adapter
	HelloHaAdapter<VisitModel> visitAdapter;
	// 下拉模式
	public static final int PULL_DOWM_MODE = 0;
	// 上拉模式
	public static final int PULL_UP_MODE = 1;
	// 是否下拉刷新
	private boolean isPullDowm = false;
	//是否是最后一页
	private boolean isLast = false; 
	private BitmapUtils bitmapUtils;
	//uid
	private int uid;
	//当前的页数
	private int currentPage = 1;
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_visit_list;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		uid = intent.getIntExtra(INTENT_KEY, 0);
		
		bitmapUtils = new BitmapUtils(this);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.ARGB_8888);
		bitmapUtils.configMemoryCacheEnabled(true);
		bitmapUtils.configDiskCacheEnabled(true);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher);
		initListViewSet();
		getVisitsData();
	}

	
	////////////////////////////////////private method //////////////////////////////////////
	/***
	 * 
	 * listview的设置
	 */
	private void initListViewSet() {
		
		//设置内容
		visitAdapter = new HelloHaAdapter<VisitModel>(
				VisitListActivity.this, R.layout.visit_listitem_adapter) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					VisitModel item) {
				
				helper.setText(R.id.name_text_view, item.getName());
				helper.setText(R.id.time_text_view, item.getVisit_time());
				helper.setText(R.id.sign_text_view, item.getSign());
				ImageView headImageView = helper.getView(R.id.head_image_view);
				bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+item.getHead_sub_image());
				
				//设置长按
				//如果是自己则开放长按删除
				if (UserManager.getInstance().getUser().getUid() == uid) {
					LinearLayout linearLayout = (LinearLayout) helper.getView();
					final int index = helper.getPosition();
					linearLayout.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							//长按弹窗
							Builder deleteBuilder = new AlertDialog.Builder(VisitListActivity.this);
							deleteBuilder.setTitle("确定要删除吗");
							deleteBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									deleteVisitModel(index);
								}
							});
							deleteBuilder.setNegativeButton("取消", null);
							deleteBuilder.show();
							
							return false;
						}
					});
				}
				
			}
		};

		// 适配器绑定
		visitListView.setAdapter(visitAdapter);
		visitListView.setMode(Mode.PULL_FROM_START);
		visitListView.setPullToRefreshOverScrollEnabled(false);
		// 设置刷新事件监听
		visitListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// 下拉刷新
				isPullDowm = true;
				currentPage = 1;
				getVisitsData();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (isLast) {
					CountDownTimer countdownTimer = new CountDownTimer(500, 1000) {
						@Override
						public void onTick(long millisUntilFinished) {
						}
						@Override
						public void onFinish() {
							visitListView.onRefreshComplete();
						}
					};
					// 开始倒计时
					countdownTimer.start();
					return;
				}
				currentPage++;
				// 上拉刷新
				isPullDowm = false;
				getVisitsData();
			}

		});

		/**
		 * 设置点击item到事件
		 * */
		visitListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				
			}
		});
		
//		visitListView.setOnItemLong

		// 设置底部自动刷新
		visitListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						if (isLast) {
							visitListView.onRefreshComplete();
							return;
						}
						currentPage++;
						// 底部自动加载
						visitListView.setMode(Mode.PULL_FROM_END);
						visitListView.setRefreshing(true);
						isPullDowm = false;
						getVisitsData();
					}
				});
		
		// 快宿滑动时不加载图片
		visitListView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils,
				false, true));
	}
	
	
	/**
	 * 获取动态数据
	 * */
	private void getVisitsData() {

		String path = JLXCConst.GET_VISIT_LIST + "?" + "uid=" + uid
				+ "&page="+currentPage;
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
							//最后一页
							isLast = jResult.getBooleanValue("is_last");
							// 获取动态列表							
							String jsonArrayStr = jResult.getString(JLXCConst.HTTP_LIST);
							List<VisitModel> list = JSON.parseArray(jsonArrayStr, VisitModel.class);
							
							//如果是下拉刷新
							if (isPullDowm) {
								visitAdapter.replaceAll(list);
							}else {
								visitAdapter.addAll(list);
							}
							visitListView.onRefreshComplete();
							//是否是最后一页
							if (isLast) {
								visitListView.setMode(Mode.PULL_FROM_START);
							}else {
								visitListView.setMode(Mode.BOTH);
							}
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(VisitListActivity.this, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
							visitListView.onRefreshComplete();
							//是否是最后一页
							if (isLast) {
								visitListView.setMode(Mode.PULL_FROM_START);
							}else {
								visitListView.setMode(Mode.BOTH);
							}
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(VisitListActivity.this, "网络有毒=_=");
						visitListView.onRefreshComplete();
						//是否是最后一页
						if (isLast) {
							visitListView.setMode(Mode.PULL_FROM_START);
						}else {
							visitListView.setMode(Mode.BOTH);
						}
					}

				}, null));
	}
	 
	//删除最近来访
	private void deleteVisitModel(final int index) {
		
		VisitModel visitModel = visitAdapter.getItem(index);
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", UserManager.getInstance().getUser().getUid() + "");
		params.addBodyParameter("current_id", visitModel.getUid() + "");
		showLoading("删除中...", false);
		HttpManager.post(JLXCConst.DELETE_VISIT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						hideLoading();
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							visitAdapter.remove(index);
						}
						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(VisitListActivity.this,
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						hideLoading();
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(VisitListActivity.this,
								"网络异常");
					}
				}, null));
	}
	
}
