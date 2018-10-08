package de.tum.bgu.msm.freight.data;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.freight.data.Zone;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.feature.simple.SimpleFeature;

public class InternalZone implements Zone {

    private int id;
    private String name;
    private SimpleFeature shapeFeature;


    public InternalZone(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SimpleFeature getShapeFeature() {
        return shapeFeature;
    }

    public void setShapeFeature(SimpleFeature shapeFeature) {
        this.shapeFeature = shapeFeature;
    }

    public Coord getRandomCoord() {
        // alternative and about 10 times faster way to generate random point inside a geometry. Amit Dec'17
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent((Geometry) shapeFeature.getDefaultGeometry());
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        Point p = MGC.coordinate2Point(coordinate);
        return new Coord(p.getX(), p.getY());
    }

    @Override
    public String getName() {
        return null;
    }


}


