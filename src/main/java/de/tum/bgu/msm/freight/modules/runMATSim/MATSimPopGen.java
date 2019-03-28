package de.tum.bgu.msm.freight.modules.runMATSim;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.LongDistanceTruckTrip;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.DepartureTimeDistribution;
import de.tum.bgu.msm.freight.modules.common.NormalDepartureTimeDistribution;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class MATSimPopGen implements Module {

    private DataSet dataSet;
    private Properties properties;
    private DepartureTimeDistribution departureTimeDistribution;



    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        departureTimeDistribution = new NormalDepartureTimeDistribution(properties);
    }

    @Override
    public void run() {

        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationFactory factory = population.getFactory();

        AtomicInteger counter = new AtomicInteger(0);

        for (LongDistanceTruckTrip longDistanceTruckTrip : dataSet.getLongDistanceTruckTrips()) {

            if (properties.getRand().nextDouble() < properties.getScaleFactor()) {
                FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

                boolean intrazonal = flowSegment.getSegmentOrigin() == flowSegment.getSegmentDestination() ? true : false;

                String idOfVehicle = flowSegment.getCommodity().getCommodityGroup() + "-" +
                        flowSegment.getTruckTrips().indexOf(longDistanceTruckTrip) + flowSegment.getCommodity().getCommodityGroup().getGoodDistribution() + "-" +
                        flowSegment.getSegmentType() + "-" +
                        counter;

                if (intrazonal) {
                    idOfVehicle += "-INTRA";
                }

                if (longDistanceTruckTrip.getLoad_tn() == 0.) {
                    idOfVehicle += "-EMPTY";
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

        dataSet.setMatsimPopulation(population);

    }
}
