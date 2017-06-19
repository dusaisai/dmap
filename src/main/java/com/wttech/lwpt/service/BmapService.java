package com.wttech.lwpt.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.wttech.lwpt.model.DPolyline;


/**
 * 百度地图数据接口
 * 提取高速路线信息
 * @author wt0448
 *
 */
public interface BmapService {
	/**
	 * 获取地市名称列表
	 * @param provincename
	 * @return
	 */
	List<String> getCityName(String provinceName);
	
	/**
	 * 获取城市内高速路网的poi数据
	 * @param cityName
	 * @return
	 */
	List<JSONObject> getPoisByCityName(String cityName);
	
	/**
	 * 获取高度内路网数据
	 * @param jsonobject
	 * @return
	 */
	List<DPolyline> getPolyLineFromBMap(JSONObject jsonobject);
	
	/**
	 * 创建线图层
	 * @param list
	 */
	void createPolylineLayer(List<DPolyline> list,String provinceName);

}
