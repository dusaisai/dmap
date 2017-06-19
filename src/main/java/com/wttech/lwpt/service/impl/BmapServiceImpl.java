package com.wttech.lwpt.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
import com.wttech.lwpt.model.DPolyline;
import com.wttech.lwpt.service.BmapService;
import com.wttech.lwpt.service.GeometryService;

@Service
@PropertySource("classpath:env.properties")
public class BmapServiceImpl implements BmapService {
	Logger logger=LoggerFactory.getLogger(getClass());
	
	@Resource Environment env;
	@Resource GeometryService geometryService;
	final OkHttpClient client=new OkHttpClient();

	@Override
	public List<String> getCityName(String provincename) {
		List<String>list=new ArrayList<>();
		String url=env.getProperty("BMAP_CITY",String.class);
		try {
			String curl=String.format(url,URLEncoder.encode("高速", "utf-8"),URLEncoder.encode(provincename, "utf-8"));
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

	@Override
	public List<JSONObject> getPoisByCityName(String cityName) {
		List<JSONObject>list=new ArrayList<>();
		String url=env.getProperty("BMAP_CITY_POI", String.class);
		try {
			String rurl=String.format(url,URLEncoder.encode("高速-道路", "utf-8"), URLEncoder.encode(cityName, "utf-8"),0);
			Request request=new Request.Builder().url(rurl).build();
			Response response = client.newCall(request).execute();
			String result= response.body().string();
			
			int total=JSONObject.parseObject(result).getIntValue("total");
			int pagesize=total%20==0?total/20:total%20+1;
			for(int j=0;j<=pagesize;j++){
				String pageurl=String.format(url,URLEncoder.encode("高速-道路", "utf-8"), URLEncoder.encode(cityName, "utf-8"),j);
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

	@Override
	public List<DPolyline> getPolyLineFromBMap(JSONObject jsonobject) {
		List<DPolyline> llist=new ArrayList<>();
		String uid=jsonobject.getString("uid");
		String name=jsonobject.getString("name");
		String address=jsonobject.getString("address");
		
		String url1=env.getProperty("BMAP_CITY_LINE", String.class);
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
					String covert=env.getProperty("BMAP_COORD_CONVERT", String.class);
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

	@Override
	public void createPolylineLayer(List<DPolyline> list,String provincename) {
		String savePath= env.getProperty("SAVE_PATH", String.class);
		savePath=savePath+provincename+"//"+"高速.shp";
		geometryService.createPolyline(savePath, list);
	}

}
