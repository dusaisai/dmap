package com.wttech.lwpt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.wttech.lwpt.model.City;
import com.wttech.lwpt.model.CityList;

@Component
public class CityUtil {
	private List<City>cityList=new ArrayList<City>();
	private Map<String, String>map=new HashMap<String, String>();
	
	@PostConstruct
	public void init(){
		XStream  xstream=new XStream(new DomDriver());
		xstream.processAnnotations(CityList.class);
		CityList routelist=(CityList) xstream.fromXML(this.getClass().getResourceAsStream("/CityList.xml"));
		this.cityList=routelist.getList();
		for(City city:cityList){
			map.put(city.getLng()+city.getLat(), city.getName());
		}
	}

	public List<City> getCityList() {
		return cityList;
	}

	public void setCityList(List<City> cityList) {
		this.cityList = cityList;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

}
