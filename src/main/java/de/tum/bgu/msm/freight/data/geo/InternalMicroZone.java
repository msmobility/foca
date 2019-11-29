package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InternalMicroZone implements Zone {

    private int id;

    public SimpleFeature getShapeFeature() {
        return shapeFeature;
    }

    private SimpleFeature shapeFeature;
    private Map<String, Double> attributes;
    private boolean isInStudyArea = true;

    public InternalMicroZone(int id, SimpleFeature shapeFeature) {
        this.id = id;
        this.shapeFeature = shapeFeature;
        this.attributes = new HashMap<>();
    }

    public void setAttribute(String attributeKey, double attributeValue){
       attributes.put(attributeKey,attributeValue);
    }

    public double getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public Coordinate getCoordinates(Random random) {
        return FreightFlowUtils.getRandomCoordinatesFromFeature(this.shapeFeature, random);
    }

    @Override
    public String getName() {
        return null;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public boolean isInStudyArea() {
        return isInStudyArea;
    }


}
