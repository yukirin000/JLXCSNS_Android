package com.jlxc.app.news.ui.activity;

import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jlxc.app.R;
import com.jlxc.app.base.model.News;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsListFragment extends Fragment {
	
	//动态listview
	@ViewInject(R.id.news_listview)
	private PullToRefreshListView newsListView;
	//
	private List<News> listItem = null;// 原始数据源
	// item数据源
//	private List<MainListItem> listItemData;
	// 适配器
	//private HelloHaAdapter<MainListItem> mAdapter = null;
	// 使支持多种item
//	private MultiItemTypeSupport<MainListItem> multiItemTypeSupport;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_news_list,
				container, false);

		return rootView;
	}

}
