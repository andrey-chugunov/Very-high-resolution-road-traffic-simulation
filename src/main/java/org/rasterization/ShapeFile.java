package org.rasterization;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
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
                roadGeometries.add(feature);
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

    public Double getRoadWidth(SimpleFeature feature) {
        Object streetWidthObj = feature.getAttribute("streetwidt");

        if (streetWidthObj != null && streetWidthObj instanceof Number) {
            return ((Number) streetWidthObj).doubleValue();
        } else {
            // Обработка случая, когда значение отсутствует
            return null;
        }
    }
}
