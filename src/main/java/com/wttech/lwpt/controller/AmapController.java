package com.wttech.lwpt.controller;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
 * 高德地图
 * @author wt0448
 *
 */
@Controller
@RequestMapping("/amap")
public class AmapController {
	Logger logger=LoggerFactory.getLogger(getClass());
	
	@Resource GeometryService geometryService;
	@Resource CityUtil cityUtil;
	final OkHttpClient client=new OkHttpClient();
	public static final String TYPECODE="190300190301";
	/**
	 * 根据省名称或者编码获取列表
	 * @param name
	 */
	private List<String> getCitysByProvinceCodeOrName(String name) {
		List<String>list=new ArrayList<>();
		String url="http://restapi.amap.com/v3/config/district?key=2cba4f41b3ba35a3614a09a5a7d28b2c&keywords=%s&subdistrict=2&showbiz=false&extensions=base";
		try {
			Request request=new Request.Builder().url(String.format(url,URLEncoder.encode(name,"utf-8"))).build();
			Response response = client.newCall(request).execute();
			String result=response.body().string();
			JSONArray jsonResult=JSONObject.parseObject(result).getJSONArray("districts");
			for(int i=0,length=jsonResult.size();i<length;i++){
				JSONArray cityArray=jsonResult.getJSONObject(i).getJSONArray("districts");
				for(int j=0,len=cityArray.size();j<len;j++){
					JSONArray distinctArray=cityArray.getJSONObject(j).getJSONArray("districts");
					for(int k=0,l=distinctArray.size();k<l;k++){
						String code=distinctArray.getJSONObject(k).getString("adcode");
						list.add(code);
					}
				}
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
			createCityPointLayer(resMap);
			createPointLayer("180200",keywords,"fee");//收费站
			createPointLayer("180300",keywords,"service");//高速服务区
			createPointLayer("180500",keywords,"signs");//路牌信息
			createPointLayer("180101",keywords,"camera");//摄像头
			createPointLayer("010100",keywords,"gasstation");//收费站
			createPointLayer("190310",keywords,"tunnel");//收费站
			createPointLayer("190304",keywords,"highway_out");//高速路出口
			createPointLayer("190305",keywords,"highway_in");//高速路入口

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done!!!");
	}
	private void createPointLayer(String types,String city,String layername) {
		List<DPoint>list=new ArrayList<>();
		try {
			String url="http://restapi.amap.com/v3/place/text?key=2cba4f41b3ba35a3614a09a5a7d28b2c&keywords=&types=%s&city=%s&extensions=base&offset=20&page=%s"; 
			Request request=new Request.Builder().url(String.format(url, types,city,1)).build();
			Response response= client.newCall(request).execute();
			String result=response.body().string();
			int cout=JSONObject.parseObject(result).getInteger("count");
			int pagesize=(cout%20==0)?cout/20:cout/20+1;
			for(int i=1;i<=pagesize;i++){
				Request pointrequest=new Request.Builder().url(String.format(url, types,city,i)).build();
				Response pointresponse= client.newCall(pointrequest).execute();
				String pointresult=pointresponse.body().string();
				JSONArray jsonArray=JSONObject.parseObject(pointresult).getJSONArray("pois");
				if(null==jsonArray){
					continue;
				}
				for(int j=0,length=jsonArray.size();j<length;j++){
					JSONObject jsonObject=jsonArray.getJSONObject(j);
					String[] coord=jsonObject.getString("location").split(",");
					DPoint dp=new DPoint();
					dp.setName(jsonObject.getString("name"));
					dp.setType(jsonObject.getString("typecode"));
					dp.setRemark(jsonObject.getString("cityname")+jsonObject.getString("adname"));
					dp.setLng(Double.valueOf(coord[0]));
					dp.setLat(Double.valueOf(coord[1]));
					list.add(dp);
				}
			}
			geometryService.createPoints("d://anhui//"+layername+"_point.shp", list);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 创建点图层
	 * @param resMap
	 */
	private void createCityPointLayer(Map<String, List<DPoint>> resMap) {
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
