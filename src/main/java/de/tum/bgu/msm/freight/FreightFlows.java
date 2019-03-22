package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.LongDistanceTruckTrip;
import de.tum.bgu.msm.freight.data.freight.Parcel;
import de.tum.bgu.msm.freight.data.freight.ShortDistanceTruckTrip;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.FirstLastMileVehicleDistribution;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.FlowsToVehicles;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.OriginDestinationAllocation;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSImPopGen;
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

        FlowsToVehicles flowsToVehicles = new FlowsToVehicles();
        OriginDestinationAllocation originDestinationAllocation = new OriginDestinationAllocation();
        FirstLastMileVehicleDistribution firstLastMileVehicleDistribution = new FirstLastMileVehicleDistribution();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        MATSImPopGen MATSImPopGen = new MATSImPopGen();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        flowsToVehicles.setup(dataSet, properties);
        originDestinationAllocation.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        firstLastMileVehicleDistribution.setup(dataSet, properties);
        MATSImPopGen.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        flowsToVehicles.run();
        originDestinationAllocation.run();
        firstLastMileVehicleDistribution.run();
        parcelGenerator.run();
        MATSImPopGen.run();


        OutputWriter.printOutObjects(dataSet.getAssignedFlowSegments(), FlowSegment.getHeader(), "output/flowSegments.csv");
        OutputWriter.printOutObjects(dataSet.getLongDistanceTruckTrips(), LongDistanceTruckTrip.getHeader(), "output/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getShortDistanceTruckTrips(), ShortDistanceTruckTrip.getHeader(), "output/sd_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getParcels(), Parcel.getHeader(), "output/parcels.csv");


        matSimAssignment.run();



    }
}
