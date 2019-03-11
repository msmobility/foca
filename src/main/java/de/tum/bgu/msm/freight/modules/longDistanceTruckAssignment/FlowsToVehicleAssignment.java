package de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment;

import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.modules.common.DepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.NormalDepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.UncongestedTravelTime;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowsToVehicleAssignment {

    private static Logger logger = Logger.getLogger(FlowsToVehicleAssignment.class);

    private DataSet dataSet;
    private UncongestedTravelTime uncongestedTravelTime;
    private Properties properties;

    private CoordinateTransformation ct;
    private DepartureTimeDistribution departureTimeDistribution;

    private Set<Integer> selectedDestinations = new HashSet<>();
    private final ArrayList<Flow> assignedFlows = new ArrayList<>();

    public FlowsToVehicleAssignment(DataSet dataSet, Properties properties) {

        ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
        departureTimeDistribution = new NormalDepartureTimeDistribution();

        this.dataSet = dataSet;
        this.properties = properties;
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

    public void generateNumberOfTrucks() {

        AtomicInteger counter = new AtomicInteger(0);

        for (int origin : dataSet.getFlowMatrix().rowKeySet()) {
            for (int destination : selectedDestinations) {
                if (dataSet.getFlowMatrix().contains(origin, destination)) {
                    if (dataSet.getZones().containsKey(origin) &&
                            dataSet.getZones().containsKey(destination)) {
                        ArrayList<OriginDestinationPair> flowsThisOrigDest = dataSet.getFlowMatrix().get(origin, destination);
                        for (OriginDestinationPair originDestinationPair : flowsThisOrigDest) {
                            for (Flow flow : originDestinationPair.getTrips().values()) {
                                if (flow.getMode().equals(Mode.ROAD)) {
                                    int tripOrigin = flow.getOrigin();
                                    int tripDestination = flow.getDestination();

                                    Zone originZone = dataSet.getZones().get(tripOrigin);
                                    Zone destinationZone = dataSet.getZones().get(tripDestination);

                                    Coord origCoord = originZone.getCoordinates(null);
                                    Coord destCoord = destinationZone.getCoordinates(null);

                                    origCoord = ct.transform(origCoord);
                                    destCoord = ct.transform(destCoord);

                                    double beelineDistance_km = NetworkUtils.getEuclideanDistance(origCoord, destCoord) / 1000;
                                    DistanceBin distanceBin = DistanceBin.getDistanceBin(beelineDistance_km);
                                    double truckLoad = dataSet.getTruckLoadsByDistanceAndCommodity().get(flow.getCommodity(), distanceBin);
                                    double proportionEmpty = dataSet.getEmptyTrucksProportionsByDistanceAndCommodity().get(flow.getCommodity(), distanceBin);

                                    double numberOfVehicles_double = flow.getVolume_tn() / properties.getDaysPerYear() / truckLoad;
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
                                    flow.setDistance_km(beelineDistance_km);
                                    flow.setLoadedTrucks(loadedTrucks_int);
                                    flow.setEmptyTrucks(emptyTrucks_int);
                                    flow.setTt_s(dataSet.getUncongestedTravelTime(tripOrigin, tripDestination));

                                    assignedFlows.add(flow);

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

    private DistributionCenter chooseDistributionCenter(int zoneId, CommodityGroup commodityGroup) {
        ArrayList<DistributionCenter> distributionCenters = dataSet.getDistributionCenterForZoneAndCommodityGroup(zoneId, commodityGroup);
        Collections.shuffle(distributionCenters, properties.getRand());
        return distributionCenters.get(0);
    }


    public Population generatePopulation(Config config) {
        Population population = PopulationUtils.createPopulation(config);
        PopulationFactory factory = population.getFactory();

        AtomicInteger counter = new AtomicInteger(0);

        for (Flow flow : assignedFlows) {

            for (int vehicle = 0; vehicle < flow.getLoadedTrucks(); vehicle++) {

                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {

                    TruckTrip truckTrip = createOneTruckTrip(flow, true);

                    String idOfVehicle = flow.getCommodity().getCommodityGroup() + "-" +
                            vehicle + flow.getCommodity().getCommodityGroup().getGoodDistribution() + "-" +
                            counter;

                    Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    Activity originActivity = factory.createActivityFromCoord("start", truckTrip.getOrigCoord());
                    originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(TransportMode.truck));

                    Activity destinationActivity = factory.createActivityFromCoord("end", truckTrip.getDestCoord());
                    plan.addActivity(destinationActivity);
                    counter.incrementAndGet();

                }
            }

            for (int vehicle = 0; vehicle < flow.getEmptyTrucks(); vehicle++) {

                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {

                    TruckTrip truckTrip = createOneTruckTrip(flow, false);

                    String idOfVehicle =
                            flow.getCommodity().getCommodityGroup() + "-" +
                            vehicle + "-IS_EMPTY-" + flow.getCommodity().getCommodityGroup().getGoodDistribution() + "-" +
                            counter;


                    Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    Activity originActivity = factory.createActivityFromCoord("start", truckTrip.getOrigCoord());
                    originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(TransportMode.truck));

                    Activity destinationActivity = factory.createActivityFromCoord("end", truckTrip.getDestCoord());
                    plan.addActivity(destinationActivity);
                    counter.incrementAndGet();
                }

            }

        }


        return population;
    }

    private TruckTrip createOneTruckTrip(Flow flow, boolean loaded) {

        Zone originZone = dataSet.getZones().get(flow.getOrigin());
        Zone destinationZone = dataSet.getZones().get(flow.getDestination());

        Coord origCoord;
        Coord destCoord;

        if (!flow.getCommodity().getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                originZone.isInStudyArea()) {
            DistributionCenter originDistributionCenter = chooseDistributionCenter(flow.getOrigin(), flow.getCommodity().getCommodityGroup());
            origCoord = originDistributionCenter.getCoordinates(flow.getCommodity());
        } else {

            origCoord = originZone.getCoordinates(flow.getCommodity());
        }

        origCoord = ct.transform(origCoord);


        if (!flow.getCommodity().getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                destinationZone.isInStudyArea()) {
            DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flow.getDestination(), flow.getCommodity().getCommodityGroup());
            destCoord = destinationDistributionCenter.getCoordinates(flow.getCommodity());
        } else {
            destCoord = destinationZone.getCoordinates(flow.getCommodity());
        }
        destCoord = ct.transform(destCoord);

        return new TruckTrip(origCoord, destCoord, flow, loaded);
    }


    public void printOutResults() throws IOException {

        File file = new File("./output/" + properties.getRunId());
        file.mkdirs();

        PrintWriter pw = new PrintWriter(new FileWriter("./output/" + properties.getRunId() + "/truckFlows.csv"));

        pw.println("orig,dest,commodity,distanceBin,volume_tn,trucks,tt");

        for (Flow Flow : assignedFlows) {

            int trucks = Flow.getEmptyTrucks() + Flow.getLoadedTrucks();

            pw.println(Flow.getOrigin() + "," +
                    Flow.getDestination() + "," +
                    Flow.getCommodity() + "," +
                    Flow.getDistance_km() + "," +
                    Flow.getVolume_tn() / properties.getDaysPerYear() + "," +
                    trucks + "," +
                    Flow.getTt_s());
        }

        pw.close();
    }

}
