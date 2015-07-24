package com.jlxc.app.base.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.NewsPushModel;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.message.model.IMModel;

import io.yunba.android.manager.YunBaManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
			
			try {
				String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
			    //如果不是自己订阅的则不接收
				if (!topic.equals(JLXCConst.JLXC+UserManager.getInstance().getUser().getUid())) {
			        return;
				}
				String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);
				StringBuilder showMsg = new StringBuilder();
				showMsg.append("[Message] ").append(YunBaManager.MQTT_TOPIC)
						.append(" = ").append(topic).append(" ,")
							.append(YunBaManager.MQTT_MSG).append(" = ").append(msg);	
				LogUtils.i(showMsg.toString(),1);
				
				//json解析
				JSONObject obj = JSON.parseObject(msg);
				if (obj == null) {
					return;
				}
				int type = obj.getIntValue("type");
				LogUtils.i(type+"",1);
				switch (type) {
				case NewsPushModel.PushAddFriend:
					//添加好友信息
					handleNewFriend(obj, context);
					break;
				case NewsPushModel.PushNewsAnwser:
		            //如果是状态回复消息
					handleNewsPush(obj, context);
					break;
				case NewsPushModel.PushSecondComment:
		            //如果是二级回复消息
					handleNewsPush(obj, context);
					break;
				case NewsPushModel.PushLikeNews:
		            //如果是点赞			
					handleNewsPush(obj, context);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		} else if(YunBaManager.MESSAGE_CONNECTED_ACTION.equals(intent.getAction())) {
			
		} else if(YunBaManager.MESSAGE_DISCONNECTED_ACTION.equals(intent.getAction())) {
			
		} else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {
			
		}
	}
	
	//处理新好友通知
	private void handleNewFriend(JSONObject jsonObject, Context context) {
		JSONObject pushObject = jsonObject.getJSONObject("content");
		IMModel imModel = IMModel.findByGroupId(pushObject.getString("uid"));
		if (null != imModel) {
			//存在 加好友 但是有新朋友
			if (imModel.getCurrentState() == IMModel.GroupNotAdd) {
				imModel.setIsNew(1);
				imModel.update();
			}
		}else {
			imModel = new IMModel();
			//保存群组信息
			imModel.setType(pushObject.getIntValue("type"));
			imModel.setTargetId(pushObject.getString("uid"));
			imModel.setTitle(pushObject.getString("name"));
			imModel.setAvatarPath(pushObject.getString("avatar"));
			imModel.setIsNew(1);
			imModel.setCurrentState(IMModel.GroupNotAdd);
			imModel.setIsRead(0);
			imModel.setOwner(UserManager.getInstance().getUser().getUid());
			imModel.save();
			
			//发送通知
			Intent newsGroupIntent = new Intent(JLXCConst.BROADCAST_NEW_MESSAGE_PUSH);
			context.sendBroadcast(newsGroupIntent);
			//顶部更新
			Intent messageIntent = new Intent(JLXCConst.BROADCAST_MESSAGE_REFRESH);
			context.sendBroadcast(messageIntent);
		}
		
		//徽标更新
		Intent tabIntent = new Intent(JLXCConst.BROADCAST_TAB_BADGE);
		context.sendBroadcast(tabIntent);
	}
	
	//新闻回复点赞到来 处理消息
	private void handleNewsPush(JSONObject jsonObject, Context context) {
		JSONObject pushObject = jsonObject.getJSONObject("content");
		NewsPushModel pushModel = new NewsPushModel();
		//存储
		pushModel.setUid(pushObject.getIntValue("uid"));
		pushModel.setName(pushObject.getString("name"));
		pushModel.setHead_image(pushObject.getString("head_image"));
		pushModel.setComment_content(pushObject.getString("comment_content"));
		pushModel.setType(jsonObject.getIntValue("type"));
		pushModel.setNews_id(pushObject.getIntValue("news_id"));
		pushModel.setNews_content(pushObject.getString("news_content"));
		pushModel.setNews_image(pushObject.getString("news_image"));
		pushModel.setNews_user_name(pushObject.getString("news_user_name"));
		pushModel.setIs_read(0);
		pushModel.setPush_time(pushObject.getString("push_time"));
		pushModel.setOwner(UserManager.getInstance().getUser().getUid());
		pushModel.save();
		
		//发送通知
		Intent newsPushIntent = new Intent(JLXCConst.BROADCAST_NEW_MESSAGE_PUSH);
		context.sendBroadcast(newsPushIntent);
		//徽标更新
		Intent tabIntent = new Intent(JLXCConst.BROADCAST_TAB_BADGE);
		context.sendBroadcast(tabIntent);
		//顶部更新
		Intent messageIntent = new Intent(JLXCConst.BROADCAST_MESSAGE_REFRESH);
		context.sendBroadcast(messageIntent);
	}
	
	//send msg to MainActivity
//	private void processCustomMessage(Context context, Intent intent) {
//	
////			intent.setAction(MainActivity.MESSAGE_RECEIVED_ACTION);
////			intent.addCategory(context.getPackageName());
////			context.sendBroadcast(intent);
//		
//	}
	
	
}
