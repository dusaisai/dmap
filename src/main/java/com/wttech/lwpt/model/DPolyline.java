package com.wttech.lwpt.model;


import com.vividsolutions.jts.geom.Coordinate;


/**
 * 线数据
 * @author wt0448
 *
 */
public class DPolyline extends BaseModel {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String roadname;
	
	private String roadnumber;
	
	private String type;
	
	private Coordinate[]coordinate;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoadname() {
		return roadname;
	}

	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}

	public String getRoadnumber() {
		return roadnumber;
	}

	public void setRoadnumber(String roadnumber) {
		this.roadnumber = roadnumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Coordinate[] getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate[] coordinate) {
		this.coordinate = coordinate;
	}


}
