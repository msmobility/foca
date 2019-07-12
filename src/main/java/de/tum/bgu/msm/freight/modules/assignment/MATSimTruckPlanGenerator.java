package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
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

            if (properties.getRand().nextDouble() < properties.getTruckScaleFactor()) {
                lDTruckTrip.setAssigned(true);

                FlowSegment flowSegment = lDTruckTrip.getFlowSegment();

//                boolean intrazonal = flowSegment.getSegmentOrigin() == flowSegment.getSegmentDestination() ? true : false;
//
//                String idOfVehicle = "lDTruck-" + flowSegment.getCommodity().getCommodityGroup() + "-" +
//                        flowSegment.getTruckTrips().indexOf(lDTruckTrip) + "-" +
//                        flowSegment.getCommodity().getCommodityGroup().getLongDistanceGoodDistribution() + "-" +
//                        flowSegment.getSegmentType() + "-" +
//                        lDTruckTrip.getId();
//
//                if (intrazonal) {
//                    idOfVehicle += "-INTRA";
//                }
//
//                if (lDTruckTrip.getLoad_tn() == 0.) {
//                    idOfVehicle += "-EMPTY";
//                }

                Person person = factory.createPerson(Id.createPersonId(lDTruckTrip.getId()));
                Plan plan = factory.createPlan();
                person.addPlan(plan);
                population.addPerson(person);

                Coordinate origCoordinate = lDTruckTrip.getOrigCoord();
                Coord origCoord = new Coord(origCoordinate.x, origCoordinate.y);
                //Link origLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, origCoord));

                Activity originActivity = factory.createActivityFromCoord("start", origCoord);
                originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                plan.addActivity(originActivity);

                plan.addLeg(factory.createLeg(TransportMode.truck));

                Coordinate destCoordinate = lDTruckTrip.getDestCoord();
                Coord destCoord = new Coord(destCoordinate.x, destCoordinate.y);
                //Link destLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, destCoord));
                Activity destinationActivity = factory.createActivityFromCoord("end", destCoord);
                plan.addActivity(destinationActivity);
                counter.incrementAndGet();
            }
        }

        for (SDTruckTrip sDTruckTrip : dataSet.getSDTruckTrips()){
            if (properties.getRand().nextDouble() < properties.getTruckScaleFactor()) {
                sDTruckTrip.setAssigned(true);
//                String idOfVehicle = "sDTruck_";
//                idOfVehicle+= sDTruckTrip.getCommodity().getCommodityGroup().toString() + "_";
//                idOfVehicle+= sDTruckTrip.getId();

                Person person = factory.createPerson(Id.createPersonId(sDTruckTrip.getId()));
                Plan plan = factory.createPlan();
                person.addPlan(plan);
                population.addPerson(person);

                Coordinate origCoordinate = sDTruckTrip.getOrigCoord();
                Coord origCoord = new Coord(origCoordinate.x, origCoordinate.y);
                //Link origLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, origCoord));
                Activity originActivity = factory.createActivityFromCoord("start", origCoord);

                originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                plan.addActivity(originActivity);

                plan.addLeg(factory.createLeg(TransportMode.truck));

                Coordinate destCoordinate = sDTruckTrip.getDestCoord();
                Coord destCoord = new Coord(destCoordinate.x, destCoordinate.y);
                //Link destLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(NetworkUtils.getNearestLink(network, destCoord));
                Activity destinationActivity = factory.createActivityFromCoord("end", destCoord);
                plan.addActivity(destinationActivity);

            }
        }

        //dataSet.setMatsimPopulation(population);

    }
}
