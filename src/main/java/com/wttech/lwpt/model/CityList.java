package com.wttech.lwpt.model;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


@XStreamAlias("citylist")
public class CityList {
	
	@XStreamImplicit(itemFieldName="city")
	private List<City>list;

	public List<City> getList() {
		return list;
	}

	public void setList(List<City> list) {
		this.list = list;
	}

}
