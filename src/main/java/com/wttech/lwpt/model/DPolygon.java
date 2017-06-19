package com.wttech.lwpt.model;


import com.vividsolutions.jts.geom.Coordinate;


/**
 * 点数据
 * @author wt0448
 *
 */
public class DPolygon extends BaseModel {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String code;
	
	private String remark;
	
	private String type;
	
	private Coordinate[] coordinate;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}


}
