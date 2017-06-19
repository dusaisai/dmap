package com.wttech.lwpt.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;

public class City {
	@XStreamAlias("name")
	private String name;
	
	@XStreamAlias("lng")
	private String lng;
	
	@XStreamAlias("lat")
	private String lat;

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	

}
