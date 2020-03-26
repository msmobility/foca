package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;


public class Properties extends PropertiesGroup {

    private ZoneSystemProperties zoneSystemProperties;
    private FlowsProperties flowsProperties;
    private LDProperties lDProperties;
    private SDProperties sDProperties;
    private static Logger LOGGER = Logger.getLogger(Properties.class);


    private String matrixFileNamePrefix = "ketten-uniform-";
    private String matrixFileNameSuffix = ".csv";
    private String commodityAttributeFile = "input/commodities/commodity_groups_kba_ipf.csv";
    private String distributionCentersFile = "input/distributionCenters/distributionCenters.csv";
    private String terminalsFileName = "input/distributionCenters/intermodal_terminals_31468.csv";

    private String networkFile = "./networks/matsim/final_V11_emissions.xml.gz";
    private String simpleNetworkFile = "./networks/matsim/europe_v2.xml.gz";
    private int iterations = 1;
    private double flowsScaleFactor = 1.;
    private double truckScaleFactor = 1.;
    private String runId = "base";
    private int randomSeed = 1;
    private Random rand;
    private int[] analysisZones = new int[]{};
    private boolean storeExpectedTimes = false;

    private int daysPerYear;

    private boolean readEventsForCounts = true;
    private String countStationLinkListFile = "input/matsim_links_stations.csv";
    private String countsFileName = "counts_multi_day.csv";
    private String vehicleFile = "input/vehicleFile.xml";


    private String[] jobTypes = new String[]{"Mnft", "Util", "Cons", "Retl", "Trns", "Finc", "Rlst", "Admn", "Serv", "Agri"};
    private String makeTableFileName = "./input/makeUseCoefficients/makeTable_eurostat.csv";
    private String useTableFileName = "./input/makeUseCoefficients/useTable_eurostat.csv";

    private String parcelWeightDistributionFile = "./input/parcel_weight_distribution_small.csv";

    private double sampleFactorForParcels = 1.;
    private boolean runParcelDelivery = true;
    private String vehicleFileForParcelDelivery = "./input/vehicleTypesForParcelDelivery.xml";

    @Deprecated
    private String distributionCentersCatchmentAreaFile = "./input/distributionCenters/distributionCentersCatchmentArea.csv";
    private String matsimBackgroundTrafficPlanFile = "";
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

    public Properties(ResourceBundle bundle) {
        super(bundle);

        //read all the properties of this class and assign the values
        matrixFileNamePrefix = PropertiesUtil.getStringProperty(bundle, "matrixFileNamePrefix", matrixFileNamePrefix);
        matrixFileNameSuffix = PropertiesUtil.getStringProperty(bundle, "matrixFileNameSuffix", matrixFileNameSuffix);
        commodityAttributeFile = PropertiesUtil.getStringProperty(bundle, "commodityAttributeFile", commodityAttributeFile);
        distributionCentersFile = PropertiesUtil.getStringProperty(bundle, "distributionCentersFile", distributionCentersFile);
        terminalsFileName = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", terminalsFileName);
        networkFile = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", networkFile);
        simpleNetworkFile = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", simpleNetworkFile);
        iterations = PropertiesUtil.getIntProperty(bundle, "iterations", iterations);
        flowsScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "flowsScaleFactor", flowsScaleFactor);
        truckScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "truckScaleFactor", truckScaleFactor);
        runId = PropertiesUtil.getStringProperty(bundle, "runId", runId);
        randomSeed = PropertiesUtil.getIntProperty(bundle, "randomSeed", randomSeed);
        analysisZones = PropertiesUtil.getIntPropertyArray(bundle, "analysisZones", analysisZones);
        storeExpectedTimes = PropertiesUtil.getBooleanProperty(bundle, "storeExpectedTimes", storeExpectedTimes);
        daysPerYear = PropertiesUtil.getIntProperty(bundle, "daysPerYear", daysPerYear);

        readEventsForCounts = PropertiesUtil.getBooleanProperty(bundle, "readEventsForCounts", readEventsForCounts);
        countStationLinkListFile = PropertiesUtil.getStringProperty(bundle, "countStationLinkListFile", countStationLinkListFile);
        countsFileName = PropertiesUtil.getStringProperty(bundle, "countsFileName", countStationLinkListFile);
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

        outputFolder = PropertiesUtil.getStringProperty(bundle, "outputFolder", outputFolder);

        year = PropertiesUtil.getIntProperty(bundle, "year", year);

        zoneSystemProperties = new ZoneSystemProperties(bundle);
        flowsProperties = new FlowsProperties(bundle);
        lDProperties = new LDProperties(bundle);
        sDProperties = new SDProperties(bundle);

    }

    public void logProperties(String outPropFile) throws FileNotFoundException {

        File propFile = new File(outPropFile);
        if (!propFile.getParentFile().exists()) {
            propFile.getParentFile().mkdirs();
        }
        PropertiesUtil.printOutPropertiesOfThisRun(outPropFile);
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

    public String getNetworkFile() {
        return networkFile;
    }

    public void setNetworkFile(String networkFile) {
        this.networkFile = networkFile;
    }

    public String getSimpleNetworkFile() {
        return simpleNetworkFile;
    }

    public void setSimpleNetworkFile(String simpleNetworkFile) {
        this.simpleNetworkFile = simpleNetworkFile;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double getTruckScaleFactor() {
        return truckScaleFactor;
    }

    public void setTruckScaleFactor(double truckScaleFactor) {
        this.truckScaleFactor = truckScaleFactor;
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

    public boolean isStoreExpectedTimes() {
        return storeExpectedTimes;
    }

    public void setStoreExpectedTimes(boolean storeExpectedTimes) {
        this.storeExpectedTimes = storeExpectedTimes;
    }

    public String getCommodityAttributeFile() {
        return commodityAttributeFile;
    }

    public void setCommodityAttributeFile(String commodityAttributeFile) {
        this.commodityAttributeFile = commodityAttributeFile;
    }

    public int getDaysPerYear() {
        return daysPerYear;
    }

    public void setDaysPerYear(int daysPerYear) {
        this.daysPerYear = daysPerYear;
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

    public String getDistributionCentersFile() {
        return distributionCentersFile;
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

    public String getTerminalsFile() {
        return terminalsFileName;
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

    public double getFlowsScaleFactor() {
        return flowsScaleFactor;
    }

    public void setFlowsScaleFactor(double flowsScaleFactor) {
        this.flowsScaleFactor = flowsScaleFactor;
    }

    @Deprecated
    public String getDistributionCentersCatchmentAreaFile() {
        return distributionCentersCatchmentAreaFile;
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

    public void setDistributionCentersFile(String distributionCentersFile) {
        this.distributionCentersFile = distributionCentersFile;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMatrixFileNamePrefix() {
        return matrixFileNamePrefix;
    }

    public String getMatrixFileNameSuffix() {
        return matrixFileNameSuffix;
    }

    public void initializeRandomNumber() {
        rand = new Random(randomSeed);
    }
}
