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
import de.tum.bgu.msm.freight.modules.syntheticMicroDepotGeneration.SyntheticMicroDepots;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class RunFocaWithModalShare {


    private static final Logger logger = Logger.getLogger(RunFocaWithModalShare.class);

    public static void main(String[] args) {


        Properties properties = new Properties(Properties.initializeResourceBundleFromFile(args[0]));

        try {
            properties.logProperties("./output/" + properties.getRunId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        RunFocaWithModalShare freightFlows = new RunFocaWithModalShare();
        logger.info("Start simulation " + properties.getRunId());
        freightFlows.run(properties);
        logger.info("End simulation " + properties.getRunId());

    }

    public void run(Properties properties) {
        properties.initializeRandomNumber();

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        SyntheticMicroDepots syntehticMicroDepots = new SyntheticMicroDepots();

        FlowsToLDTruckConverter flowsToLDTruckConverter = null;
        LDTruckODAllocator LDTruckODAllocator = null;
        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            LDTruckODAllocator = new LDTruckODAllocator();
            flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        }
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        ModeChoiceModel modeChoiceModel = new GlobalModalShareModeChoice();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        syntehticMicroDepots.setup(dataSet, properties);
        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            flowsToLDTruckConverter.setup(dataSet, properties);
            LDTruckODAllocator.setup(dataSet, properties);
        }

        SDTruckGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        modeChoiceModel.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        syntehticMicroDepots.run();

        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            flowsToLDTruckConverter.run();
            LDTruckODAllocator.run();

        }
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
