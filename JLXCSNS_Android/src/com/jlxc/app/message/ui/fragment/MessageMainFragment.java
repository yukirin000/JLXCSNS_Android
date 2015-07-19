package com.jlxc.app.message.ui.fragment;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.demo.ui.fragment.FragmentPage1;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class MessageMainFragment extends BaseFragment {

	@ViewInject(R.id.vPager)
	private ViewPager mPager;//页卡内容
	@ViewInject(R.id.text1)
	private TextView t1;
	@ViewInject(R.id.text2)
    private TextView t2;
//    private ImageView cursor;// 动画图片
    private int offset = 0;// 动画图片偏移量
//    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
	
	@OnClick({R.id.text1,R.id.text2})
    private void clickEvent(View view) {
		switch (view.getId()) {
		case R.id.text1:
			mPager.setCurrentItem(0);
			break;
		case R.id.text2:
			mPager.setCurrentItem(1);				
			break;
		default:
			break;
		}
		
	}
    
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.fragment_message_main;
	}

	@Override
	public void loadLayout(View rootView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpViews(View rootView) {
		// TODO Auto-generated method stub

		InitViewPager();
	}
	

	/**
     * 初始化ViewPager
     */
    @SuppressLint("InflateParams") 
    private void InitViewPager() {
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        
        mPager.setAdapter(new MessageFragmentPagerAdapter(getChildFragmentManager()));;
//        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }
    
    /**
     * ViewPager适配器
*/
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;
        
        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }
    
    
    private class MessageFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public MessageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                        ConversationListFragment listFragment = ConversationListFragment.getInstance();
                        Uri uri = Uri.parse("rong://" + getActivity().getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//讨论组
                                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//应用公众服务。
                                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//系统
                                .build();
                        listFragment.setUri(uri);
                        fragment = listFragment;
                    break;
                case 1:
                	fragment = new FragmentPage1();
                    break;

            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
    
    
    /**
     * 页卡切换监听
*/
    public class MyOnPageChangeListener implements OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        @Override
        public void onPageSelected(int arg0) {
//        	Animation animation = null;
//            switch (arg0) {
//            case 0:
//                if (currIndex == 1) {
//                    animation = new TranslateAnimation(one, 0, 0, 0);
//                } else if (currIndex == 2) {
//                    animation = new TranslateAnimation(two, 0, 0, 0);
//                }
//                break;
//            case 1:
//                if (currIndex == 0) {
//                    animation = new TranslateAnimation(offset, one, 0, 0);
//                } else if (currIndex == 2) {
//                    animation = new TranslateAnimation(two, one, 0, 0);
//                }
//                break;
//            case 2:
//                if (currIndex == 0) {
//                    animation = new TranslateAnimation(offset, two, 0, 0);
//                } else if (currIndex == 1) {
//                    animation = new TranslateAnimation(one, two, 0, 0);
//                }
//                break;
//            }
//            currIndex = arg0;
//            animation.setFillAfter(true);// True:图片停在动画结束位置
//            animation.setDuration(300);
//            cursor.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

}
