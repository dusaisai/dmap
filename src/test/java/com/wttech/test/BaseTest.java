package com.wttech.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import com.vividsolutions.jts.geom.Coordinate;
import com.wttech.lwpt.model.DPoint;
import com.wttech.lwpt.model.DPolyline;
import com.wttech.lwpt.service.GeometryService;
import com.wttech.lwpt.service.impl.GeometryServiceImpl;
import com.wttech.lwpt.util.GPSUtils;


/**
 * 业务对象单元测试基类。
 * 
 * 
 */
// AbstractTransactionalJUnit4SpringContextTests

//@ContextConfiguration(locations = { "classpath*:applicationContext-test.xml" }) extends AbstractJUnit4SpringContextTests
public abstract class BaseTest  {
	public static void main1(String[] args) {
		List<DPolyline> list=new ArrayList<>();
		List<Coordinate> linelist=new ArrayList<>();
		DPolyline p1=new DPolyline();
		p1.setName("测试收费站");
		p1.setType("收费站");
		p1.setRoadname("绕城高速");
		p1.setRoadnumber("G4001");
		linelist.add(new Coordinate(117d, 32d));
		linelist.add(new Coordinate(116d, 31d));
		
		List<Coordinate[]> coorlist=new ArrayList<>();
		coorlist.add(linelist.toArray(new Coordinate[1]));
//		p1.setList(coorlist);
		list.add(p1);
		GeometryService service=new GeometryServiceImpl();
//		service.createPolyline("d://hefei_line.shp", list);
		BigDecimal lng=GPSUtils.WebMercator2Lat(new BigDecimal(3443980.79));
		BigDecimal lat=GPSUtils.WebMercator2Lng(new BigDecimal(13054826.97));
		
		System.out.println(lng.doubleValue()+"-----"+lat.doubleValue());
	}
	
}