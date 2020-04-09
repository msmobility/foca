package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.TruckTrip;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.modules.common.DepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.NormalDepartureTimeDistribution;
import de.tum.bgu.msm.freight.properties.Properties;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.network.NetworkUtils;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates MATSim population of trucks (not for parcel or freight-tour-based distribution), which is carried out using
 * the freight extension
 */
public class MATSimTruckPlanGenerator {

    private DataSet dataSet;
    private Properties properties;
    private DepartureTimeDistribution departureTimeDistribution;



    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        departureTimeDistribution = new NormalDepartureTimeDistribution(properties);
    }

    public void addTrucks(Population population, Network network) {
        PopulationFactory factory = population.getFactory();
        AtomicInteger counter = new AtomicInteger(0);

        for (LDTruckTrip lDTruckTrip : dataSet.getLDTruckTrips()) {

            if (properties.getRand().nextDouble() < properties.longDistance().getTruckScaleFactor()) {
                lDTruckTrip.setAssigned(true);

                generatePlanForThisTruck(population, factory, counter, lDTruckTrip);
            }
        }

        for (SDTruckTrip sDTruckTrip : dataSet.getSDTruckTrips()){
            if (properties.getRand().nextDouble() < properties.longDistance().getTruckScaleFactor()) {
                sDTruckTrip.setAssigned(true);
//                String idOfVehicle = "sDTruck_";
//                idOfVehicle+= sDTruckTrip.getCommodity().getCommodityGroup().toString() + "_";
//                idOfVehicle+= sDTruckTrip.getId();


                generatePlanForThisTruck(population, factory, counter, sDTruckTrip);
//                Person person = factory.createPerson(Id.createPersonId(sDTruckTrip.getId()));
//                Plan plan = factory.createPlan();
//                person.addPlan(plan);
//                population.addPerson(person);
//
//                Coordinate origCoordinate = sDTruckTrip.getOrigCoord();
//                Coord origCoord = new Coord(origCoordinate.x, origCoordinate.y);
//                //Link origLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, origCoord));
//                Activity originActivity = factory.createActivityFromCoord("start", origCoord);
//
//                originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
//                plan.addActivity(originActivity);
//
//                plan.addLeg(factory.createLeg(TransportMode.truck));
//
//                Coordinate destCoordinate = sDTruckTrip.getDestCoord();
//                Coord destCoord = new Coord(destCoordinate.x, destCoordinate.y);
//                //Link destLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, destCoord));
//                Activity destinationActivity = factory.createActivityFromCoord("end", destCoord);
//                plan.addActivity(destinationActivity);

            }
        }

        //dataSet.setMatsimPopulation(population);

    }

    private void generatePlanForThisTruck(Population population, PopulationFactory factory, AtomicInteger counter, TruckTrip lDTruckTrip) {
        Person person = factory.createPerson(Id.createPersonId(lDTruckTrip.getId()));
        Plan plan = factory.createPlan();
        person.addPlan(plan);


        Coordinate origCoordinate = lDTruckTrip.getOrigCoord();
        Coord origCoord;
        try {
            origCoord = new Coord(origCoordinate.x, origCoordinate.y);
            //Link origLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, origCoord));
        } catch (NullPointerException e){
            origCoord = null;
        }
        Activity originActivity = factory.createActivityFromCoord("start", origCoord);
        originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
        plan.addActivity(originActivity);

        plan.addLeg(factory.createLeg(TransportMode.truck));

        Coordinate destCoordinate = lDTruckTrip.getDestCoord();
        Coord destCoord;
        try {
            destCoord = new Coord(destCoordinate.x, destCoordinate.y);
        } catch (NullPointerException e){
            destCoord = null;
        }

        //Link destLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, destCoord));
        Activity destinationActivity = factory.createActivityFromCoord("end", destCoord);
        plan.addActivity(destinationActivity);
        counter.incrementAndGet();

        if (origCoord != null && destCoord != null ) {
            population.addPerson(person);
        }

    }
}
