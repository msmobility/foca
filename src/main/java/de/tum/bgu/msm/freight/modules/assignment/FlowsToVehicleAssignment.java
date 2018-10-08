package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.data.*;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowsToVehicleAssignment {

    private FreightFlowsDataSet dataSet;
    private final double tons_by_truck = 10.;

    public FlowsToVehicleAssignment(FreightFlowsDataSet dataSet) {
        this.dataSet = dataSet;

    }

    public Population disaggregateToVehicles(Config config, double scaleFactor) {
        Population population = PopulationUtils.createPopulation(config);
        PopulationFactory factory = population.getFactory();

        AtomicInteger counter = new AtomicInteger(0);

        CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);

        Set<Integer> destinations = dataSet.getFlowMatrix().columnKeySet();
        destinations = new HashSet<Integer>();
        destinations.add(9162);

        for(int origin : dataSet.getFlowMatrix().rowKeySet()){
            for (int destination : destinations) {
                if (dataSet.getFlowMatrix().contains(origin, destination)) {
                    if (dataSet.getZones().containsKey(origin) &&
                            dataSet.getZones().containsKey(destination)) {
                        Zone originZone = dataSet.getZones().get(origin);
                        Zone destinationZone = dataSet.getZones().get(destination);
                        ArrayList<OrigDestFlow> flowsThisOrigDest = dataSet.getFlowMatrix().get(origin, destination);
                        for (OrigDestFlow origDestFlow : flowsThisOrigDest) {
                            if (origDestFlow.getMode().equals(Mode.ROAD)) {
                                double  numberOfVehicles_double = origDestFlow.getVolume_tn()/365/tons_by_truck;
                                int numberOfVehicles_int = (int) Math.floor(numberOfVehicles_double);
                                if(Math.random() < (numberOfVehicles_double - numberOfVehicles_int)){
                                    numberOfVehicles_int++;
                                }
                                for (int vehicle = 0; vehicle < numberOfVehicles_int; vehicle++) {
                                    if (Math.random() < scaleFactor) {
                                        String idOfVehicle = origin + "-" +
                                                destination + "-" +
                                                origDestFlow.getCommodity() + "-" +
                                                vehicle + "-" +
                                                counter;

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

                                            Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                                            Plan plan = factory.createPlan();
                                            person.addPlan(plan);
                                            population.addPerson(person);

                                            Activity originActivity = factory.createActivityFromCoord("production", origCoord);
                                            originActivity.setEndTime(Math.random() * 24 * 60 * 60);
                                            plan.addActivity(originActivity);

                                            plan.addLeg(factory.createLeg(TransportMode.car));

                                            Activity destinationActivity = factory.createActivityFromCoord("consumption", destCoord);
                                            plan.addActivity(destinationActivity);
                                            counter.incrementAndGet();
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

}
