package de.tum.bgu.msm.freight.data;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.Zone;
import de.tum.bgu.msm.util.MitoUtil;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InternalZone implements Zone {

    private int id;
    private String name;
    private SimpleFeature shapeFeature;
    private Map<Integer, InternalMicroZone> microZones;


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

    public Coord getRandomCoord() {
        if (microZones.isEmpty()) {
            return FreightFlowUtils.getRandomCoordinatesFromFeature(this.shapeFeature);
        } else {
            int microZoneId = disaggregateToMicroZone();
            return microZones.get(microZoneId).getRandomCoord();
        }
    }

    public void addMicroZone(InternalMicroZone microZone){
        this.microZones.put(microZone.getId(), microZone);
    }

    private int disaggregateToMicroZone() {
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


}


