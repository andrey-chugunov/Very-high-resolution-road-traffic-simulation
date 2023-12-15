package org.rasterization;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
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
}
