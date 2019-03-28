package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.FirstLastMileVehicleDistribution;
import de.tum.bgu.msm.freight.modules.distributionFromCenters.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.FlowsToVehicles;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.OriginDestinationAllocation;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimPopGen;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.io.IOException;

public class FreightFlows {


    private static final Logger logger = Logger.getLogger(FreightFlows.class);

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();

        /*
        Place to configure the properties according to users' prefrereces, otherwise the default values are chosen
         */

       // properties.setSelectedDestinations(new int[]{9162, 9362});
        properties.setScaleFactor(0.05);
        properties.setRunId("test");
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
        MATSimPopGen MATSImPopGen = new MATSimPopGen();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        flowsToVehicles.setup(dataSet, properties);
        originDestinationAllocation.setup(dataSet, properties);
        firstLastMileVehicleDistribution.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        MATSImPopGen.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        flowsToVehicles.run();
        originDestinationAllocation.run();
        double r1 = properties.getRand().nextDouble();
        firstLastMileVehicleDistribution.run();
        double r2 = properties.getRand().nextDouble();
        parcelGenerator.run();
        double r3 = properties.getRand().nextDouble();
        MATSImPopGen.run();


        OutputWriter.printOutObjects(dataSet.getAssignedFlowSegments(), FlowSegment.getHeader(), "output/flowSegments.csv");
        OutputWriter.printOutObjects(dataSet.getLongDistanceTruckTrips(), LongDistanceTruckTrip.getHeader(), "output/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getShortDistanceTruckTrips(), ShortDistanceTruckTrip.getHeader(), "output/sd_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getParcels(), Parcel.getHeader(), "output/parcels.csv");


        //matSimAssignment.run();


    }
}
