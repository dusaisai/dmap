package com.wttech.lwpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
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
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;



public class LWPTTask {

	
	public static void main(String[] args) throws Exception  {
		final SimpleFeatureType TYPE_POINT = DataUtilities.createType("Location",
		        "the_geom:Point:srid=4326," + // <- the geometry attribute: Point type
		        "name:String," +   // <- a String attribute
		        "number:Integer"   // a number attribute
		);
		final SimpleFeatureType TYPE_POLYLINE = DataUtilities.createType("Location",
		        "the_geom:MuiltiLineString:srid=4326," + // <- the geometry attribute: Point type
		        "name:String," +   // <- a String attribute
		        "number:Integer"   // a number attribute
		);
		
        System.out.println("TYPE:"+TYPE_POINT);
        
        List<SimpleFeature> features = new ArrayList<>();
        
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//        geometryFactory.createLineString(coordinates)

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE_POINT);
        Point point = geometryFactory.createPoint(new Coordinate(117, 32));

        featureBuilder.add(point);
        featureBuilder.add("合肥");
        featureBuilder.add(123);
        SimpleFeature feature = featureBuilder.buildFeature(null);
        features.add(feature);
        File newFile = getNewShapeFile();

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.setCharset(Charset.forName("utf-8"));
        newDataStore.createSchema(TYPE_POINT);
        
        /*
         * Write the features to the shapefile
         */
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        /*
         * The Shapefile format has a couple limitations:
         * - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
         * - Attribute names are limited in length 
         * - Not all data types are supported (example Timestamp represented as Date)
         * 
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */

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
            System.exit(0); // success!
        } else {
            System.exit(1);
        }
	}
    private static File getNewShapeFile() {
        String newPath =  "d://hefei.shp";

        return new File(newPath);
    }

}
