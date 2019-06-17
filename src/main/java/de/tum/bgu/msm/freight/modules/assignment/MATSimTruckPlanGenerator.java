package de.tum.bgu.msm.freight.modules.assignment;

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
import org.matsim.api.core.v01.population.*;


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

    public void addTrucks(Population population) {
        PopulationFactory factory = population.getFactory();
        AtomicInteger counter = new AtomicInteger(0);

        for (LDTruckTrip lDTruckTrip : dataSet.getLDTruckTrips()) {

            if (properties.getRand().nextDouble() < properties.getTruckScaleFactor()) {
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

                Coordinate origCoord = lDTruckTrip.getOrigCoord();
                Activity originActivity = factory.createActivityFromCoord("start", new Coord(origCoord.x, origCoord.y));
                originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                plan.addActivity(originActivity);

                plan.addLeg(factory.createLeg(TransportMode.truck));

                Coordinate destCoord = lDTruckTrip.getDestCoord();
                Activity destinationActivity = factory.createActivityFromCoord("end", new Coord(destCoord.x, destCoord.y));
                plan.addActivity(destinationActivity);
                counter.incrementAndGet();
            }
        }

        for (SDTruckTrip sDTruckTrip : dataSet.getSDTruckTrips()){
            if (properties.getRand().nextDouble() < properties.getTruckScaleFactor()) {
//                String idOfVehicle = "sDTruck_";
//                idOfVehicle+= sDTruckTrip.getCommodity().getCommodityGroup().toString() + "_";
//                idOfVehicle+= sDTruckTrip.getId();

                Person person = factory.createPerson(Id.createPersonId(sDTruckTrip.getId()));
                Plan plan = factory.createPlan();
                person.addPlan(plan);
                population.addPerson(person);

                Coordinate origCoord = sDTruckTrip.getOrigCoord();
                Activity originActivity = factory.createActivityFromCoord("start",  new Coord(origCoord.x, origCoord.y));
                originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                plan.addActivity(originActivity);

                plan.addLeg(factory.createLeg(TransportMode.truck));

                Coordinate destCoord = sDTruckTrip.getDestCoord();
                Activity destinationActivity = factory.createActivityFromCoord("end", new Coord(destCoord.x, destCoord.y));
                plan.addActivity(destinationActivity);

            }
        }

        //dataSet.setMatsimPopulation(population);

    }
}
