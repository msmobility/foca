package de.tum.bgu.msm.freight;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;


public class FreightFlowUtils {

    public static Random random;
    private final static CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);

    public static Coord getRandomCoordinatesFromFeature(SimpleFeature feature){
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent((Geometry) feature.getDefaultGeometry());
        Envelope envelope = randomPointsBuilder.getExtent();
        double x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
        double y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
        return convertWGS84toGK4(new Coord(x, y));
    }

    public static void setRandomNumber(Properties properties){
        random = properties.getRand();
    }

    public static <T> T select(Map<T, Double> probabilities, double sum) {
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

    public static Coord convertWGS84toGK4(Coord coord){
        return ct.transform(coord);
    }

}
