package de.tum.bgu.msm.freight;


import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


public class FreightFlowUtils {

    private final static CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
    private final static Logger logger = Logger.getLogger(FreightFlowUtils.class);
    private static int projectionErrorCounter = 0;

    public static Coordinate getRandomCoordinatesFromFeature(SimpleFeature feature, Random random){
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent((Geometry) feature.getDefaultGeometry());
        Envelope envelope = randomPointsBuilder.getExtent();
        double x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
        double y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
        return new Coordinate(x, y);
    }


    public static <T> T select(Map<T, Double> probabilities, double sum, Random random) {
        // select item based on probabilities (for mapped double probabilities)
        double selectedWeight = random.nextDouble() * sum;
        double select = 0;
        for (Map.Entry<T, Double> entry : probabilities.entrySet()) {
            select += entry.getValue();
            if (select > selectedWeight) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities");
    }

    public static double getSum(Collection<Double> values) {
        double sm = 0.0D;

        Double value;
        for(Iterator var3 = values.iterator(); var3.hasNext(); sm += value) {
            value = (Double)var3.next();
        }

        return sm;
    }

    @Deprecated
    public static Coordinate convertWGS84toGK4(Coordinate coord){
        Coord newCoord;
        try{
            newCoord = ct.transform(new Coord(coord.x, coord.y));
            return new Coordinate(newCoord.getX(), newCoord.getY());
        } catch (Exception e){
            projectionErrorCounter++;
            logger.warn("Converting false coordinates from " + coord.toString() + "Count " + projectionErrorCounter);
            return new Coordinate(0, 0);
        }
    }

    public static Link findUpstreamLinksForMotorizedVehicle(Link thisLink) {
        if (thisLink.getAttributes().getAttribute("onlyCargoBike").equals(true)) {
            Map<Id<Link>, Link> upstreamLinks = (Map<Id<Link>, Link>) thisLink.getFromNode().getInLinks();
            for (Link upstreamLink : upstreamLinks.values()) {
                if (upstreamLink.getAttributes().getAttribute("onlyCargoBike").equals(true)) {
                    return upstreamLink;
                } else {
                    return findUpstreamLinksForMotorizedVehicle(upstreamLink);
                }
            }
        } else {
            return thisLink;
        }
        return null;
    }
}
