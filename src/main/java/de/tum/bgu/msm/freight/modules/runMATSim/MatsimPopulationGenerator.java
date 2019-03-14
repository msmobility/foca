package de.tum.bgu.msm.freight.modules.runMATSim;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.DepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.NormalDepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class MatsimPopulationGenerator implements Module {

    private Properties properties;
    private DataSet dataSet;
    private CoordinateTransformation ct;
    private DepartureTimeDistribution departureTimeDistribution;

    @Override
    public void setup(DataSet dataset, Properties properties) {
        this.dataSet = dataset;
        this.properties = properties;
        ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
        departureTimeDistribution = new NormalDepartureTimeDistribution();
    }

    @Override
    public void run() {
       generatePopulation();
    }


    private void generatePopulation() {
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory factory = population.getFactory();

        AtomicInteger counter = new AtomicInteger(0);

        for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {

            for (int vehicle = 0; vehicle < flowSegment.getLoadedTrucks(); vehicle++) {

                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {

                    LongDistanceTruckTrip longDistanceTruckTrip = createOneTruckTrip(flowSegment, true);

                    boolean intrazonal = flowSegment.getOrigin() == flowSegment.getDestination() ? true : false;

                    String idOfVehicle = flowSegment.getCommodity().getCommodityGroup() + "-" +
                            vehicle + flowSegment.getCommodity().getCommodityGroup().getGoodDistribution() + "-" +
                            flowSegment.getSegmentType() + "-" +
                            counter;

                    if (intrazonal) {
                        idOfVehicle += "-INTRA";
                    }

                    Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    Activity originActivity = factory.createActivityFromCoord("start", longDistanceTruckTrip.getOrigCoord());
                    originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(TransportMode.truck));

                    Activity destinationActivity = factory.createActivityFromCoord("end", longDistanceTruckTrip.getDestCoord());
                    plan.addActivity(destinationActivity);
                    counter.incrementAndGet();

                }
            }

            for (int vehicle = 0; vehicle < flowSegment.getEmptyTrucks(); vehicle++) {

                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {

                    LongDistanceTruckTrip longDistanceTruckTrip = createOneTruckTrip(flowSegment, false);

                    String idOfVehicle =
                            flowSegment.getCommodity().getCommodityGroup() + "-" +
                                    vehicle + "-IS_EMPTY-" + flowSegment.getCommodity().getCommodityGroup().getGoodDistribution() + "-" +
                                    counter;


                    Person person = factory.createPerson(Id.createPersonId(idOfVehicle));
                    Plan plan = factory.createPlan();
                    person.addPlan(plan);
                    population.addPerson(person);

                    Activity originActivity = factory.createActivityFromCoord("start", longDistanceTruckTrip.getOrigCoord());
                    originActivity.setEndTime(departureTimeDistribution.getDepartureTime(0) * 60);
                    plan.addActivity(originActivity);

                    plan.addLeg(factory.createLeg(TransportMode.truck));

                    Activity destinationActivity = factory.createActivityFromCoord("end", longDistanceTruckTrip.getDestCoord());
                    plan.addActivity(destinationActivity);
                    counter.incrementAndGet();
                }

            }

        }


        dataSet.setMatsimPopulation(population);
    }

    private LongDistanceTruckTrip createOneTruckTrip(FlowSegment flowSegment, boolean loaded) {

        Zone originZone = dataSet.getZones().get(flowSegment.getOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getDestination());

        Coord origCoord;
        Coord destCoord;

        if (flowSegment.getSegmentType().equals(SegmentType.POST)) {
            origCoord = dataSet.getTerminals().get(flowSegment.getOriginTerminal()).getCoordinates();
        } else if (!flowSegment.getCommodity().getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                originZone.isInStudyArea()) {
            //pick up a distribution center
            DistributionCenter originDistributionCenter = chooseDistributionCenter(flowSegment.getOrigin(), flowSegment.getCommodity().getCommodityGroup());
            origCoord = originDistributionCenter.getCoordinates();
            //further disaggregate the flows, including the destination microzones if needed

        } else {
            if (!originZone.isInStudyArea()) {
                //if zone does not have microzones
                origCoord = originZone.getCoordinates();
            } else {
                //if zone does have microzones
                InternalZone internalZone = (InternalZone) originZone;
                int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(flowSegment.getCommodity(), internalZone, dataSet.getMakeTable());
                origCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
            }
        }

        origCoord = ct.transform(origCoord);


        if (flowSegment.getSegmentType().equals(SegmentType.PRE)) {
            destCoord = dataSet.getTerminals().get(flowSegment.getDestinationTerminal()).getCoordinates();
        } else if (!flowSegment.getCommodity().getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                destinationZone.isInStudyArea()) {
            DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flowSegment.getDestination(), flowSegment.getCommodity().getCommodityGroup());
            destCoord = destinationDistributionCenter.getCoordinates();
            //further disaggregate the flows, including the destination microzones if needed

        } else {
            if (!destinationZone.isInStudyArea()) {
                //if zone does not have microzones
                destCoord = destinationZone.getCoordinates();
            } else {
                //if zone does have microzones
                InternalZone internalZone = (InternalZone) destinationZone;
                int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(flowSegment.getCommodity(), internalZone, dataSet.getUseTable());
                destCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
            }
        }
        destCoord = ct.transform(destCoord);

        return new LongDistanceTruckTrip(origCoord, destCoord, flowSegment, loaded);
    }

    private DistributionCenter chooseDistributionCenter(int zoneId, CommodityGroup commodityGroup) {
        ArrayList<DistributionCenter> distributionCenters = dataSet.getDistributionCenterForZoneAndCommodityGroup(zoneId, commodityGroup);
        Collections.shuffle(distributionCenters, properties.getRand());
        return distributionCenters.get(0);
    }
}
