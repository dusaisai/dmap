package com.wttech.lwpt.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.wttech.lwpt.model.DPoint;


/**
 * 高德地图数据接口
 * 提取点和行政区划面数据
 * @author wt0448
 *
 */
public interface AmapService {

	/**
	 * 获取省份名称与编码
	 * @return
	 */
	List<JSONObject>getCityList();
	/**
	 * 获取城市区域面和城市中心点图层数据
	 * 省面 省点
	 * 市面 市点
	 * 县面 县点
	 */
	Map<String, List<DPoint>> getCityPolygonAndPointData(String codeOrName);
	
	/**
	 * 创建图层数据
	 * 省面 省点
	 * 市面 市点
	 * 县面 县点
	 */
	void createCityPolygonAndPointLayer(Map<String, List<DPoint>> resMap,String provincename);
	/**
	 *创建点图层
	 * @param types
	 * @param city
	 * @param layername
	 */
	void createPointLayer(String types,String province,String city,String layername);
	
	
	
}
