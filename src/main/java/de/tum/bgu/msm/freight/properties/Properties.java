package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;


public class Properties extends PropertiesGroup {

    private ZoneSystemProperties zoneSystemProperties;
    private FlowsProperties flowsProperties;
    private LDProperties lDProperties;
    private SDProperties sDProperties;
    private static Logger LOGGER = Logger.getLogger(Properties.class);

    private String matrixFileName = "./input/matrices/ketten-2010.csv";
    private String commodityAttributeFile = "input/commodities/commodity_groups_kba_ipf.csv";
    private String distributionCentersFile = "input/distributionCenters/distributionCenters.xml";
    private String terminalsFileName = "input/distributionCenters/intermodal_terminals_31468.csv";

    private String networkFile = "./networks/matsim/final_V9_emissions.xml.gz";
    private String simpleNetworkFile = "./networks/matsim/europe_v2.xml.gz";
    private int iterations = 1;
    private double flowsScaleFactor = 1.;
    private double truckScaleFactor = 1.;
    private String runId = "assignmentFull";
    private int randomSeed = 1;
    private Random rand = new Random(randomSeed);
    private int[] selectedZones = new int[]{-1};
    private boolean storeExpectedTimes = false;

    private int daysPerYear = 365;

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
    private String distributionCentersCatchmenAreaFile = "./input/distributionCenters/distributionCentersCatchmentArea.csv";

    private String matsimBackgroundTrafficPlanFile = "";

    private String outputFolder = "output/";


    public Properties() {
        FreightFlowUtils.setRandomNumber(this);
        zoneSystemProperties = new ZoneSystemProperties();
        flowsProperties = new FlowsProperties();
        lDProperties = new LDProperties();
        sDProperties = new SDProperties();

    }

    public void logProperties(String outPropFile) throws FileNotFoundException {
        File propFile = new File(outPropFile);
        if (!propFile.getParentFile().exists()){
            propFile.getParentFile().mkdirs();
        }
        PrintWriter pw = new PrintWriter(propFile);
        this.logUsedProperties(pw);
        zoneSystemProperties.logUsedProperties(pw);
        flowsProperties.logUsedProperties(pw);
        lDProperties.logUsedProperties(pw);
        sDProperties.logUsedProperties(pw);
        pw.close();
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

    public String getMatrixFileName() {
        return matrixFileName;
    }

    public void setMatrixFileName(String matrixFileName) {
        this.matrixFileName = matrixFileName;
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

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public int[] getSelectedZones() {
        return selectedZones;
    }

    public void setSelectedZones(int[] selectedZones) {
        this.selectedZones = selectedZones;
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
        return distributionCentersCatchmenAreaFile;
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
}
