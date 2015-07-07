package com.jlxc.app.base.utils;

public interface JLXCConst {

	//	测试环境
	public static final String DOMIN = "http://192.168.1.100/jlxc_php/index.php/Home/MobileApi";
	public static final String ATTACHMENT_ADDR = "http://192.168.1.100/jlxc_php/Uploads/";	
	
	public static final int STATUS_SUCCESS = 1;// 接口返回成功
	public static final int STATUS_FAIL = 0;// 接口返回失败
	public static final String HTTP_MESSAGE = "message"; //返回值信息
	public static final String HTTP_RESULT = "result";// 返回值结果
	public static final String HTTP_STATUS = "status";// 返回值状态
	public static final String HTTP_LIST = "list";// 返回值列表

	public static final int PAGE_SIZE = 10;

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

	////////////////////////////////////////////////登录注册部分////////////////////////////////////////////////
	// 是否有该用户
	public static final String IS_USER = DOMIN + "/isUser";
	// 获取验证码
	public static final String GET_MOBILE_VERIFY = DOMIN + "/getMobileVerify";
	// 用户注册
	public static final String REGISTER_USER = DOMIN + "/registerUser";
	// 找回密码
	public static final String FIND_PWD = DOMIN + "/findPwd";
	// 用户登录
	public static final String LOGIN_USER = DOMIN + "/loginUser";	
	// 注册时填写个人信息
	public static final String SAVE_PERSONAL_INFO = DOMIN + "/savePersonalInfo";
	////////////////////////////////////////////////首页'说说'部分////////////////////////////////////////////////
	//发布状态
	public static final String PUBLISH_NEWS = DOMIN + "/publishNews";
	//状态新闻列表
	public static final String NEWS_LIST = DOMIN + "/newsList";
	//校园广场新闻列表
	public static final String SCHOOL_NEWS_LIST = DOMIN + "/schoolNewsList";
	//状态点赞列表
	public static final String GET_NEWS_LIKE_LIST = DOMIN + "/getNewsLikeList";
	//发送评论
	public static final String SEND_COMMENT = DOMIN + "/sendComment";
	//删除评论
	public static final String DELETE_COMMENT = DOMIN + "/deleteComment";
	//删除二级评论
	public static final String DELETE_SECOND_COMMENT = DOMIN + "/deleteSecondComment";
	//发送二级评论
	public static final String SEND_SECOND_COMMENT = DOMIN + "/sendSecondComment";
	//点赞或者取消赞
	public static final String LIKE_OR_CANCEL = DOMIN + "/likeOrCancel";
	//新闻详情
	public static final String NEWS_DETAIL = DOMIN + "/newsDetail";
	//浏览过该新闻的用户列表
	public static final String GET_NEWS_VISIT_LIST = DOMIN + "/getNewsVisitList";	
	////////////////////////////////////////////////个人信息////////////////////////////////////////////////
	//修改个人信息
	public static final String CHANGE_PERSONAL_INFORMATION = DOMIN + "/changePersonalInformation";
	//获取学校列表
	public static final String GET_SCHOOL_LIST = DOMIN + "/getSchoolList";
	//获取学校学生列表
	public static final String GET_SCHOOL_STUDENT_LIST = DOMIN + "/getSchoolStudentList";
	//修改学校
	public static final String CHANGE_SCHOOL = DOMIN + "/changeSchool";
	//设置HelloHaID
	public static final String SET_HELLOHAID = DOMIN + "/setHelloHaId";
	//获取用户二维码
	public static final String GET_USER_QRCODE = DOMIN + "/getUserQRCode";
	//修改个人信息中的图片 如背景图 头像
	public static final String CHANGE_INFORMATION_IMAGE = DOMIN + "/changeInformationImage";
	//个人信息中 获取最新动态的三张图片
	public static final String GET_NEWS_IMAGES = DOMIN + "/getNewsImages";
	//个人信息中 获取来访三张头像
	public static final String GET_VISIT_IMAGES = DOMIN + "/getVisitImages";
	//个人信息中 用户发布过的状态列表
	public static final String USER_NEWS_LIST = DOMIN + "/userNewsList";
	//个人信息 删除状态
	public static final String DELETE_NEWS = DOMIN + "/deleteNews";
	//个人信息 查看别人的信息
	public static final String PERSONAL_INFORMATION = DOMIN + "/personalInformation";
	//最近来访列表
	public static final String GET_VISIT_LIST = DOMIN + "/getVisitList";
	//删除来访
	public static final String DELETE_VISIT = DOMIN + "/deleteVisit";
	//别人的好友列表
	public static final String GET_OTHER_FRIENDS_LIST = DOMIN + "/getOtherFriendsList";
	//共同的好友列表
	public static final String GET_COMMON_FRIENDS_LIST = DOMIN + "/getCommonFriendsList";
	//举报用户
	public static final String REPORT_OFFENCE = DOMIN + "/reportOffence";
	
}
