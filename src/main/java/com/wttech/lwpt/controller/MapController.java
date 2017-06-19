package com.wttech.lwpt.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.wttech.lwpt.model.DPoint;
import com.wttech.lwpt.model.DPolyline;
import com.wttech.lwpt.service.AmapService;
import com.wttech.lwpt.service.BmapService;

@Controller
@RequestMapping("/map")
public class MapController {
	@Resource AmapService amapService;
	@Resource BmapService bmapService;
	
	@PostConstruct
	public void init(){
		List<JSONObject> list=amapService.getCityList();
//		for(JSONObject jsonobject:list){
			//创建区域点和面图层
			String provinceCode="340000";//jsonobject.getString("adcode");
			String provinceName="安徽";//jsonobject.getString("name");
			Map<String, List<DPoint>> map=amapService.getCityPolygonAndPointData(provinceCode);
			amapService.createCityPolygonAndPointLayer(map, provinceName);
			List<DPolyline> llist=new ArrayList<>();

			List<String>nameList=bmapService.getCityName(provinceName);
			for(String name:nameList){
				List<JSONObject> poiList=bmapService.getPoisByCityName(name);
				for(JSONObject poi:poiList){
					List<DPolyline> dlineList=bmapService.getPolyLineFromBMap(poi);
					llist.addAll(dlineList);
				}
			}
			bmapService.createPolylineLayer(llist,provinceName);
			
			amapService.createPointLayer("180200",provinceName,provinceName,"收费站");//收费站
			amapService.createPointLayer("180300",provinceName,provinceName,"服务区");//高速服务区
			amapService.createPointLayer("180500",provinceName,provinceName,"路牌信息");//路牌信息
			amapService.createPointLayer("180101",provinceName,provinceName,"摄像头");//摄像头
			amapService.createPointLayer("010100",provinceName,provinceName,"加油站");//收费站
			amapService.createPointLayer("190310",provinceName,provinceName,"隧道");//收费站
			amapService.createPointLayer("190304",provinceName,provinceName,"高速出口");//高速路出口
			amapService.createPointLayer("190305",provinceName,provinceName,"高速入口");//高速路入口
//		}
		
	}

}
