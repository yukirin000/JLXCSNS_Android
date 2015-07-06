package com.jlxc.app.base.utils;

public interface JLXCConst {

	//	测试环境
	public static final String DOMIN = "http://192.168.1.101/jlxc_php/index.php/Home/MobileApi";
	public static final String ATTACHMENT_ADDR = "http://192.168.1.101/jlxc_php/Uploads/";	
	
	public static final int STATUS_SUCCESS = 1;// 接口返回成功
	public static final int STATUS_FAIL = 0;// 接口返回失败

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

}
