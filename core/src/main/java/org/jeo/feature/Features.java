package org.jeo.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Features {

    /**
     * Retypes a feature object to a new schema.
     * <p>
     * This method works by "pulling" the attributes defined by the fields of {@link Schema} from 
     * the feature object. 
     * </p>
     * @param feature The original feature.
     * @param schema The schema to retype to.
     * 
     * @return The retyped feature.
     */
    public static Feature retype(Feature feature, Schema schema) {
        List<Object> values = new ArrayList<Object>();
        for (Field f : schema) {
            values.add(feature.get(f.getName()));
        }

        return new ListFeature(feature.getId(), values, schema);
    }

    /**
     * Copies values from one feature to another.
     * 
     * @param from THe source feature.
     * @param to The target feature.
     * 
     * @return The target feature.
     */
    public static Feature copy(Feature from, Feature to) {
        Field geom = from.schema().geometry();
        for (Map.Entry<String, Object> kv : from.map().entrySet()) {
            String key = kv.getKey();
            Object val = kv.getValue();

            if (geom != null && geom.getName().equals(key)) {
                to.put((Geometry)val);
            }
            else {
                to.put(kv.getKey(), val);
            }
        }
        return to;
    }

    /**
     * Creates a feature object from a map with an explicit schema.
     */
    public static MapFeature create(String id, Schema schema, Map<String, Object> map) {
        return new MapFeature(id, map, schema);
    }

    /**
     * Creates a feature object from a list with an explicit schema.
     */
    public static ListFeature create(String id, Schema schema, Object... values) {
        return new ListFeature(id, Arrays.asList(values), schema);
    }
}
