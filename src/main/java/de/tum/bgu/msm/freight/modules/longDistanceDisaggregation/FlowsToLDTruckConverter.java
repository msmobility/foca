package de.tum.bgu.msm.freight.modules.longDistanceDisaggregation;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowOriginToDestination;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDMode;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.common.UncongestedTravelTime;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Disaggregates flows to trucks by year to day and tons to truck conversion. adds uniform share of empty trucks
 */
public class FlowsToLDTruckConverter implements de.tum.bgu.msm.freight.modules.Module {

    private static Logger logger = Logger.getLogger(FlowsToLDTruckConverter.class);

    private UncongestedTravelTime uncongestedTravelTime;
    private Properties properties;

    private DataSet dataSet;

    private CoordinateTransformation ct;

    private boolean doAllZones;


    @Override
    public void setup(DataSet dataSet, Properties properties) {
        ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);

        this.properties = properties;
        this.dataSet = dataSet;

        if (properties.longDistance().isStoreExpectedTimes()) {
            uncongestedTravelTime = new UncongestedTravelTime(properties.longDistance().getSimpleNetworkFile());
            uncongestedTravelTime.calculateTravelTimeMatrix(ct, dataSet, properties);
        }

        if (properties.getAnalysisZones().length == 0) {
            doAllZones = true;
        } else {
            doAllZones = false;
        }

    }

    @Override
    public void run() {
        generateNumberOfTrucks();
        printOutResults();
    }

    private void generateNumberOfTrucks() {
        AtomicInteger flowCounter = new AtomicInteger(0);
        AtomicInteger counter = new AtomicInteger(0);

        for (int origin : dataSet.getFlowMatrix().rowKeySet()) {
            for (int destination : dataSet.getFlowMatrix().columnKeySet()) {
                if (dataSet.getFlowMatrix().contains(origin, destination) && dataSet.getZones().containsKey(origin) &&
                        dataSet.getZones().containsKey(destination)) {
                    Map<Integer, FlowOriginToDestination> flowsThisOrigDest = dataSet.getFlowMatrix().get(origin, destination);
                    for (FlowOriginToDestination flowOriginToDestination : flowsThisOrigDest.values()) {
                        for (FlowSegment flowSegment : flowOriginToDestination.getFlowSegments().values()) {
                            if (flowSegment.getLDMode().equals(LDMode.ROAD)) {
                                int tripOrigin = flowSegment.getSegmentOrigin();
                                int tripDestination = flowSegment.getSegmentDestination();
                                Zone originZone = dataSet.getZones().get(tripOrigin);
                                Zone destinationZone = dataSet.getZones().get(tripDestination);
                                Coordinate origCoord = originZone.getCoordinates(properties.getRand());
                                Coordinate destCoord = destinationZone.getCoordinates(properties.getRand());

                                double beelineDistance_km = NetworkUtils.getEuclideanDistance(new Coord(origCoord.x, origCoord.y), new Coord(destCoord.x, destCoord.y)) / 1000;
                                DistanceBin distanceBin = DistanceBin.getDistanceBin(beelineDistance_km);
                                double truckLoad = dataSet.getTruckLoadsByDistanceAndCommodity().get(flowSegment.getCommodity(), distanceBin);
                                double proportionEmpty = dataSet.getEmptyTrucksProportionsByDistanceAndCommodity().get(flowSegment.getCommodity(), distanceBin);

                                double numberOfVehicles_double = flowSegment.getVolume_tn() / properties.longDistance().getDaysPerYear() / truckLoad;
                                double numberOfEmptyVehicles_double = numberOfVehicles_double / (1 - proportionEmpty) - numberOfVehicles_double;

                                int loadedTrucks_int = (int) Math.floor(numberOfVehicles_double);
                                int emptyTrucks_int = (int) Math.floor(numberOfEmptyVehicles_double);

                                if (properties.getRand().nextDouble() < (numberOfVehicles_double - loadedTrucks_int)) {
                                    loadedTrucks_int++;
                                }

                                if (properties.getRand().nextDouble() < (numberOfEmptyVehicles_double - emptyTrucks_int)) {
                                    emptyTrucks_int++;
                                }
                                //set new trip details
                                for (int truck = 0; truck < loadedTrucks_int; truck++) {
                                    Id<TruckTrip> truckTripId = Id.create("LD_" + counter.getAndIncrement(), TruckTrip.class);
                                    flowSegment.getTruckTrips().add(

                                            new LDTruckTrip(truckTripId, flowSegment, truckLoad));
                                }

                                for (int truck = 0; truck < emptyTrucks_int; truck++) {
                                    Id<TruckTrip> truckTripId = Id.create("LD_" + counter.getAndIncrement(), TruckTrip.class);
                                    flowSegment.getTruckTrips().add(
                                            new LDTruckTrip(truckTripId, flowSegment, 0.));
                                }

                                flowSegment.setDistance_km(beelineDistance_km);
                                flowSegment.setTt_s(dataSet.getUncongestedTravelTime(tripOrigin, tripDestination));

                                if (originZone.isInStudyArea() || destinationZone.isInStudyArea() || doAllZones) {
                                    dataSet.getAssignedFlowSegments().add(flowSegment);
                                }
                                if (LongMath.isPowerOfTwo(flowCounter.incrementAndGet())) {
                                    logger.info(flowCounter.get() + " LD trucks assigned");
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.info(counter.get() + " long distance trucks assigned");

    }


    public void printOutResults() {

        File file = new File("./output/" + properties.getRunId());
        file.mkdirs();

        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter("./output/" + properties.getRunId() + "/truckFlows.csv"));

            pw.println("orig,dest,commodity,distanceBin,volume_tn,trucks,tt");

            for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {

                int trucks = flowSegment.getTruckTrips().size();

                pw.println(flowSegment.getSegmentOrigin() + "," +
                        flowSegment.getSegmentDestination() + "," +
                        flowSegment.getCommodity() + "," +
                        flowSegment.getDistance_km() + "," +
                        flowSegment.getVolume_tn() / properties.longDistance().getDaysPerYear() + "," +
                        trucks + "," +
                        flowSegment.getTt_s());
            }

            pw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
