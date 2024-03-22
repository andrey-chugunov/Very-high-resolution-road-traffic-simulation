package org.Rasterization;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShapeFile {

    public List<SimpleFeature> readRoadGeometries(String filePath) throws IOException {
        File file = new File(filePath);

        Map<String, Object> params = Map.of("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(params);

        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();

        List<SimpleFeature> roadGeometries = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                if (getNonPed(feature).equals("")) {
                    String streetName = getName(feature);
                    if (streetName != null) {
                        roadGeometries.add(feature);
                    }
                }
            }
        }

        return roadGeometries;
    }

    public String getShapeFileCrs(String filePath) throws IOException {
        File file = new File(filePath);
        URL url = file.toURI().toURL();
        ShapefileDataStore dataStore = new ShapefileDataStore(url);
        ContentFeatureSource featureSource = dataStore.getFeatureSource();

        SimpleFeatureType schema = featureSource.getSchema();
        return schema.getCoordinateReferenceSystem().toString();
    }

    public MultiLineString generateParallelMultiLineString(MultiLineString multiLine, double distance) {
        GeometryFactory geometryFactory = new GeometryFactory();
        List<LineString> parallelLines = new ArrayList<>();

        for (int i = 0; i < multiLine.getNumGeometries(); i++) {
            LineString line = (LineString) multiLine.getGeometryN(i);
            LineString parallelLine = (LineString) generateParallelLineString(line, distance);
            parallelLines.add(parallelLine);
        }

        return geometryFactory.createMultiLineString(parallelLines.toArray(new LineString[0]));
    }

    public Geometry generateParallelLineString(LineString line, double distance) {
        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = line.getCoordinateSequence();
        int numPoints = coordinateSequence.size();

        Coordinate[] parallelCoordinates = new Coordinate[numPoints];

        for (int i = 0; i < numPoints; i++) {
            Coordinate p = coordinateSequence.getCoordinate(i);

            double dx = 0.0;
            double dy = 0.0;

            if (i > 0 && i < numPoints - 1) {
                Coordinate p1 = coordinateSequence.getCoordinate(i - 1);
                Coordinate p2 = coordinateSequence.getCoordinate(i + 1);
                dx = p2.x - p1.x;
                dy = p2.y - p1.y;
            } else if (i == 0) {
                Coordinate p2 = coordinateSequence.getCoordinate(1);
                dx = p2.x - p.x;
                dy = p2.y - p.y;
            } else if (i == numPoints - 1) {
                Coordinate p1 = coordinateSequence.getCoordinate(numPoints - 2);
                dx = p.x - p1.x;
                dy = p.y - p1.y;
            }

            double length = Math.sqrt(dx * dx + dy * dy);
            double ux = dx / length;
            double uy = dy / length;

            double vx = -uy;
            double vy = ux;

            double vxScaled = vx * distance;
            double vyScaled = vy * distance;

            parallelCoordinates[i] = new Coordinate(p.x + vxScaled, p.y + vyScaled);
        }

        return geometryFactory.createLineString(parallelCoordinates);
    }



    public Double getRoadWidth(SimpleFeature feature) {
        Object streetWidthObj = feature.getAttribute("streetwidt");
        if (streetWidthObj != null) {
            return ((Number) streetWidthObj).doubleValue();
        } else {
            return null;
        }
    }

    public String getTrafficDirection(SimpleFeature feature) {
        Object trafficDirectionObj = feature.getAttribute("trafdir");
        if (trafficDirectionObj != null) {
            return ((String) trafficDirectionObj);
        } else {
            return null;
        }
    }

    public String getNonPed(SimpleFeature feature) {
        Object nonPedObj = feature.getAttribute("nonped");
        if (nonPedObj != null) {
            return ((String) nonPedObj);
        } else {
            return null;
        }
    }

    public String getName(SimpleFeature feature) {
        Object nameObj = feature.getAttribute("stname_lab");
        if (nameObj != null) {
            return ((String) nameObj);
        } else {
            return null;
        }
    }
}
