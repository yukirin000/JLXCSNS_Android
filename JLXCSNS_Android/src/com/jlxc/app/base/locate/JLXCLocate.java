package com.jlxc.app.base.locate;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

public abstract class JLXCLocate implements AMapLocationListener, Runnable {
	private Context mContext;
	private LocationManagerProxy aMapLocManager = null;
	private AMapLocation aMapLocation;
	private Handler handler = new Handler();
	// 经度
	private Double longitude = 0.0;
	// 纬度
	private Double latitude = 0.0;
	// 定位精确度（单位为米）
	private float accuracy = 0;
	// 定位采用的模式
	private String locateMode = "";
	// 城市编码
	private String cityCode = "";
	// 位置描述
	private String locationDescription = "";
	// 省
	private String provinceStr = "";
	// 市
	private String cityStr = "";
	// 区
	private String districtStr = "";
	// 区域编码
	private String regionCoding = "";

	public JLXCLocate(Context context) {
		mContext = context;
	}

	/**
	 * 初始化定位,通知时间（毫秒），通知距离（米），超时值（毫秒）
	 */
	public void locateInit(long minTime, float minDistance, int timeOutValue) {
		aMapLocManager = LocationManagerProxy.getInstance(mContext);
		aMapLocManager.requestLocationData(LocationProviderProxy.AMapNetwork,
				minTime, minDistance, this);
		handler.postDelayed(this, timeOutValue);
	}

	/**
	 * 混合定位回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation location) {
		if (location != null) {
			this.aMapLocation = location;// 判断超时机制
			longitude = location.getLatitude();// 获取纬度
			latitude = location.getLongitude();// 获取经度
			Bundle locBundle = location.getExtras();
			if (locBundle != null) {
				cityCode = locBundle.getString("citycode");
				locationDescription = locBundle.getString("desc");
			}
			accuracy = location.getAccuracy();
			locateMode = location.getProvider();
			provinceStr = location.getProvince();
			cityStr = location.getCity();
			districtStr = location.getDistrict();
			regionCoding = location.getAdCode();
			// 定位成功
			onLocateFinish(true);
		}
	}

	/***
	 * 定位失败或成功后调用此函数
	 */
	public abstract void onLocateFinish(boolean state);

	/**
	 * 停止定位并销毁对象
	 */
	public void stopLocation() {
		handler.removeCallbacks(this);
		if (aMapLocManager != null) {
			aMapLocManager.removeUpdates(this);
			aMapLocManager.destroy();
		}
		aMapLocManager = null;
	}

	@Override
	public void onLocationChanged(Location arg0) {

	}

	@Override
	public void onProviderDisabled(String arg0) {

	}

	@Override
	public void onProviderEnabled(String arg0) {

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

	}

	@Override
	public void run() {
		// 未定位成功则停止并销毁定位
		onLocateFinish(false);
		stopLocation();
	}

	/**
	 * 获取经度值
	 * */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * 获取纬度值
	 * */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * 获取经度（单位米）
	 * */
	public float getAccuracy() {
		return accuracy;
	}

	/**
	 * 获取定位模式
	 * */
	public String getLocateMode() {
		return locateMode;
	}

	/**
	 * 获取城市代码
	 * */
	public String getCityCode() {
		return cityCode;
	}

	/**
	 * 获取位置描述
	 * */
	public String getLocationDescription() {
		return locationDescription;
	}

	/**
	 * 获取所在的城市
	 * */
	public String getCityStr() {
		return cityStr;
	}

	/**
	 * 获取省
	 * */
	public String getProvinceStr() {
		return provinceStr;
	}

	/**
	 * 获取区域
	 * */
	public String getDistrictStr() {
		return districtStr;
	}

	/**
	 * 获取区域编码
	 * */
	public String getRegionCoding() {
		return regionCoding;
	}
}
