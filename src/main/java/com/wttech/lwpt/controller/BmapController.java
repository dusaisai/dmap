package com.wttech.lwpt.controller;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vividsolutions.jts.geom.Coordinate;
import com.wttech.lwpt.model.DPoint;
import com.wttech.lwpt.model.DPolygon;
import com.wttech.lwpt.model.DPolyline;
import com.wttech.lwpt.service.GeometryService;
import com.wttech.lwpt.util.CityUtil;
/**
 * 百度地图  提取高速
 * @author wt0448
 *
 */
@Controller
@RequestMapping("/bmap")
public class BmapController {
	Logger logger=LoggerFactory.getLogger(getClass());
	
	@Resource GeometryService geometryService;
	@Resource CityUtil cityUtil;
	final OkHttpClient client=new OkHttpClient();
//	@PostConstruct
	public void bmapline(){
		List<DPolyline> llist=new ArrayList<>();
		List<String> nameList=getCityName("湖北");
		for(String name:nameList){
			System.out.println(name);
			List<JSONObject> cityList=getPoisByCityName(name);
			for(JSONObject jsonobject:cityList){
				llist.addAll(getPolyLineFromBMap(jsonobject));
			}
		}
		geometryService.createPolyline("d://hefei_line_bmap"+".shp", llist);
	}
	
	private List<DPolyline> getPolyLineFromBMap(JSONObject jsonobject) {
		List<DPolyline> llist=new ArrayList<>();
		String uid=jsonobject.getString("uid");
		String name=jsonobject.getString("name");
		String address=jsonobject.getString("address");
		
		String url1="http://map.baidu.com/?newmap=1&reqflag=pcmap&biz=1&from=webmap&da_par=baidu&qt=ext&uid=%s&l=16&c=299&wd=%s&tn=B_NORMAL_MAP&nn=0&ie=utf-8";
		url1=String.format(url1, uid,name);
		try {
			Request request=new Request.Builder().url(url1).build();
			Response response = client.newCall(request).execute();
			String result=response.body().string();
			JSONObject jsonObject=JSONObject.parseObject(result);
			if(jsonObject.getJSONObject("content")==null){
				return llist;
			}

			String results=jsonObject.getJSONObject("content").getString("geo");
			String res=results.substring(results.lastIndexOf("|")+1);
			String []lines=res.split(";");
			for(String line:lines){
				DPolyline p1=new DPolyline();
				p1.setName(name);
				p1.setType("高速");
				p1.setRoadname(name);
				p1.setRoadnumber(address);
				String[]path=line.split(",");
				List<Coordinate> linelist=new ArrayList<>();
				for(int i=0,length=path.length;i<length;i=i+2){
					String covert="http://api.map.baidu.com/geoconv/v1/?coords=%s,%s&from=6&to=5&ak=210e4662fc6e2682a0bece4efa6534b4";
					Request covertrequest=new Request.Builder().url(String.format(covert, path[i],path[i+1])).build();
					Response covertresponse = client.newCall(covertrequest).execute();
					String covertresult=covertresponse.body().string();
					JSONObject covertjsonObject=JSONObject.parseObject(covertresult).getJSONArray("result").getJSONObject(0);
					linelist.add(new Coordinate(covertjsonObject.getDoubleValue("x"), covertjsonObject.getDoubleValue("y")));
				}
				p1.setCoordinate(linelist.toArray(new Coordinate[linelist.size()]));
				llist.add(p1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return llist;
	}

	private List<JSONObject> getPoisByCityName(String name) {
		List<JSONObject>list=new ArrayList<>();
		String url="http://api.map.baidu.com/place/v2/search?q=%s&region=%s&output=json&ak=210e4662fc6e2682a0bece4efa6534b4&scope=1&page_size=20&page_num=%s";
		try {
			String rurl=String.format(url,URLEncoder.encode("高速-道路", "utf-8"), URLEncoder.encode(name, "utf-8"),0);
			Request request=new Request.Builder().url(rurl).build();
			Response response = client.newCall(request).execute();
			String result= response.body().string();
			
			int total=JSONObject.parseObject(result).getIntValue("total");
			int pagesize=total%20==0?total/20:total%20+1;
			for(int j=0;j<=pagesize;j++){
				String pageurl=String.format(url,URLEncoder.encode("高速-道路", "utf-8"), URLEncoder.encode(name, "utf-8"),j);
				Request pagerequest=new Request.Builder().url(pageurl).build();
				Response pageresponse = client.newCall(pagerequest).execute();
				String pageresult= pageresponse.body().string();
				JSONArray jsonArray=JSONObject.parseObject(pageresult).getJSONArray("results");
				for(int i=0,length=jsonArray.size();i<length;i++ ){
					JSONObject jsonObject=jsonArray.getJSONObject(i);
					list.add(jsonObject);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List<String> getCityName(String name) {
		List<String>list=new ArrayList<>();
		String url="http://api.map.baidu.com/place/v2/search?q=%s&region=%s&output=json&ak=210e4662fc6e2682a0bece4efa6534b4&scope=1&page_size=20";
		try {
			String curl=String.format(url,URLEncoder.encode("高速", "utf-8"),URLEncoder.encode(name, "utf-8"));
			Request request=new Request.Builder().url(curl).build();
			Response response = client.newCall(request).execute();
			String result= response.body().string();
			
			JSONArray jsonArray=JSONObject.parseObject(result).getJSONArray("results");
			for(int i=0,length=jsonArray.size();i<length;i++ ){
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				list.add(jsonObject.getString("name"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private DPolygon getDPolygonFromObject(String section,JSONObject jsonObject) {
		List<Coordinate>list=new ArrayList<>();
		String name=jsonObject.getString("name");
		String adcode=jsonObject.getString("adcode");
		String level=jsonObject.getString("level");
		String citycode=jsonObject.getString("citycode");
		
		String[] coordinates=section.split(";");
		for(String coordinate:coordinates){
			String[] coord=coordinate.split(",");
			list.add(new Coordinate(Double.valueOf(coord[0]),Double.valueOf(coord[1])));
		}
		
		DPolygon polygon=new DPolygon();
		polygon.setName(name);
		polygon.setCode(adcode);
		polygon.setType(level);
		polygon.setRemark(citycode);
		polygon.setCoordinate(list.toArray(new Coordinate[list.size()]));
		return polygon;
	}

	private DPoint getDPointFromObject(JSONObject province) {
		String[] cooridate=province.getString("center").split(",");
		DPoint dpoint=new DPoint();
		dpoint.setName(province.getString("name"));
		dpoint.setType(province.getString("level"));
		dpoint.setCode(province.getString("adcode"));
		dpoint.setRemark(province.getString("citycode"));
		dpoint.setLng(Double.valueOf(cooridate[0]));
		dpoint.setLat(Double.valueOf(cooridate[1]));
		return dpoint;
	}

	@ResponseBody
	@RequestMapping("/district")
	public  void district(String  keywords){
		try {
			String words=URLEncoder.encode(keywords, "utf-8");
			String url="http://restapi.amap.com/v3/config/district?key=2cba4f41b3ba35a3614a09a5a7d28b2c&keywords=%s&subdistrict=2&showbiz=false&extensions=base"; 
			
			Request request=new Request.Builder().url(String.format(url, words)).build();
			Response response = client.newCall(request).execute();
			String result=response.body().string();
			
			Map<String, List<DPoint>> resMap=getLayerDataFromResult(result);
			createPointLayer(resMap);

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done!!!");
	}
	/**
	 * 创建点图层
	 * @param resMap
	 */
	private void createPointLayer(Map<String, List<DPoint>> resMap) {
		Set<String> set=resMap.keySet();
		for(String key:set){
			List<DPoint> dpointlist=resMap.get(key);
			createPolygonLayer(dpointlist);
			geometryService.createPoints("d://anhui//"+key+"_point.shp", dpointlist);
		}
	}

	/**
	 * 创建面图层
	 * @param dpointlist
	 */
	private void createPolygonLayer(List<DPoint> dpointlist) {
		List<DPolygon>provinceList=new ArrayList<>();

		try {
			for(DPoint dp:dpointlist){
				String url="http://restapi.amap.com/v3/config/district?key=2cba4f41b3ba35a3614a09a5a7d28b2c&keywords=%s&subdistrict=0&showbiz=false&extensions=all"; 
				Request request=new Request.Builder().url(String.format(url, URLEncoder.encode(dp.getCode(), "utf-8"))).build();
				Response response = client.newCall(request).execute();
				String result=response.body().string();
				JSONObject jsonObject=JSONObject.parseObject(result);
				if(jsonObject.get("info").equals("OK")){
					JSONArray provincejsonarray=(JSONArray) jsonObject.get("districts");
					for(int i=0,length=provincejsonarray.size();i<length;i++){
						JSONObject province=(JSONObject) provincejsonarray.get(i);
						if(province.get("polyline") instanceof String){
							String polyline=(String) province.get("polyline");
							String[]sections=polyline.split("\\|");
							for(String section:sections){
								DPolygon dpolygon=getDPolygonFromObject(section,province);
								provinceList.add(dpolygon);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(provinceList.size()>0){
			geometryService.createPolygon("d://anhui//"+dpointlist.get(0).getType()+"_polygon.shp", provinceList);
		}
	}


	/**
	 * 
	 * @param result
	 */
	private Map<String, List<DPoint>> getLayerDataFromResult(String result) {
		Map<String, List<DPoint>>resMap=new HashMap<String, List<DPoint>>();
		List<DPoint>provinceList=new ArrayList<DPoint>();
		List<DPoint>cityList=new ArrayList<DPoint>();
		List<DPoint>districtList=new ArrayList<DPoint>();
		
		JSONObject jsonObject=JSONObject.parseObject(result);
		if(jsonObject.get("info").equals("OK")){
			JSONArray provincejsonarray=(JSONArray) jsonObject.get("districts");
			for(int i=0,length=provincejsonarray.size();i<length;i++){
				JSONObject province=(JSONObject) provincejsonarray.get(i);
				DPoint provincePoint=getDPointFromObject(province);
				provinceList.add(provincePoint);
				
				JSONArray cityjsonarray=(JSONArray) province.get("districts");
				for(int j=0,len=cityjsonarray.size();j<len;j++){
					JSONObject city=(JSONObject) cityjsonarray.get(j);
					DPoint cityPoint=getDPointFromObject(city);
					cityList.add(cityPoint);
					
					JSONArray districtsjsonarray=(JSONArray) city.get("districts");
					for(int k=0,leng=districtsjsonarray.size();k<leng;k++){
						JSONObject district=(JSONObject) districtsjsonarray.get(k);
						DPoint districtPoint=getDPointFromObject(district);
						districtList.add(districtPoint);
					}
				}
			}
			if(provinceList.size()>0){
				resMap.put(provinceList.get(0).getType(), provinceList);
			}
			if(cityList.size()>0){
				resMap.put(cityList.get(0).getType(), cityList);
			}
			if(districtList.size()>0){
				resMap.put(districtList.get(0).getType(), districtList);
			}
		}else{
			logger.info("网络异常,无法连接网络！");
		}
		return resMap;
	}


	/**
	 * 返回经纬度对应的城市名称
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/citymap")
	public  Object citymap(){
		return cityUtil.getMap();
	}

	/**
	 * 返回经纬度对应的城市名称
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/anhui_polygon")
	public  void anhuipolygon(String points){
		List<DPolygon>provinceList=new ArrayList<>();
		JSONArray jsonArray= JSONObject.parseArray(points);
		List<Coordinate>list=new ArrayList<>();
		for(int i=0,length=jsonArray.size();i<length;i++){
			JSONObject jsonObject=(JSONObject) jsonArray.get(i);
			double lng=jsonObject.getDouble("lng");
			double lat=jsonObject.getDouble("lat");
			list.add(new Coordinate(lng,lat));
		}
		
		DPolygon polygon=new DPolygon();
		polygon.setName("");
		polygon.setCode("");
		polygon.setType("");
		polygon.setRemark("");
		polygon.setCoordinate(list.toArray(new Coordinate[list.size()]));
		provinceList.add(polygon);
		geometryService.createPolygon("d://anhui//anhui_polygon.shp", provinceList);

	}
}
