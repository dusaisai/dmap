package com.wttech.lwpt.service.impl;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vividsolutions.jts.geom.Coordinate;
import com.wttech.lwpt.model.DPoint;
import com.wttech.lwpt.model.DPolygon;
import com.wttech.lwpt.service.AmapService;
import com.wttech.lwpt.service.GeometryService;
@Service
@PropertySource("classpath:env.properties")
public class AmapServiceImpl implements AmapService {
	
	Logger logger=LoggerFactory.getLogger(getClass());
	
	@Resource Environment env;
	@Resource GeometryService geometryService;
	final OkHttpClient client=new OkHttpClient();

	@PostConstruct
	@Override
	public List<JSONObject> getCityList() {
		List<JSONObject> list=new ArrayList<JSONObject>();
		String amap_province=env.getProperty("AMAP_PROVINCE", String.class);
		try {
			Request request=new Request.Builder().url(amap_province).build();
			Response response = client.newCall(request).execute();
			String result=response.body().string();
			JSONArray jsonResult=JSONObject.parseObject(result).getJSONArray("districts");
			for(int i=0,length=jsonResult.size();i<length;i++){
				JSONArray countryArray=jsonResult.getJSONObject(i).getJSONArray("districts");
				for(int j=0,len=countryArray.size();j<len;j++){
					JSONObject jsonobject=countryArray.getJSONObject(j);
					list.add(jsonobject);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public Map<String, List<DPoint>> getCityPolygonAndPointData(String codeOrName) {
		try {
			String words=URLEncoder.encode(codeOrName, "utf-8");
			String url=env.getProperty("AMAP_CITY", String.class); 
			
			Request request=new Request.Builder().url(String.format(url, words)).build();
			Response response = client.newCall(request).execute();
			String result=response.body().string();
			
			return getLayerDataFromResult(result);

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
	 * 获取点图层数据
	 * @param province
	 * @return
	 */
	private DPoint getDPointFromObject(JSONObject province) {
		DPoint dpoint=new DPoint();
		
		dpoint.setName(province.getString("name"));
		dpoint.setType(province.getString("level"));
		dpoint.setCode(province.getString("adcode"));
		dpoint.setRemark(province.getString("citycode"));
		
		String[] cooridate=province.getString("center").split(",");
		dpoint.setLng(Double.valueOf(cooridate[0]));
		dpoint.setLat(Double.valueOf(cooridate[1]));
		
		return dpoint;
	}
	/**
	 * 创建面图层
	 * @param dpointlist
	 */
	private void createPolygonLayer(List<DPoint> dpointlist,String provinceName) {
		List<DPolygon>provinceList=new ArrayList<>();

		try {
			for(DPoint dp:dpointlist){
				String url=env.getProperty("AMAP_CITY_BORDER"); 
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
			String savepath=env.getProperty("SAVE_PATH", String.class);
			geometryService.createPolygon(savepath+provinceName+"//"+dpointlist.get(0).getType()+"_面.shp", provinceList);
		}
	}
	/**
	 * 面图层数据
	 * @param section
	 * @param jsonObject
	 * @return
	 */
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

	@Override
	public void createCityPolygonAndPointLayer(Map<String, List<DPoint>> resMap,String provincename) {
		Set<String> set=resMap.keySet();
		for(String key:set){
			List<DPoint> dpointlist=resMap.get(key);
			createPolygonLayer(dpointlist,provincename);
			String savepath=env.getProperty("SAVE_PATH", String.class);
			geometryService.createPoints(savepath+provincename+"//"+key+"_点.shp", dpointlist);
		}
	}

	@Override
	public void createPointLayer(String types,String province, String city, String layername) {
		List<DPoint>list=new ArrayList<>();
		try {
			String url=env.getProperty("AMAP_TEXT"); 
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
			String savepath=env.getProperty("SAVE_PATH", String.class);
			geometryService.createPoints(savepath+province+"//"+layername+"_点.shp", list);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
