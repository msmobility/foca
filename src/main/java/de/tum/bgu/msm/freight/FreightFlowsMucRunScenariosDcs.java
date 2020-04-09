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
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.GlobalModalShareModeChoice;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ModeChoiceModel;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.SDTruckGenerator;
import de.tum.bgu.msm.freight.modules.syntheticMicroDepotGeneration.SyntheticMicroDepots;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.core.population.io.PopulationWriter;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsMucRunScenariosDcs {


    private static final Logger logger = Logger.getLogger(FreightFlowsMucRunScenariosDcs.class);

    public static void main(String[] args) {

        List<Properties> listOfSimulations = new ArrayList<>();

        int dc = Integer.parseInt(args[0]);

        Properties thisProperties = new Properties(null);
        thisProperties.initializeRandomNumber();
        thisProperties.flows().setMatrixFolder("./input/matrices/");
        thisProperties.setAnalysisZones(new int[]{9162});
        thisProperties.longDistance().setTruckScaleFactor(1.00);
        thisProperties.setSampleFactorForParcels(0.25);
        thisProperties.setIterations(50);
        thisProperties.shortDistance().setReadMicroDepotsFromFile(false);
        thisProperties.shortDistance().setSelectedDistributionCenters(new int[]{dc});
        thisProperties.setRunId("muc_dc_" + args[0]);
        thisProperties.setDistributionCentersFile("./input/distributionCenters/distributionCenters.csv");
        thisProperties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(1.0);
        try {
            thisProperties.logProperties("./output/" + thisProperties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        listOfSimulations.add(thisProperties);


        for (Properties properties : listOfSimulations) {
            //adds a 5% pf cars as background traffic
            //properties.setMatsimBackgroundTrafficPlanFile("./input/carPlans/cars_5_percent.xml.gz");
            FreightFlowsMucRunScenariosDcs freightFlows = new FreightFlowsMucRunScenariosDcs();
            logger.info("Start simulation " + properties.getRunId());
            freightFlows.run(properties);
            logger.info("End simulation " + properties.getRunId());
        }


    }

    public void run(Properties properties) {

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        SyntheticMicroDepots syntehticMicroDepots = new SyntheticMicroDepots();
        FlowsToLDTruckConverter flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        LDTruckODAllocator LDTruckODAllocator = new LDTruckODAllocator();
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        ModeChoiceModel modeChoiceModel = new GlobalModalShareModeChoice();
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
        PopulationWriter pw;


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
