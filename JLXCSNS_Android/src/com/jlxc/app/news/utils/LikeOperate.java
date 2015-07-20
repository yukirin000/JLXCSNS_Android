package com.jlxc.app.news.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.LikeModel;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;

public class LikeOperate {

	// 对应的gridview适配器
	private HelloHaAdapter<LikeModel> likeGVAdapter;
	// 上行文信息
	private Context mContext;
	// 用户对象
	private UserModel userModel = UserManager.getInstance().getUser();
	// 是否正在上传点赞数据
	private boolean isLikeUpload = false;
	// 上次点赞的状态
	private boolean severIsLike = false;
	// 记录上次的操作类型
	private boolean lastOperateIsLike = false;
	// 动态id
	private String newsID;
	// 回调接口
	private LikeCallBack callInterface;

	// 设置回调
	public void setLikeListener(LikeCallBack callInterface) {
		this.callInterface = callInterface;
	}

	public LikeOperate(Context context) {
		this.mContext = context;
	}

	/**
	 * 设置操作的数据
	 * */
	public void setOperateData(HelloHaAdapter<LikeModel> gvAdapter,
			String newsId) {
		this.likeGVAdapter = gvAdapter;
		this.newsID = newsId;
		if (null == likeGVAdapter) {
			LogUtils.e("点赞adpter为空");
		}
	}

	/**
	 * 点赞操作函数
	 * */
	public void Like() {
		LogUtils.i("点赞");
		lastOperateIsLike = true;
		callInterface.onOperateStart(lastOperateIsLike);

		LikeModel myModel = new LikeModel();
		myModel.setUserID(String.valueOf(userModel.getUid()));
		myModel.setHeadImage(userModel.getHead_image());
		myModel.setHeadSubImage(userModel.getHead_sub_image());
		try {
			likeGVAdapter.addToFirst(myModel);
			LogUtils.i("-----添加头像");
		} catch (Exception e) {
			ToastUtil.show(mContext, "发生了点小故障 (・ˍ・*)");
		}

		likeNetOperate(newsID, "1");
	}

	/**
	 * 取消点赞
	 * */
	public void Cancel() {
		LogUtils.i("取消点赞");
		lastOperateIsLike = false;
		callInterface.onOperateStart(lastOperateIsLike);
		// 移除头像
		try {
			for (int index = 0; index < likeGVAdapter.getCount(); ++index) {
				if (likeGVAdapter.getItem(index).getUserID()
						.equals(String.valueOf(userModel.getUid()))) {
					likeGVAdapter.remove(index);
					LogUtils.i("-----移除头像");
					break;
				} else {
					LogUtils.e("点赞数据发生了错误.");
				}
			}
		} catch (Exception e) {
			ToastUtil.show(mContext, "发生了点小故障 (・ˍ・*)");
		}
		likeNetOperate(newsID, "0");
	}

	/**
	 * 撤销上次操作
	 * */
	public void Revoked() {
		if (lastOperateIsLike) {
			this.Cancel();
		} else {
			this.Like();
		}
	}

	/**
	 * 设置上次服务器的点赞状态
	 * */
	public void setlastSeverLikeState(boolean state) {
		severIsLike = state;
	}

	/***
	 * 点赞操作网络请求
	 */
	private void likeNetOperate(String newsId, String likeOrCancel) {
		if (!isLikeUpload) {
			isLikeUpload = true;
			if (likeOrCancel.equals("1")) {
				severIsLike = true;
			} else {
				severIsLike = false;
			}
			// 参数设置
			RequestParams params = new RequestParams();
			params.addBodyParameter("news_id", newsId);
			params.addBodyParameter("isLike", likeOrCancel);
			params.addBodyParameter("user_id",
					String.valueOf(userModel.getUid()));
			params.addBodyParameter("is_second", "0");

			HttpManager.post(JLXCConst.LIKE_OR_CANCEL, params,
					new JsonRequestCallBack<String>(
							new LoadDataHandler<String>() {

								@Override
								public void onSuccess(JSONObject jsonResponse,
										String flag) {
									super.onSuccess(jsonResponse, flag);
									int status = jsonResponse
											.getInteger(JLXCConst.HTTP_STATUS);
									if (status == JLXCConst.STATUS_SUCCESS) {
										isLikeUpload = false;
									}

									if (status == JLXCConst.STATUS_FAIL) {
										// 失败则取消上次操作
										callInterface
												.onOperateFail(lastOperateIsLike);
										Revoked();
										LogUtils.e(jsonResponse
												.getString(JLXCConst.HTTP_MESSAGE));
										severIsLike = !severIsLike;
										isLikeUpload = false;
									}
								}

								@Override
								public void onFailure(HttpException arg0,
										String arg1, String flag) {
									super.onFailure(arg0, arg1, flag);
									// 失败则取消上次操作
									callInterface
											.onOperateFail(lastOperateIsLike);
									Revoked();
									ToastUtil.show(mContext, "卧槽，竟然操作失败，检查下网络");
									severIsLike = !severIsLike;
									isLikeUpload = false;
								}
							}, null));
		} else {
			LogUtils.e("正在传输数据 ");
		}
	}

	public interface LikeCallBack {
		public void onOperateStart(boolean isLike);

		public void onOperateFail(boolean isLike);
	}
}
