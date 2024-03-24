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

    public List<LineString> generateParallelLineStrings(LineString line, double distance) {
        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = line.getCoordinateSequence();
        int numPoints = coordinateSequence.size();

        List<LineString> parallelLineStrings = new ArrayList<>();

        for (int i = 0; i < numPoints - 1; i++) {
            Coordinate p1 = coordinateSequence.getCoordinate(i);
            Coordinate p2 = coordinateSequence.getCoordinate(i + 1);

            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;

            double length = Math.sqrt(dx * dx + dy * dy);
            double ux = dx / length;
            double uy = dy / length;

            double vx = -uy;
            double vy = ux;

            double vxScaled = vx * distance;
            double vyScaled = vy * distance;

            Coordinate[] coordinates = {
                    new Coordinate(p1.x + vxScaled, p1.y + vyScaled),
                    new Coordinate(p2.x + vxScaled, p2.y + vyScaled)
            };

            parallelLineStrings.add(geometryFactory.createLineString(coordinates));
        }

        return parallelLineStrings;
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
