package com.wttech.lwpt.service.impl;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.wttech.lwpt.model.BaseModel;
import com.wttech.lwpt.model.DPoint;
import com.wttech.lwpt.model.DPolygon;
import com.wttech.lwpt.model.DPolyline;
import com.wttech.lwpt.service.GeometryService;
import com.wttech.lwpt.util.Constant;

@Component
public class GeometryServiceImpl implements GeometryService {

	@Override
	public void createPoints(String path, List<? extends BaseModel> list) {
		try {
			SimpleFeatureType TYPE_POINT =getFeatureTypeByData(Constant.POINT);
	        List<SimpleFeature> features = new ArrayList<>();
	        
	        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE_POINT);
	        
	        for(Object dp:list){
	        	DPoint p=(DPoint) dp;
	        	
	        	Point point = geometryFactory.createPoint(new Coordinate(p.getLng(), p.getLat()));
	        	featureBuilder.add(point);
	        	featureBuilder.add(p.getName());
	        	featureBuilder.add(p.getCode());
	        	featureBuilder.add(p.getRemark());
	        	featureBuilder.add(p.getType());
	        	featureBuilder.add(p.getLng());
	        	featureBuilder.add(p.getLat());
	        	SimpleFeature feature = featureBuilder.buildFeature(null);
	        	features.add(feature);
	        }
	        
	        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

	        Map<String, Serializable> params = new HashMap<>();
	        File file=new File(path.substring(0,path.lastIndexOf("/")));
	        if(!file.exists()){
	        	file.mkdirs();
	        }else{
	        	file.delete();
	        	file.mkdirs();
	        }
	        params.put("url", new File(path).toURI().toURL());
	        
	        params.put("create spatial index", Boolean.TRUE);

	        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
	        newDataStore.setCharset(Charset.forName("utf-8"));
	        newDataStore.createSchema(TYPE_POINT);
	        
	        Transaction transaction = new DefaultTransaction("create");

	        String typeName = newDataStore.getTypeNames()[0];
	        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
	        
	        if (featureSource instanceof SimpleFeatureStore) {
	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
	            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE_POINT, features);
	            featureStore.setTransaction(transaction);
	            try {
	                featureStore.addFeatures(collection);
	                transaction.commit();
	            } catch (Exception problem) {
	                problem.printStackTrace();
	                transaction.rollback();
	            } finally {
	                transaction.close();
	            }
	        } 
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private SimpleFeatureType getFeatureTypeByData(String type){
		try {
			SimpleFeatureType featureType=null;
			switch (type) {
			case "POINT":
				featureType = DataUtilities.createType("Location",
				        "the_geom:Point:srid=4326," +
				        "name:String," +   
				        "code:String,"+  
				        "remark:String,"+
				        "type:String,"+
				        "lng:Double,"+
				        "lat:Double"
				);
				break;
			case "MUILTILINESTRING":
				featureType = DataUtilities.createType("Location",
				        "the_geom:LineString:srid=4326," +
				        "name:String," +   
				        "code:String,"+  
				        "remark:String,"+
				        "type:String,"+
				        "startstake:Double,"+
				        "endstake:Double"
				);
				break;
			case "MULTIPOLYGON":
				featureType = DataUtilities.createType("Location",
				        "the_geom:MultiPolygon:srid=4326," +
				        "name:String," +   
				        "code:String,"+  
				        "remark:String,"+
				        "type:String"
				);
				break;

			default:
				featureType = DataUtilities.createType("Location",
				        "the_geom:Point:srid=4326," + 
				        "name:String," +   
				        "roadname:String,"+  
				        "roadnumber:String"
				);
				break;
			}
		
			
			return featureType;
		} catch (SchemaException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void createPolyline(String path, List<? extends BaseModel> list) {
		try {
			SimpleFeatureType TYPE_MUILTILINESTRING =getFeatureTypeByData(Constant.MUILTILINESTRING);
	        List<SimpleFeature> features = new ArrayList<>();
	        
	        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE_MUILTILINESTRING);
	        
	        for(Object dline:list){
	        	DPolyline line=(DPolyline) dline;
	        	List<LineString>lineStringList=new ArrayList<>();
        		Coordinate[] coords  =line.getCoordinate();
        		LineString lineString = geometryFactory.createLineString(coords);
        		lineStringList.add(lineString);
	        	MultiLineString multiline=geometryFactory.createMultiLineString(lineStringList.toArray(new LineString[lineStringList.size()]));
	        	
	        	featureBuilder.add(multiline);
	        	featureBuilder.add(line.getName());
	        	featureBuilder.add(line.getRoadname());
	        	featureBuilder.add(line.getRoadnumber());
	        	featureBuilder.add(line.getType());
	        	SimpleFeature feature = featureBuilder.buildFeature(null);
	        	features.add(feature);
	        }
	        
	        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

	        Map<String, Serializable> params = new HashMap<>();
	        File file=new File(path.substring(0,path.lastIndexOf("/")));
	        if(!file.exists()){
	        	file.mkdirs();
	        }else{
	        	file.delete();
	        	file.mkdirs();
	        }
	        params.put("url", new File(path).toURI().toURL());
	        
	        params.put("create spatial index", Boolean.TRUE);

	        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
	        newDataStore.setCharset(Charset.forName("utf-8"));
	        newDataStore.createSchema(TYPE_MUILTILINESTRING);
	        
	        Transaction transaction = new DefaultTransaction("create");

	        String typeName = newDataStore.getTypeNames()[0];
	        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
	        
	        if (featureSource instanceof SimpleFeatureStore) {
	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
	            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE_MUILTILINESTRING, features);
	            featureStore.setTransaction(transaction);
	            try {
	                featureStore.addFeatures(collection);
	                transaction.commit();
	            } catch (Exception problem) {
	                problem.printStackTrace();
	                transaction.rollback();
	            } finally {
	                transaction.close();
	            }
	        } 
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createPolygon(String path, List<? extends BaseModel> list) {
		try {
			SimpleFeatureType TYPE_MULTIPOLYGON=getFeatureTypeByData(Constant.MULTIPOLYGON);
	        List<SimpleFeature> features = new ArrayList<>();
	        
	        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE_MULTIPOLYGON);
	        
	        for(Object dpolygon:list){
	        	DPolygon polygon=(DPolygon) dpolygon;
	        	List<Polygon>ppolygonList=new ArrayList<>();
        		Coordinate[] coords =polygon.getCoordinate();
        		Polygon ppolygon = geometryFactory.createPolygon(coords);
        		ppolygonList.add(ppolygon);
	        	MultiPolygon multipolygon=geometryFactory.createMultiPolygon(ppolygonList.toArray(new Polygon[ppolygonList.size()]));
	        	
	        	featureBuilder.add(multipolygon);
	        	featureBuilder.add(polygon.getName());
	        	featureBuilder.add(polygon.getCode());
	        	featureBuilder.add(polygon.getRemark());
	        	featureBuilder.add(polygon.getType());
	        	SimpleFeature feature = featureBuilder.buildFeature(null);
	        	features.add(feature);
	        }
	        
	        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

	        Map<String, Serializable> params = new HashMap<>();
	        File file=new File(path.substring(0,path.lastIndexOf("/")));
	        if(!file.exists()){
	        	file.mkdirs();
	        }else{
	        	file.delete();
	        	file.mkdirs();
	        }
	        params.put("url", new File(path).toURI().toURL());
	        
	        params.put("create spatial index", Boolean.TRUE);

	        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
	        newDataStore.setCharset(Charset.forName("utf-8"));
	        newDataStore.createSchema(TYPE_MULTIPOLYGON);
	        
	        Transaction transaction = new DefaultTransaction("create");

	        String typeName = newDataStore.getTypeNames()[0];
	        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
	        
	        if (featureSource instanceof SimpleFeatureStore) {
	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
	            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE_MULTIPOLYGON, features);
	            featureStore.setTransaction(transaction);
	            try {
	                featureStore.addFeatures(collection);
	                transaction.commit();
	            } catch (Exception problem) {
	                problem.printStackTrace();
	                transaction.rollback();
	            } finally {
	                transaction.close();
	            }
	        } 
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
