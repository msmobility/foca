package de.tum.bgu.msm.freight;

import com.pb.common.datafile.ExcelFileReader;
import de.tum.bgu.msm.freight.properties.Properties;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.MapProjection;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Point2D;
import java.util.*;


public class FreightFlowUtils {

    public static Random random;
    private final static CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
    private final static Logger logger = Logger.getLogger(FreightFlowUtils.class);
    private static int projectionErrorCounter = 0;

    public static Coordinate getRandomCoordinatesFromFeature(SimpleFeature feature){
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent((Geometry) feature.getDefaultGeometry());
        Envelope envelope = randomPointsBuilder.getExtent();
        double x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
        double y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
        return new Coordinate(x, y);
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

}
