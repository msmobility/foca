package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.LongDistanceTruckTrip;
import de.tum.bgu.msm.freight.data.freight.Parcel;
import de.tum.bgu.msm.freight.data.freight.ShortDistanceTruckTrip;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.FirstLastMileVehicleDistribution;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.FlowsToVehicleAssignment;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.runMATSim.MatsimPopulationGenerator;
import de.tum.bgu.msm.freight.properties.Properties;

import java.io.IOException;

public class FreightFlows {

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();

        /*
        Place to configure the properties according to users' prefrereces, otherwise the default values are chosen
         */

        properties.setSelectedDestinations(new int[]{9162, 9362});
        properties.setRunId("assignmentFull4");
        properties.setIterations(1);

        //properties.setStoreExpectedTimes(true);

        properties.logUsedProperties();

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        FlowsToVehicleAssignment flowsToVehicleAssignment = new FlowsToVehicleAssignment();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator();
        FirstLastMileVehicleDistribution firstLastMileVehicleDistribution = new FirstLastMileVehicleDistribution();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        flowsToVehicleAssignment.setup(dataSet, properties);
        matsimPopulationGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        firstLastMileVehicleDistribution.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        flowsToVehicleAssignment.run();
        matsimPopulationGenerator.run();
        parcelGenerator.run();
        firstLastMileVehicleDistribution.run();

        OutputWriter.printOutObjects(dataSet.getLongDistanceTruckTrips(), LongDistanceTruckTrip.getHeader(), "output/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getShortDistanceTruckTrips(), ShortDistanceTruckTrip.getHeader(), "output/sd_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getParcels(), Parcel.getHeader(), "output/parcels.csv");


        matSimAssignment.run();



    }
}
