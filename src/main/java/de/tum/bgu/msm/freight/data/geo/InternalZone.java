package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InternalZone implements Zone {

    private int id;
    private String name;
    private SimpleFeature shapeFeature;
    private Map<Integer, InternalMicroZone> microZones;
    private boolean isInStudyArea = false;


    public InternalZone(int id, String name) {
        this.id = id;
        this.name = name;
        this.microZones = new HashMap<>();
    }

    public SimpleFeature getShapeFeature() {
        return shapeFeature;
    }

    public void setShapeFeature(SimpleFeature shapeFeature) {
        this.shapeFeature = shapeFeature;
    }

    public Coordinate getCoordinates() {
        return Objects.requireNonNull(FreightFlowUtils.getRandomCoordinatesFromFeature(this.shapeFeature));
    }

    public void addMicroZone(InternalMicroZone microZone) {
        this.microZones.put(microZone.getId(), microZone);
    }



    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isInStudyArea() {
        return isInStudyArea;
    }

    public void setInStudyArea(boolean inStudyArea) {
        isInStudyArea = inStudyArea;
    }

    public Map<Integer, InternalMicroZone> getMicroZones() {
        return microZones;
    }
}


