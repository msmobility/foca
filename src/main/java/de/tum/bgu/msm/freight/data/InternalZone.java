package de.tum.bgu.msm.freight.data;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.matsim.api.core.v01.Coord;
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

    public Coord getCoordinates(Commodity commodity) {
        if (microZones.isEmpty() || commodity == null) {
            return Objects.requireNonNull(FreightFlowUtils.getRandomCoordinatesFromFeature(this.shapeFeature));
        } else {
            int microZoneId = disaggregateToMicroZone(commodity);
            Coord coord =  microZones.get(microZoneId).getCoordinates(commodity);
            return Objects.requireNonNull(coord);
        }

    }

    public void addMicroZone(InternalMicroZone microZone){
        this.microZones.put(microZone.getId(), microZone);
    }

    private int disaggregateToMicroZone(Commodity commodity) {
        Map<Integer, Double> microZonesProbabilities = new HashMap<>();
        this.microZones.values().stream().forEach(microZone -> {
            microZonesProbabilities.put(microZone.getId(), microZone.getAttribute("employment"));
        } );
        return FreightFlowUtils.select(microZonesProbabilities,
                FreightFlowUtils.getSum(microZonesProbabilities.values()));

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
}


