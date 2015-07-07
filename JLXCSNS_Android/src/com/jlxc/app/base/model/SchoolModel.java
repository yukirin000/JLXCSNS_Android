package com.jlxc.app.base.model;

public class SchoolModel {

	// 初中
	public static final int JUNIOR_MIDDLE_SCHOOL = 0;
	// 高中
	public static final int SENIOR_MIDDLE_SCHOOL = 1;

	// 学校的名字
	private String schoolName;
	// 学校的区域
	private String schoolLocation;
	// 学校的区域
	private int schoolType;

	public SchoolModel(String name, String location, int type) {
		setSchoolName(name);
		setSchoolLocation(location);
		setSchoolType(type);
	}

	// 获取学校的名称
	public String getSchoolName() {
		return schoolName;
	}

	// 设置学校名字
	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	// 获取学校的区域
	public String getSchoolLocation() {
		return schoolLocation;
	}

	// 设置学校的区域
	public void setSchoolLocation(String schoolLocation) {
		this.schoolLocation = schoolLocation;
	}

	// 获取学校类型
	public int getSchoolType() {
		return schoolType;
	}

	// 设置学校类型
	public void setSchoolType(int schoolType) {
		this.schoolType = schoolType;
	}

}
