package com.jlxc.app.base.utils;

public interface JLXCConst {

	//	测试环境
	public static final String DOMIN = "http://121.42.150.67/ubaby/";

	public static final int ROLE_ID_SHOUSHU_DOCTOR = 1;// 手术医生
	public static final int ROLE_ID_NURSE = 2;// 护士
	public static final int ROLE_ID_MENZHEN_DOCTOR = 3;// 门诊医生
	public static final int ROLE_ID_ADMINISTRATOR_DOCTOR = 4;// 管理员医生

	public static final int ROLE_ID_NORMAL_PATIENT = 1;// 普通患者
	public static final int ROLE_ID_VIP_PATIENT = 2;// VIP患者

	public static final int STATUS_SUCCESS = 1;// 接口返回成功
	public static final int STATUS_FAIL = 0;// 接口返回失败

	public static final int NOTI_ID_CHAT = 0;

	public static final int PAGE_SIZE = 10;
	// 电话
	public static final int TYPE_PHONE = 1;
	// 门诊
	public static final int TYPE_OUTPATIENT = 2;
	// 检查
	public static final int TYPE_CHECK = 3;
	// 造影
	public static final int TYPE_RADIOGRAPHY = 1;
	// 宫腔镜检查
	public static final int TYPE_HYSTEROSCOPY = 2;
	// 宫腔镜手术
	public static final int TYPE_SURGERY = 3;

	/**
	 * <pre>
	 * 资讯类型
	 * '1' => '医疗资讯'
	 * '2' => '活动信息'
	 * '3' => '专业资讯'
	 * '4' => '参考文献'
	 * </pre>
	 */
	public static final int INFO_TYPE_PATIENT = 1; // 患者端医疗资讯
	public static final int INFO_TYPE_ACTIVITY = 2; // 患者端活动信息
	public static final int INFO_TYPE_DOCTOR = 3; // 医生端专业资讯
	public static final int INFO_TYPE_REFERENCES = 4; // 医生端参考文献

	// 匹配网页
	public static final String URL_PATTERN = "[http|https]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*";
	// 匹配手机号
	public static final String PHONENUMBER_PATTERN = "1[3|4|5|7|8|][0-9]{9}";
	// 用户名匹配
	public static final String USER_ACCOUNT_PATTERN = "^[a-z0-9]{6,20}+$";
	// 匹配身份证号:15位 18位
	public static final String ID_CARD = "^\\d{15}|^\\d{17}([0-9]|X|x)$";
	// 姓名正则
	public static final String NAME_PATTERN = "([\u4e00-\u9fa5]{2,5})(&middot;[\u4e00-\u9fa5]{2,5})*";

	// 获取问答详情
	public static final String GET_QA_DETAIL = DOMIN + "index.php?r=mobile/api/QuestionDea";
	// 获取用户头像
	public static final String GET_USER_ICONPATH = DOMIN + "index.php?r=mobile/api/UserImage";
	// 上传聊天附件
	public static final String UPLOAD_CHAT_ATTACHMENT = DOMIN + "index.php?r=mobile/api/ImAttachment";
	// 获取随访列表（医生端）
	public static final String GET_FOLLOW_LIST_DOCTOR = DOMIN + "index.php?r=mobile/api/CustFollowList";
	// 获取随访列表（患者端）
	public static final String GET_FOLLOW_LIST_PATIENT = DOMIN + "index.php?r=mobile/api/FollowView";
	// 获取随访详情
	public static final String GET_FOLLOWDETAIL = DOMIN + "index.php?r=mobile/api/FollowDetail";
	// 患者端修改随访信息
	public static final String EDIT_FOLLOWDETAIL_PATIENT = DOMIN + "index.php?r=mobile/api/CustUpdateFollow";
	// 医生端修改随访信息
	public static final String EDIT_FOLLOWDETAIL_DOCTOR = DOMIN + "index.php?r=mobile/api/DrUpdateFollowContent";
	// 医生端完成本次随访
	public static final String COMPLETE_FOLLOWUP = DOMIN + "index.php?r=mobile/api/CompleteFollow";
	// 医生端 修改随访状态 终止或者恢复
	public static final String EDIT_MEDICAL_RECORD = DOMIN + "index.php?r=mobile/api/ChangeMedicalStatus";
	// 医生端 获取订单列表
	public static final String GET_RECORDER_DOCTOR = DOMIN + "index.php?r=mobile/api/QueryDrOrders";
	// 患者端 获取订单列表
	public static final String GET_RECORDER_PATIENT = DOMIN + "index.php?r=mobile/api/QueryCrOrders";
	// 获取是否需要更新
	public static final String GET_UPDATE = DOMIN + "index.php?r=mobile/api/Version";
	// 取消预约
	public static final String POST_CLRAR_APPOINTMENT = DOMIN + "index.php?r=mobile/api/CancelAppoint";
	// 获取私人医生列表接口
	public static final String GET_PRIVATE_DOCTOR_LIST = DOMIN + "index.php?r=mobile/api/PrivateDocList";
	//退款接口
	public static final String BACKMONEY = DOMIN + "index.php?r=mobile/api/Backmoney";

	/**
	 * 广播
	 */
	// 预约成功广播
	public static final String BROADCAST_BOOK_SUCCESS = "com.ubaby.broadcastreceiver.booksuccess";
	// 更新排班界面广播（排班修改后会发出该广播更新界面）
	public static final String BROADCAST_UPDATE_WORKTIME = "com.ubaby.broadcastreceiver.updateworktime";
	// 图文问题回答成功广播
	public static final String BROADCAST_ANSWERED_SUCCESS = "com.ubaby.broadcastreceiver.message.answeredsuccess";
	// 私人医生服务购买成功广播
	public static final String BROADCAST_PRIVATE_DOCTOR_PURCHASE_SUCCESS = "com.ubaby.broadcastreceiver.privatedoctor.purchase.success";
	// 图文咨询服务购买成功广播
	public static final String BROADCAST_RICHMSG_PURCHASE_SUCCESS = "com.ubaby.broadcastreceiver.richmsg.purchase.success";
	// 取消预约广播
	public static final String BROADCAST_CLEAR_APPOINTMENT = "com.ubaby.broadcastreceiver.clearappointment";
	//指派刷新广播
	public static final String BROADCAST_APPOINTDOCTOR = "com.ubaby.broadcastreceiver.appointdoctor";
	//电话指派刷新广播
	public static final String PHONE_BROADCAST = "com.ubaby.broadcastreceiver.phone";
	// 中心介绍
	public static final String URL_CENTER_INFO = DOMIN + "index.php?r=MobileArticle/mobile&id=102";
	// 关于优生宝
	public static final String URL_ABOUT_UBABY = DOMIN + "index.php?r=MobileArticle/mobile&id=101";
	//免责
	public static final String URL_LIABILITY_UBABY = DOMIN + "index.php?r=Disclaimer";

	/**
	 * Preferences
	 */
	// 是否记住密码
	public static final String PREFS_REMEMBER_PASSWORD = "RememberPassword";

}
