package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowsToVehicleAssignment {

    private FreightFlowsDataSet dataSet;
    private UncongestedTravelTime uncongestedTravelTime;

    private ArrayList<StoredFlow> flowsByTruck = new ArrayList<>();

    public FlowsToVehicleAssignment(FreightFlowsDataSet dataSet) {
        this.dataSet = dataSet;
        if (Properties.storeExpectedTimes) {
            uncongestedTravelTime = new UncongestedTravelTime(Properties.simpleNetworkFile);
        }
    }

    public Population disaggregateToVehicles(Config config, double scaleFactor) {
        Population population = PopulationUtils.createPopulation(config);
        PopulationFactory factory = population.getFactory();

        AtomicInteger counter = new AtomicInteger(0);

        CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);

        Set<Integer> destinations = new HashSet<>();
        for (int destId : Properties.selectedDestinations) {
            if (destId == -1) {
                destinations = dataSet.getFlowMatrix().columnKeySet();
                break;
            } else {
                destinations.add(destId);
            }
        }

        for (int origin : dataSet.getFlowMatrix().rowKeySet()) {
            for (int destination : destinations) {
                if (dataSet.getFlowMatrix().contains(origin, destination)) {
                    if (dataSet.getZones().containsKey(origin) &&
                            dataSet.getZones().containsKey(destination)) {
                        ArrayList<OrigDestFlow> flowsThisOrigDest = dataSet.getFlowMatrix().get(origin, destination);
                        for (OrigDestFlow origDestFlow : flowsThisOrigDest) {
                            for (Trip trip : origDestFlow.getTrips().values()) {
                                if (trip.getMode().equals(Mode.ROAD)) {
                                    int tripOrigin = trip.getOrigin();
                                    int tripDestination = trip.getDestination();
                                    Zone originZone = dataSet.getZones().get(tripOrigin);
                                    Zone destinationZone = dataSet.getZones().get(tripDestination);

                                    Coord origCoord;
                                    Coord destCoord;
                                    try {
                                        origCoord = originZone.getRandomCoord();
                                    } catch (NullPointerException e) {
                                        origCoord = null;
                                    }

                                    try {
                                        destCoord = destinationZone.getRandomCoord();
                                    } catch (NullPointerException e) {
                                        destCoord = null;
                                    }

                                    if (origCoord != null && destCoord != null) {

                                        origCoord = ct.transform(origCoord);
                                        destCoord = ct.transform(destCoord);

                                        double beelineDistance_km = NetworkUtils.getEuclideanDistance(origCoord, destCoord)/1000;
                                        DistanceBin distanceBin = DistanceBin.getDistanceBin(beelineDistance_km);
                                        double truckLoad = dataSet.getTruckLoadsByDistanceAndCommodity().get(trip.getCommodity(), distanceBin);
                                        double proportionEmpty = dataSet.getEmptyTruckProportionsByDistanceAndCommodity().get(trip.getCommodity(), distanceBin);

                                        double numberOfVehicles_double = trip.getVolume_tn() / Properties.daysPerYear / truckLoad;
                                        numberOfVehicles_double = numberOfVehicles_double / (1 - proportionEmpty);

                                        int numberOfVehicles_int = (int) Math.floor(numberOfVehicles_double);
                                        if (Properties.rand.nextDouble() < (numberOfVehicles_double - numberOfVehicles_int)) {
                                            numberOfVehicles_int++;
                                        }

                                        flowsByTruck.add(new StoredFlow(trip.getCommodity(), beelineDistance_km, trip.getVolume_tn(), numberOfVehicles_int));
                                        for (int vehicle = 0; vehicle < numberOfVehicles_int; vehicle++) {
                                            if (Properties.rand.nextDouble() < scaleFactor) {
                                                String idOfVehicle = tripOrigin + "-" +
                                                        tripDestination + "-" +
                                                        trip.getCommodity().getCommodityGroup() + "-" +
                                                        vehicle + "-" +
                                                        counter;

                                                if (!trip.getSegment().equals(Segment.MAIN)) {
                                                    idOfVehicle += "-" + trip.getSegment().toString();
                                                }

                                                if (trip.getFlowType().equals(FlowType.CONTAINER_RO_RO)) {
                                                    idOfVehicle += "-" + trip.getFlowType().toString();
                                                }


                                                Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                                                Plan plan = factory.createPlan();
                                                person.addPlan(plan);
                                                population.addPerson(person);

                                                Activity originActivity = factory.createActivityFromCoord("start", origCoord);
                                                originActivity.setEndTime(Properties.rand.nextDouble() * 24 * 60 * 60);
                                                plan.addActivity(originActivity);

                                                plan.addLeg(factory.createLeg(TransportMode.car));

                                                Activity destinationActivity = factory.createActivityFromCoord("end", destCoord);
                                                plan.addActivity(destinationActivity);
                                                counter.incrementAndGet();

                                                //simply adds the expected time as an attribute to the plan (no current application)
                                                //it is slow at this point
                                                //may be used to better decide on the arrival time at destination
                                                if (Properties.storeExpectedTimes) {
                                                    double time = uncongestedTravelTime.getTravelTime(origCoord, destCoord);
                                                    plan.getAttributes().putAttribute("time", time);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return population;
    }


    public void printOutResults() throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("./output/" + Properties.runId + "/truckFlows.csv"));

        pw.println("commodity,distanceBin,volume_tn,trucks");

        for (StoredFlow storedFlow : flowsByTruck){
            pw.println(storedFlow.commodity + "," +  storedFlow.distance_km + "," +  storedFlow.volume_tn / Properties.daysPerYear + "," +  storedFlow.numberOfTrucks);
        }

        pw.close();
    }


    private class StoredFlow {
        private Commodity commodity;
        private double distance_km;
        private double volume_tn;
        private int numberOfTrucks;

        public StoredFlow(Commodity commodity, double distance_km, double volume_tn, int numberOfTrucks) {
            this.commodity = commodity;
            this.distance_km = distance_km;
            this.volume_tn = volume_tn;
            this.numberOfTrucks = numberOfTrucks;
        }

    }
}
