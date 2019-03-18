package de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment;

import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.common.DepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.NormalDepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.modules.common.UncongestedTravelTime;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import scala.sys.Prop;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tum.bgu.msm.freight.data.freight.SegmentType.POST;

public class FlowsToVehicleAssignment implements de.tum.bgu.msm.freight.modules.Module {

    private static Logger logger = Logger.getLogger(FlowsToVehicleAssignment.class);

    private UncongestedTravelTime uncongestedTravelTime;
    private Properties properties;

    private DataSet dataSet;

    private CoordinateTransformation ct;


    private Set<Integer> selectedDestinations = new HashSet<>();



    @Override
    public void setup(DataSet dataSet, Properties properties){
        ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);


        this.properties = properties;
        this.dataSet = dataSet;

        if (properties.isStoreExpectedTimes()) {
            uncongestedTravelTime = new UncongestedTravelTime(properties.getSimpleNetworkFile());
            uncongestedTravelTime.calculateTravelTimeMatrix(ct, dataSet);
        }

        for (int destId : properties.getSelectedDestinations()) {
            if (destId == -1) {
                selectedDestinations = dataSet.getFlowMatrix().columnKeySet();
                break;
            } else {
                selectedDestinations.add(destId);
            }
        }
    }

    @Override
    public void run(){
        generateNumberOfTrucks();
        printOutResults();
    }

    private void generateNumberOfTrucks() {

        AtomicInteger counter = new AtomicInteger(0);

        for (int origin : dataSet.getFlowMatrix().rowKeySet()) {
            for (int destination : dataSet.getFlowMatrix().columnKeySet()) {
                if (selectedDestinations.contains(origin) || selectedDestinations.contains(destination)) {
                    if (dataSet.getFlowMatrix().contains(origin, destination) && dataSet.getZones().containsKey(origin) &&
                            dataSet.getZones().containsKey(destination)) {
                        Collection<FlowOriginToDestination> flowsThisOrigDest = dataSet.getFlowMatrix().get(origin, destination).values();
                        for (FlowOriginToDestination flowOriginToDestination : flowsThisOrigDest) {
                            for (FlowSegment flowSegment : flowOriginToDestination.getFlows().values()) {
                                if (flowSegment.getMode().equals(Mode.ROAD)) {
                                    int tripOrigin = flowSegment.getOrigin();
                                    int tripDestination = flowSegment.getDestination();

                                    Zone originZone = dataSet.getZones().get(tripOrigin);
                                    Zone destinationZone = dataSet.getZones().get(tripDestination);

                                    Coord origCoord = originZone.getCoordinates();
                                    Coord destCoord = destinationZone.getCoordinates();

                                    origCoord = ct.transform(origCoord);
                                    destCoord = ct.transform(destCoord);

                                    double beelineDistance_km = NetworkUtils.getEuclideanDistance(origCoord, destCoord) / 1000;
                                    DistanceBin distanceBin = DistanceBin.getDistanceBin(beelineDistance_km);
                                    double truckLoad = dataSet.getTruckLoadsByDistanceAndCommodity().get(flowSegment.getCommodity(), distanceBin);
                                    double proportionEmpty = dataSet.getEmptyTrucksProportionsByDistanceAndCommodity().get(flowSegment.getCommodity(), distanceBin);

                                    double numberOfVehicles_double = flowSegment.getVolume_tn() / properties.getDaysPerYear() / truckLoad;
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
                                    flowSegment.setDistance_km(beelineDistance_km);
                                    flowSegment.setLoadedTrucks(loadedTrucks_int);
                                    flowSegment.setEmptyTrucks(emptyTrucks_int);
                                    flowSegment.setTt_s(dataSet.getUncongestedTravelTime(tripOrigin, tripDestination));

                                    dataSet.getAssignedFlowSegments().add(flowSegment);

                                }

                                counter.incrementAndGet();
                                if (counter.get() % 10000 == 0) {
                                    logger.info(counter.get() + " flows to trucks assigned");
                                }
                            }
                        }
                    }
                }
            }
        }

    }







    public void printOutResults() {

        File file = new File("./output/" + properties.getRunId());
        file.mkdirs();

        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter("./output/" + properties.getRunId() + "/truckFlows.csv"));

            pw.println("orig,dest,commodity,distanceBin,volume_tn,trucks,tt");

            for (FlowSegment FlowSegment : dataSet.getAssignedFlowSegments()) {

                int trucks = FlowSegment.getEmptyTrucks() + FlowSegment.getLoadedTrucks();

                pw.println(FlowSegment.getOrigin() + "," +
                        FlowSegment.getDestination() + "," +
                        FlowSegment.getCommodity() + "," +
                        FlowSegment.getDistance_km() + "," +
                        FlowSegment.getVolume_tn() / properties.getDaysPerYear() + "," +
                        trucks + "," +
                        FlowSegment.getTt_s());
            }

            pw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
