package com.wttech.lwpt.service;

import java.util.List;

import com.wttech.lwpt.model.BaseModel;


public interface GeometryService  {
	/**
	 * 点图层
	 * @param path 保存路径
	 * @param list  空间数据
	 */
	void createPoints(String path,List<? extends BaseModel> list);
	
	/**
	 * 线数据
	 * @param path
	 * @param list
	 */
	void createPolyline(String path,List<? extends BaseModel> list);
	
	/**
	 * 面数据
	 * @param path
	 * @param list
	 */
	void createPolygon(String path,List<? extends BaseModel> list);

}
