package com.wttech.lwpt.model;


/**
 * 点数据
 * @author wt0448
 *
 */
public class DPoint extends BaseModel {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String code;
	
	private String type;
	
	private Double lng;
	
	private Double lat;

	private String remark;
	
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

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
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
