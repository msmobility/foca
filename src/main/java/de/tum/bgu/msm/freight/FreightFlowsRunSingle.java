package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.assignment.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.FlowsToLDTruckConverter;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.LDTruckODAllocator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.*;
import de.tum.bgu.msm.freight.modules.syntehticMicroDepotGeneration.SyntehticMicroDepots;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.core.population.io.PopulationWriter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsRunSingle {


    private static final Logger logger = Logger.getLogger(FreightFlowsRunSingle.class);

    public static void main(String[] args) {


        Properties properties = new Properties();
        properties.setMatrixFolder("./input/matrices/");
        properties.setAnalysisZones(new int[]{9162});
        properties.setNetworkFile("./networks/matsim/final_V11_emissions.xml.gz");
        properties.setTruckScaleFactor(0.01);
        properties.setSampleFactorForParcels(0.1);
        properties.setIterations(2);
        properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
        properties.setRunId("test");
        properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters.csv");
        properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(1.);
        properties.shortDistance().setDistanceBetweenMicrodepotsInGrid(2000.);
        properties.shortDistance().setMaxDistanceToMicroDepot(2000.);

        properties.shortDistance().setReadMicroDepotsFromFile(false);

        try {
            properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //adds a 5% pf cars as background traffic
        //properties.setMatsimBackgroundTrafficPlanFile("./input/carPlans/cars_5_percent.xml.gz");
        FreightFlowsRunSingle freightFlows = new FreightFlowsRunSingle();
        logger.info("Start simulation " + properties.getRunId());
        freightFlows.run(properties);
        logger.info("End simulation " + properties.getRunId());

    }

    public void run(Properties properties) {
        properties.initializeRandomNumber();

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        SyntehticMicroDepots syntehticMicroDepots = new SyntehticMicroDepots();
        FlowsToLDTruckConverter flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        LDTruckODAllocator LDTruckODAllocator = new LDTruckODAllocator();
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        ModeChoiceModel modeChoiceModel = new ContinuousApproximationModeChoice();
        MATSimAssignment matSimAssignment = new MATSimAssignment();

        syntehticMicroDepots.setup(dataSet, properties);
        flowsToLDTruckConverter.setup(dataSet, properties);
        LDTruckODAllocator.setup(dataSet, properties);
        SDTruckGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        modeChoiceModel.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        syntehticMicroDepots.run();
        flowsToLDTruckConverter.run();
        LDTruckODAllocator.run();
        SDTruckGenerator.run();
        parcelGenerator.run();
        modeChoiceModel.run();
        matSimAssignment.run();

        //PopulationWriter pw;


        String outputFolder = properties.getOutputFolder();
        OutputWriter.printOutObjects(dataSet.getAssignedFlowSegments(), FlowSegment.getHeader(), outputFolder + properties.getRunId() + "/flowSegments.csv");
        OutputWriter.printOutObjects(dataSet.getLDTruckTrips(), LDTruckTrip.getHeader(), outputFolder + properties.getRunId() + "/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getSDTruckTrips(), SDTruckTrip.getHeader(), outputFolder + properties.getRunId() + "/sd_trucks.csv");
        List<Parcel> parcelsList = new ArrayList<>();
        for (List<Parcel> listOfParcelsInDc : dataSet.getParcelsByDistributionCenter().values()) {
            parcelsList.addAll(listOfParcelsInDc);
        }
        OutputWriter.printOutObjects(parcelsList, Parcel.getHeader(), outputFolder + properties.getRunId() + "/parcels.csv");
    }
}
