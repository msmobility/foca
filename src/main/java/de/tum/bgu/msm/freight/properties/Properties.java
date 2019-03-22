package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Random;


public class Properties {

    private static Logger LOGGER = Logger.getLogger(Properties.class);

    private String zoneInputFile = "./input/zones_edit.csv";
    private String zoneShapeFile = "./input/shp/de_lkr_4326.shp";
    private String munichMicroZonesShapeFile = "input/shp/zones_4326_jobs.shp";
    private String regensburgMicroZonesShapeFile = "input/shp/zones_regensburg_4326.shp";
    private String idFieldInZonesShp = "RS";
    private String idFieldInMicroZonesShp = "id";
    private String matrixFileName = "./input/matrices/ketten-2010.csv";
    private String networkFile = "./networks/matsim/final_v3.xml.gz";
    private String simpleNetworkFile = "./networks/matsim/europe.xml.gz";
    private int iterations = 1;
    private double scaleFactor = 1.;
    private String runId = "assignmentFull";
    private Random rand = new Random(1);
    private int[] selectedDestinations = new int[]{-1};
    private boolean storeExpectedTimes = false;
    private String commodityAttributeFile = "input/commodities/commodity_groups_kba_ipf.csv";
    private  int daysPerYear = 365;

    private  boolean readEventsForCounts = true;
    private  String countStationLinkListFile = "input/matsim_links_stations.csv";
    private  String countsFileName = "counts_multi_day.csv";
    private String vehicleFile = "input/vehicleFile.xml";
    private String distributionCentersFile = "input/distributionCenters/distributionCenters.csv";
    private  String  terminalsFileName = "input/distributionCenters/intermodal_terminals.csv";

    private String[] jobTypes = new String[]{"Mnft","Util","Cons","Retl","Trns","Finc","Rlst","Admn","Serv","Agri"};
    private String makeTableFileName = "./input/makeUseCoefficients/makeTable.csv";
    private String useTableFileName = "./input/makeUseCoefficients/useTable.csv";

    private String parcelWeightDistributionFile = "./input/parcel_weight_distribution.csv";

    private double sampleFactorForParcels = 1.;


    public Properties(){
        FreightFlowUtils.setRandomNumber(this);
    }

    public void logUsedProperties(){
        for (Field x : Properties.class.getDeclaredFields()) {
            x.setAccessible(true);
            try {
                LOGGER.info(x.getName() + ": " + x.get(this).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public String getZoneInputFile() {
        return zoneInputFile;
    }
    public String getZoneShapeFile() {
        return zoneShapeFile;
    }

    public String getMunichMicroZonesShapeFile() {
        return munichMicroZonesShapeFile;
    }

    public String getRegensburgMicroZonesShapeFile() {
        return regensburgMicroZonesShapeFile;
    }

    public String getIdFieldInZonesShp() {
        return idFieldInZonesShp;
    }

    public void setZoneInputFile(String zoneInputFile) {
        this.zoneInputFile = zoneInputFile;
    }

    public void setZoneShapeFile(String zoneShapeFile) {
        this.zoneShapeFile = zoneShapeFile;
    }

    public void setMunichMicroZonesShapeFile(String munichMicroZonesShapeFile) {
        this.munichMicroZonesShapeFile = munichMicroZonesShapeFile;
    }

    public void setRegensburgMicroZonesShapeFile(String regensburgMicroZonesShapeFile) {
        this.regensburgMicroZonesShapeFile = regensburgMicroZonesShapeFile;
    }

    public void setIdFieldInZonesShp(String idFieldInZonesShp) {
        this.idFieldInZonesShp = idFieldInZonesShp;
    }

    public String getIdFieldInMicroZonesShp() {
        return idFieldInMicroZonesShp;
    }

    public void setIdFieldInMicroZonesShp(String idFieldInMicroZonesShp) {
        this.idFieldInMicroZonesShp = idFieldInMicroZonesShp;
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

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
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

    public int[] getSelectedDestinations() {
        return selectedDestinations;
    }

    public void setSelectedDestinations(int[] selectedDestinations) {
        this.selectedDestinations = selectedDestinations;
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

    public void setVehicleFile(String vehicleFile){
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
}
