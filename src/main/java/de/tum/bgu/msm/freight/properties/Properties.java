package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;


public class Properties extends PropertiesGroup {

    private final ZoneSystemProperties zoneSystemProperties;
    public final FlowsProperties flowsProperties;
    private final LDProperties lDProperties;
    private final SDProperties sDProperties;
    private final ModeChoiceProperties modeChoiceProperties;
    private static Logger LOGGER = Logger.getLogger(Properties.class);




    private String networkFile = "./networks/matsim/final_V11_emissions.xml.gz";

    private int iterations = 1;

    private String runId = "base";

    private int randomSeed = 1;
    private Random rand;

    private int[] analysisZones = new int[]{};


    private boolean readEventsForCounts = true;
    private String countStationLinkListFile = "input/matsim_links_stations.csv";
    private String countsFileName = "counts.csv";
    private String vehicleFile = "input/vehicleFile.xml";

    private String makeTableFileName = "./input/makeUseCoefficients/makeTable_eurostat.csv";
    private String useTableFileName = "./input/makeUseCoefficients/useTable_eurostat.csv";

    private String[] jobTypes = new String[]{"Mnft", "Util", "Cons", "Retl", "Trns", "Finc", "Rlst", "Admn", "Serv", "Agri"};


    private String parcelWeightDistributionFile = "./input/parcel_weight_distribution.csv";
    private double sampleFactorForParcels = 1.;
    private boolean runParcelDelivery = true;
    private String vehicleFileForParcelDelivery = "./input/vehicleTypesForParcelDelivery.xml";

    @Deprecated
    private String distributionCentersCatchmentAreaFile = "./input/distributionCenters/distributionCentersCatchmentArea.csv";
    private String matsimBackgroundTrafficPlanFile = "";
    private double matsimAdditionalScaleFactor = 1.0;
    private String outputFolder = "output/";
    private int year = 2010;


    public static ResourceBundle initializeResourceBundleFromFile(String propertiesFileName){
        File propFile = null;
        propFile = new File(propertiesFileName);
        ResourceBundle bundle = null;
        try {
            bundle = new PropertyResourceBundle(new FileReader(propFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;

    }

    public double getMatsimAdditionalScaleFactor() {
        return matsimAdditionalScaleFactor;
    }

    public void setMatsimAdditionalScaleFactor(double matsimAdditionalScaleFactor) {
        this.matsimAdditionalScaleFactor = matsimAdditionalScaleFactor;
    }

    public Properties(ResourceBundle bundle) {
        super(bundle);

        //read all the properties of this class and assign the values

        networkFile = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", networkFile);

        iterations = PropertiesUtil.getIntProperty(bundle, "iterations", iterations);
        runId = PropertiesUtil.getStringProperty(bundle, "runId", runId);
        randomSeed = PropertiesUtil.getIntProperty(bundle, "randomSeed", randomSeed);
        analysisZones = PropertiesUtil.getIntPropertyArray(bundle, "analysisZones", analysisZones);



        readEventsForCounts = PropertiesUtil.getBooleanProperty(bundle, "readEventsForCounts", readEventsForCounts);
        countStationLinkListFile = PropertiesUtil.getStringProperty(bundle, "countStationLinkListFile", countStationLinkListFile);
        countsFileName = PropertiesUtil.getStringProperty(bundle, "countsFileName", countsFileName);
        vehicleFile = PropertiesUtil.getStringProperty(bundle, "vehicleFile", vehicleFile);

        jobTypes = PropertiesUtil.getStringPropertyArray(bundle, "jobTypes", jobTypes);
        makeTableFileName = PropertiesUtil.getStringProperty(bundle, "makeTableFileName", makeTableFileName);
        useTableFileName = PropertiesUtil.getStringProperty(bundle, "useTableFileName", useTableFileName);

        parcelWeightDistributionFile = PropertiesUtil.getStringProperty(bundle, "parcelWeightDistributionFile", parcelWeightDistributionFile);

        sampleFactorForParcels = PropertiesUtil.getDoubleProperty(bundle, "sampleFactorForParcels", sampleFactorForParcels);
        runParcelDelivery = PropertiesUtil.getBooleanProperty(bundle, "runParcelDelivery", runParcelDelivery);
        vehicleFileForParcelDelivery = PropertiesUtil.getStringProperty(bundle, "vehicleFileForParcelDelivery", vehicleFileForParcelDelivery);

        //distributionCentersCatchmentAreaFile = PropertiesUtil.getBooleanProperty(bundle, "distributionCentersCatchmentAreaFile");

        matsimBackgroundTrafficPlanFile = PropertiesUtil.getStringProperty(bundle, "matsimBackgroundTrafficPlanFile", matsimBackgroundTrafficPlanFile);
        matsimAdditionalScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "matsim.additional.scale", matsimAdditionalScaleFactor);

        outputFolder = PropertiesUtil.getStringProperty(bundle, "outputFolder", outputFolder);

        year = PropertiesUtil.getIntProperty(bundle, "year", year);

        zoneSystemProperties = new ZoneSystemProperties(bundle);
        flowsProperties = new FlowsProperties(bundle);
        lDProperties = new LDProperties(bundle);
        sDProperties = new SDProperties(bundle);
        modeChoiceProperties = new ModeChoiceProperties(bundle);

    }

    public void logProperties(String propFolder) throws FileNotFoundException {

        File propFile = new File(propFolder);
        if (!propFile.exists()) {
            propFile.mkdirs();
        }

        PropertiesUtil.printOutPropertiesOfThisRun(propFolder);
//        PrintWriter pw = new PrintWriter(propFile);
//        this.logUsedProperties(pw);
//        zoneSystemProperties.logUsedProperties(pw);
//        flowsProperties.logUsedProperties(pw);
//        lDProperties.logUsedProperties(pw);
//        sDProperties.logUsedProperties(pw);
//        pw.close();
    }


    public ZoneSystemProperties zoneSystem() {
        return zoneSystemProperties;
    }

    public FlowsProperties flows() {
        return flowsProperties;
    }

    public LDProperties longDistance() {
        return lDProperties;
    }

    public SDProperties shortDistance() {
        return sDProperties;
    }

    public ModeChoiceProperties modeChoice() {
        return modeChoiceProperties;
    }

    public String getNetworkFile() {
        return networkFile;
    }

    public void setNetworkFile(String networkFile) {
        this.networkFile = networkFile;
    }



    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Random getRand() {
        return rand;
    }


    //public void setRand(Random rand) {
//        this.rand = rand;
//    }
    public int[] getAnalysisZones() {
        return analysisZones;
    }

    public void setAnalysisZones(int[] analysisZones) {
        this.analysisZones = analysisZones;
    }


    public boolean isReadEventsForCounts() {
        return readEventsForCounts;
    }

    public void setReadEventsForCounts(boolean readEventsForCounts) {
        this.readEventsForCounts = readEventsForCounts;
    }

    public String getCountStationLinkListFile() {
        return countStationLinkListFile;
    }

    public void setCountStationLinkListFile(String countStationLinkListFile) {
        this.countStationLinkListFile = countStationLinkListFile;
    }

    public String getCountsFileName() {
        return countsFileName;
    }

    public void setCountsFileName(String countsFileName) {
        this.countsFileName = countsFileName;
    }

    public String getVehicleFile() {
        return vehicleFile;
    }

    public void setVehicleFile(String vehicleFile) {
        this.vehicleFile = vehicleFile;
    }



    public String[] getJobTypes() {
        return jobTypes;
    }

    public String getMakeTableFilename() {
        return makeTableFileName;
    }

    public String getUseTableFilename() {
        return useTableFileName;
    }



    public String getParcelWeightDistributionFile() {
        return parcelWeightDistributionFile;
    }

    public void setParcelWeightDistributionFile(String parcelWeightDistributionFile) {
        this.parcelWeightDistributionFile = parcelWeightDistributionFile;
    }

    public void setSampleFactorForParcels(double sampleFactorForParcels) {
        this.sampleFactorForParcels = sampleFactorForParcels;
    }

    public double getSampleFactorForParcels() {
        return sampleFactorForParcels;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public boolean isRunParcelDelivery() {
        return runParcelDelivery;
    }

    public String getVehicleFileForParcelDelivery() {
        return vehicleFileForParcelDelivery;
    }


    public String getMatsimBackgroundTraffic() {
        return matsimBackgroundTrafficPlanFile;
    }

    public void setMatsimBackgroundTrafficPlanFile(String matsimBackgroundTrafficPlanFile) {
        this.matsimBackgroundTrafficPlanFile = matsimBackgroundTrafficPlanFile;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setVehicleFileForParcelDelivery(String s) {
        this.vehicleFileForParcelDelivery = s;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }



    public void initializeRandomNumber() {
        rand = new Random(randomSeed);
    }
}
