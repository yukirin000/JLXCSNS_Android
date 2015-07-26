package com.jlxc.app.news.utils;

import android.R.integer;
import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.news.model.CommentModel;
import com.jlxc.app.news.model.SubCommentModel;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;

public class NewsOperate {
	// 操作类型
	public final static int OP_Type_Delete_News = 0;
	public final static int OP_Type_Add_Comment = 1;
	public final static int OP_Type_Delete_Comment = 2;
	public final static int OP_Type_Add_Sub_Comment = 3;
	public final static int OP_Type_Delete_Sub_Comment = 4;

	private Context mContext;
	// 回调接口
	private OperateCallBack callInterface;

	public NewsOperate(Context context) {
		this.mContext = context;
	}

	// 设置回调
	public void setOperateListener(OperateCallBack callInterface) {
		this.callInterface = callInterface;
	}

	/**
	 * 删除动态操作
	 * */
	public void deleteNews(String newsId) {
		callInterface.onStart(OP_Type_Delete_News);
		RequestParams params = new RequestParams();
		params.addBodyParameter("news_id", newsId);

		HttpManager.post(JLXCConst.DELETE_NEWS, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							callInterface.onFinish(OP_Type_Delete_News, true,
									null);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							callInterface.onFinish(OP_Type_Delete_News, false,
									null);
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						callInterface
								.onFinish(OP_Type_Delete_News, false, null);
						ToastUtil.show(mContext, "竟然删除失败，请检查网络 （/TДT)/");
					}
				}, null));
	}

	/**
	 * 发送一级评论
	 * */
	public void publishComment(final UserModel user, String newsID,
			String content) {
		callInterface.onStart(OP_Type_Add_Comment);
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", String.valueOf(user.getUid()));
		params.addBodyParameter("news_id", newsID);
		params.addBodyParameter("comment_content", content);

		HttpManager.post(JLXCConst.SEND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							// 获取评论成功的返回值
							JSONObject JResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							CommentModel temMode = new CommentModel();
							temMode.setContentWithJson(JResult);
							temMode.setPublishName(user.getName());
							temMode.setHeadSubImage(JLXCConst.ATTACHMENT_ADDR
									+ user.getHead_sub_image());
							callInterface.onFinish(OP_Type_Add_Comment, true,
									temMode);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							callInterface.onFinish(OP_Type_Add_Comment, false,
									null);
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						callInterface
								.onFinish(OP_Type_Add_Comment, false, null);
						ToastUtil.show(mContext, "竟然评论失败，请检查网络 （/TДT)/");
					}
				}, null));
	}

	/**
	 * 发送二级评论
	 * */
	public void publishSubComment(final UserModel user, String newsID,
			SubCommentModel subModle) {
		callInterface.onStart(OP_Type_Add_Sub_Comment);
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", String.valueOf(user.getUid()));
		params.addBodyParameter("news_id", newsID);
		params.addBodyParameter("comment_content", subModle.getCommentContent());
		params.addBodyParameter("reply_uid", subModle.getReplyUid());
		params.addBodyParameter("reply_comment_id",
				subModle.getReplyCommentId());
		params.addBodyParameter("top_comment_id", subModle.getTopCommentId());

		final String replyToName = subModle.getReplyName();
		HttpManager.post(JLXCConst.SEND_SECOND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject JResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);

							SubCommentModel tempMd = new SubCommentModel();
							tempMd.setContentWithJson(JResult);
							tempMd.setReplyName(replyToName);
							tempMd.setPublishName(user.getName());
							tempMd.setPublishId(String.valueOf(user.getUid()));
							callInterface.onFinish(OP_Type_Add_Sub_Comment,
									true, tempMd);

						}

						if (status == JLXCConst.STATUS_FAIL) {
							callInterface.onFinish(OP_Type_Add_Sub_Comment,
									false, null);
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						callInterface.onFinish(OP_Type_Add_Sub_Comment, false,
								null);
						ToastUtil.show(mContext, "竟然回复失败，请检查网络 （/TДT)/");
					}
				}, null));
	}

	/**
	 * 删除一级评论
	 * */
	public void deleteComment(String CID, String newsID) {
		callInterface.onStart(OP_Type_Delete_Comment);
		RequestParams params = new RequestParams();
		params.addBodyParameter("cid", CID);
		params.addBodyParameter("news_id", newsID);

		HttpManager.post(JLXCConst.DELETE_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							callInterface.onFinish(OP_Type_Delete_Comment,
									true, null);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							callInterface.onFinish(OP_Type_Delete_Comment,
									false, null);
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						callInterface.onFinish(OP_Type_Delete_Comment, false,
								null);
					}
				}, null));
	}

	/**
	 * 删除二级评论
	 * */
	public void deleteSubComment(String CID, String newsID) {
		callInterface.onStart(OP_Type_Delete_Sub_Comment);
		RequestParams params = new RequestParams();
		params.addBodyParameter("cid", CID);
		params.addBodyParameter("news_id", newsID);

		HttpManager.post(JLXCConst.DELETE_SECOND_COMMENT, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							callInterface.onFinish(OP_Type_Delete_Sub_Comment,
									true, null);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							callInterface.onFinish(OP_Type_Delete_Sub_Comment,
									false, null);
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						callInterface.onFinish(OP_Type_Delete_Sub_Comment,
								false, null);
						super.onFailure(arg0, arg1, flag);
					}
				}, null));

	}

	/***
	 * 点赞操作网络请求
	 */
	public void likeNetOperate(String userID, String newsId, String likeOrCancel) {
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("news_id", newsId);
		params.addBodyParameter("isLike", likeOrCancel);
		params.addBodyParameter("user_id", userID);
		params.addBodyParameter("is_second", "0");

		HttpManager.post(JLXCConst.LIKE_OR_CANCEL, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							// 点赞成功
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(mContext, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						// 点赞失败
					}
				}, null));
	}

	/**
	 * 回调类
	 **/
	public interface OperateCallBack {
		public void onStart(int operateType);

		public void onFinish(int operateType, boolean isSucceed,
				Object resultValue);
	}
}
