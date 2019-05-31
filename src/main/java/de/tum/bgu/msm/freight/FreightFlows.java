package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.*;
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
import java.util.ArrayList;
import java.util.List;

public class FreightFlows {


    private static final Logger logger = Logger.getLogger(FreightFlows.class);

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();

        /*
        Place to configure the properties according to users' prefrerences, otherwise the default values are chosen
         */

        //properties.setFlowsScaleFactor();
        properties.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties.setRunId("results_2010");

        properties.setSelectedZones(new int[]{9162, 9362});
        properties.setTruckScaleFactor(0.05);
        properties.setSampleFactorForParcels(0.05);

        properties.setIterations(10);

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
        firstLastMileVehicleDistribution.run();
        parcelGenerator.run();
        MATSImPopGen.run();


        OutputWriter.printOutObjects(dataSet.getAssignedFlowSegments(), FlowSegment.getHeader(), "output/" + properties.getRunId() +  "/flowSegments.csv");
        OutputWriter.printOutObjects(dataSet.getLongDistanceTruckTrips(), LongDistanceTruckTrip.getHeader(), "output/" + properties.getRunId() +  "/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getShortDistanceTruckTrips(), ShortDistanceTruckTrip.getHeader(), "output/" + properties.getRunId() +  "/sd_trucks.csv");
        List<Parcel> parcelsList = new ArrayList<>();
        for (List<Parcel> listOfParcelsInDc : dataSet.getParcelsByDistributionCenter().values()){
            parcelsList.addAll(listOfParcelsInDc);
        }
        OutputWriter.printOutObjects(parcelsList, Parcel.getHeader(), "output/" + properties.getRunId() +  "/parcels.csv");


        //matSimAssignment.run();


    }
}
