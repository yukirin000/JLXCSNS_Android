package com.jlxc.app.discovery.ui.fragment;

import android.view.View;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.lidroid.xutils.view.annotation.ViewInject;

public class DiscoveryFragment extends BaseFragment {

	// 标头
	@ViewInject(R.id.tv_discovey_title)
	private TextView titleTextView;
	// 扫一扫按钮
	@ViewInject(R.id.tv_dicovery_sweep)
	private TextView sweepTextView;
	// 搜索框按钮
	@ViewInject(R.id.tv_discovey_search)
	private TextView searchTextView;
	// 推荐的人列表
	@ViewInject(R.id.listview_discovey)
	private PullToRefreshListView rcmdPersonListView;

	@Override
	public int setLayoutId() {
		return R.layout.fragment_discovey_layout;
	}

	@Override
	public void loadLayout(View rootView) {

	}

	@Override
	public void setUpViews(View rootView) {

	}

}
